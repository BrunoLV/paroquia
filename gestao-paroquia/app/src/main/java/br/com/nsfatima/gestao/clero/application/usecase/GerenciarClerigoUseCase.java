package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EncargoClericalResponse;
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
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(UUID.randomUUID());
        aplicar(clerigo, request, actor, true);
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
        aplicar(clerigo, request, actor, false);
        atualizarEncargo(id, request.funcaoAtual());
        return resposta(clerigo);
    }

    @Transactional
    public void inativar(UUID id, String actor, UUID actorUsuarioId) {
        ClerigoEntity clerigo = encontrar(id);
        Instant agora = Instant.now();
        clerigo.setAtivo(false);
        clerigo.setDeletedAt(agora);
        clerigo.setAtualizadoPor(actor == null ? ACTOR_SYSTEM : actor);
        clerigo.setAtualizadoEmUtc(agora);

        encargos.findByClerigoIdAndAtualTrue(id).forEach(e -> {
            e.setAtual(false);
            e.setEncerradoEmUtc(agora);
        });

        usuarios.findByClerigoId(id).ifPresent(u -> {
            u.setEnabled(false);
            u.setAtualizadoPor(actor == null ? ACTOR_SYSTEM : actor);
            u.setAtualizadoEmUtc(agora);
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

    private void aplicar(ClerigoEntity c, ClerigoRequest r, String actor, boolean novo) {
        r.grauOrdem().validarOrdenacoes(
                r.dataOrdenacaoDiaconal(),
                r.dataOrdenacaoPresbiteral(),
                r.dataOrdenacaoEpiscopal());
        c.setNomeCivil(r.nomeCivil());
        c.setNomeEclesiastico(r.nomeEclesiastico());
        c.setDataNascimento(r.dataNascimento());
        c.setPaisOrigem(r.paisOrigem().toUpperCase());
        c.setCpf(r.cpf());
        c.setRg(r.rg());
        c.setDocumentoEstrangeiro(r.documentoEstrangeiro());
        c.setEmail(r.email());
        c.setCelular(r.celular());
        c.setWhatsapp(r.isWhatsapp());
        c.setTelefoneFixo(r.telefoneFixo());
        c.setEndereco(r.endereco());
        c.setGrauOrdem(r.grauOrdem());
        c.setDioceseIncardinacao(r.dioceseIncardinacao());
        c.setDataOrdenacaoDiaconal(r.dataOrdenacaoDiaconal());
        c.setDataOrdenacaoPresbiteral(r.dataOrdenacaoPresbiteral());
        c.setDataOrdenacaoEpiscopal(r.dataOrdenacaoEpiscopal());
        Instant agora = Instant.now();
        c.setAtualizadoEmUtc(agora);
        c.setAtualizadoPor(actor == null ? ACTOR_SYSTEM : actor);
        if (novo) {
            c.setCriadoEmUtc(agora);
            c.setCriadoPor(c.getAtualizadoPor());
        }
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
        UUID unidadeClero = unidades.findByTipoAndAtivoTrue(TipoUnidadeTerritorial.CLERO).stream()
                .findFirst()
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalStateException("A unidade territorial CLERO é obrigatória."));
        EncargoClericalEntity encargo = new EncargoClericalEntity();
        encargo.setId(UUID.randomUUID());
        encargo.setClerigoId(clerigoId);
        encargo.setUnidadeTerritorialId(unidadeClero);
        encargo.setFuncao(funcao);
        encargo.setAtual(true);
        encargo.setCriadoEmUtc(agora);
        encargos.save(encargo);
    }
}
