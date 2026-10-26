package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EncargoClericalResponse;
import br.com.nsfatima.gestao.clero.domain.model.Clerigo;
import br.com.nsfatima.gestao.clero.domain.model.DadosCadastraisClerigo;
import br.com.nsfatima.gestao.clero.domain.model.EnderecoClerigo;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.policy.PoliticaProvisaoEncargo;
import br.com.nsfatima.gestao.clero.infrastructure.observability.ClerigoAuditPublisher;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.mapper.ClerigoMapper;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquestra cadastro, inativação, provisões e perfil próprio sem expor PII em logs. */
@Service
public class GerenciarClerigoUseCase {

    private static final String ACTOR_SYSTEM = "system";

    private final ClerigoJpaRepository clerigos;
    private final EncargoClericalJpaRepository encargos;
    private final UnidadeTerritorialJpaRepository unidades;
    private final UsuarioJpaRepository usuarios;
    private final ClerigoAuditPublisher auditoria;
    private final ClerigoMapper mapper;

    public GerenciarClerigoUseCase(
            ClerigoJpaRepository clerigos,
            EncargoClericalJpaRepository encargos,
            UnidadeTerritorialJpaRepository unidades,
            UsuarioJpaRepository usuarios,
            ClerigoAuditPublisher auditoria,
            ClerigoMapper mapper) {
        this.clerigos = clerigos;
        this.encargos = encargos;
        this.unidades = unidades;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.mapper = mapper;
    }

    /**
     * Cadastra o clérigo. Conferir função (encargo) no cadastro exige alçada de provisão.
     *
     * @param papelAtor papel do ator, usado para autorizar a provisão de função
     * @throws AccessDeniedException se o ator informar função sem ser Pároco ou Bispo Diocesano
     * @throws IllegalArgumentException se já houver outro Pároco ativo e a função informada for de Pároco
     */
    @Transactional
    public ClerigoResponse criar(ClerigoRequest request, String actor, String papelAtor) {
        Clerigo agregado = Clerigo.cadastrar(UUID.randomUUID(), dados(request), actor, Instant.now());
        ClerigoEntity clerigo = mapper.toEntity(agregado);
        clerigos.save(clerigo);
        atualizarEncargo(clerigo.getId(), request.funcaoAtual(), papelAtor);
        return resposta(clerigo);
    }

