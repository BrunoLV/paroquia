package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EncargoClericalResponse;
import br.com.nsfatima.gestao.clero.domain.model.Clerigo;
import br.com.nsfatima.gestao.clero.domain.model.DadosCadastraisClerigo;
import br.com.nsfatima.gestao.clero.domain.model.EnderecoClerigo;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
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

    @Transactional
    public ClerigoResponse criar(ClerigoRequest request, String actor) {
        Clerigo agregado = Clerigo.cadastrar(UUID.randomUUID(), dados(request), actor, Instant.now());
        ClerigoEntity clerigo = mapper.toEntity(agregado);
        clerigos.save(clerigo);
        atualizarEncargo(clerigo.getId(), request.funcaoAtual());
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

    @Transactional
    public ClerigoResponse atualizar(UUID id, ClerigoRequest request, String actor) {
        ClerigoEntity clerigo = encontrar(id);
        Clerigo agregado = mapper.toDomain(clerigo);
        agregado.atualizarCadastro(dados(request), actor, Instant.now());
        mapper.aplicar(agregado, clerigo);
        atualizarEncargo(id, request.funcaoAtual());
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

    private void atualizarEncargo(UUID clerigoId, FuncaoEclesiastica funcao) {
        if (funcao == null) {
            return;
        }
        Instant agora = Instant.now();
        encargos.findByClerigoIdAndAtualTrue(clerigoId).forEach(e -> {
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
        encargo.setCriadoEmUtc(agora);
        encargos.save(encargo);
    }
}