    @Transactional(readOnly = true)
    public List<ClerigoResponse> listar() {
        return clerigos.findByAtivoTrueOrderByNomeEclesiasticoAsc().stream()
                .map(this::resposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClerigoResponse obter(UUID id) {
        return resposta(encontrar(id));
    }

    @Transactional(readOnly = true)
    public ClerigoResponse obterPorUsuario(UUID usuarioId) {
        UsuarioEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (usuario.getClerigoId() == null) {
            throw new IllegalStateException("Usuário autenticado não é um clérigo.");
        }
        return obter(usuario.getClerigoId());
    }

    /**
     * Atualiza o cadastro. A função só é tocada se mudou em relação ao encargo atual; mudá-la exige alçada de
     * provisão, e reenviar a mesma função não encerra nem reabre o encargo.
     *
     * @param papelAtor papel do ator, usado para autorizar a mudança de função
     * @throws AccessDeniedException se a função mudou e o ator não for Pároco ou Bispo Diocesano
     * @throws IllegalArgumentException se já houver outro Pároco ativo e a função informada for de Pároco
     */
    @Transactional
    public ClerigoResponse atualizar(UUID id, ClerigoRequest request, String actor, String papelAtor) {
        ClerigoEntity clerigo = encontrar(id);
        Clerigo agregado = mapper.toDomain(clerigo);
        agregado.atualizarCadastro(dados(request), actor, Instant.now());
        mapper.aplicar(agregado, clerigo);
        atualizarEncargo(id, request.funcaoAtual(), papelAtor);
        return resposta(clerigo);
    }

    @Transactional
    public void inativar(UUID id, String actor, UUID actorUsuarioId) {
        ClerigoEntity clerigo = encontrar(id);
        Instant agora = Instant.now();
        Clerigo agregado = mapper.toDomain(clerigo);
        agregado.inativar(actor, agora);
        mapper.aplicar(agregado, clerigo);

        encargos.findByClerigoIdAndAtualTrue(id).forEach(e -> {
            e.setAtual(false);
            e.setEncerradoEmUtc(agora);
        });

        usuarios.findByClerigoId(id).ifPresent(u -> {
            u.setEnabled(false);
            usuarios.save(u);
        });

        clerigos.save(clerigo);
        if (actorUsuarioId != null) {
            auditoria.publicarInativacao(id, actorUsuarioId);
        }
    }

    @Transactional
    public void inativar(UUID id, String actor) {
        inativar(id, actor, null);
    }

    @Transactional(readOnly = true)
    public List<ClerigoInstitucionalResponse> diretorio() {
        return clerigos.findByAtivoTrueOrderByNomeEclesiasticoAsc().stream()
                .map(c -> mapper.institucional(c, funcaoAtual(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EncargoClericalResponse> listarEncargos(UUID clerigoId) {
        encontrar(clerigoId);
        return encargos.findByClerigoIdOrderByCriadoEmUtcDesc(clerigoId).stream()
                .map(e -> new EncargoClericalResponse(
                        e.getId(),
                        e.getClerigoId(),
                        e.getUnidadeTerritorialId(),
                        e.getFuncao(),
                        e.isAtual(),
                        e.getCriadoEmUtc(),
                        e.getEncerradoEmUtc()))
                .toList();
    }

    private ClerigoEntity encontrar(UUID id) {
        return clerigos.findById(id)
                .filter(ClerigoEntity::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Clérigo não encontrado."));
    }

    private ClerigoResponse resposta(ClerigoEntity clerigo) {
        return mapper.completo(clerigo, funcaoAtual(clerigo.getId()));
    }

    private FuncaoEclesiastica funcaoAtual(UUID id) {
        return encargos.findByClerigoIdAndAtualTrue(id).stream()
                .findFirst()
                .map(EncargoClericalEntity::getFuncao)
                .orElse(null);
    }

    private static DadosCadastraisClerigo dados(ClerigoRequest r) {
        EnderecoClerigo endereco = r.endereco() == null ? EnderecoClerigo.vazio() : new EnderecoClerigo(
                r.endereco().cep(),
                r.endereco().logradouro(),
                r.endereco().numero(),
                r.endereco().complemento(),
                r.endereco().bairro(),
                r.endereco().cidade(),
                r.endereco().uf());
        return new DadosCadastraisClerigo(
                r.nomeCivil(), r.nomeEclesiastico(), r.dataNascimento(), r.paisOrigem(),
                r.cpf(), r.rg(), r.documentoEstrangeiro(), r.email(), r.celular(), r.isWhatsapp(),
                r.telefoneFixo(), endereco, r.grauOrdem(), r.dioceseIncardinacao(),
                r.dataOrdenacaoDiaconal(), r.dataOrdenacaoPresbiteral(), r.dataOrdenacaoEpiscopal());
    }

    private void atualizarEncargo(UUID clerigoId, FuncaoEclesiastica funcao, String papelAtor) {
        if (funcao == null) {
            return;
        }
        List<EncargoClericalEntity> atuais = encargos.findByClerigoIdAndAtualTrue(clerigoId);
        if (atuais.stream().anyMatch(e -> e.getFuncao() == funcao)) {
            return;
        }
        exigirProvisao(clerigoId, funcao, papelAtor);
        Instant agora = Instant.now();
        atuais.forEach(e -> {
            e.setAtual(false);
            e.setEncerradoEmUtc(agora);
        });
        UUID unidadeSede = unidades.findByTipoAndAtivoTrue(TipoUnidadeTerritorial.MATRIZ).stream()
                .findFirst()
                .map(UnidadeTerritorialEntity::getId)
                .or(() -> unidades.findAll().stream()
                        .filter(UnidadeTerritorialEntity::isAtivo)
                        .map(UnidadeTerritorialEntity::getId)
                        .findFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "A unidade territorial sede (MATRIZ) é obrigatória para atribuição de encargo."));
        EncargoClericalEntity encargo = new EncargoClericalEntity();
        encargo.setId(UUID.randomUUID());
        encargo.setClerigoId(clerigoId);
        encargo.setUnidadeTerritorialId(unidadeSede);
        encargo.setFuncao(funcao);
        encargo.setAtual(true);
        encargos.save(encargo);
    }

    private void exigirProvisao(UUID clerigoId, FuncaoEclesiastica funcao, String papelAtor) {
        if (!PoliticaProvisaoEncargo.temAlcada(papelAtor)) {
            throw new AccessDeniedException(
                    "Apenas o Pároco ou o Bispo Diocesano podem conferir ou alterar a função de um clérigo");
        }
        if (PoliticaProvisaoEncargo.exigeTitularUnico(funcao)) {
            boolean outroTitular = PoliticaProvisaoEncargo.funcoesDeParoco().stream()
                    .flatMap(f -> encargos.findByFuncaoAndAtualTrue(f).stream())
                    .anyMatch(e -> !clerigoId.equals(e.getClerigoId()));
            if (outroTitular) {
                throw new IllegalArgumentException("Já existe um Pároco (ou Administrador Paroquial) ativo; "
                        + "encerre o encargo dele antes de nomear outro");
            }
        }
    }
}
