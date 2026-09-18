package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitacaoAjusteCadastralResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitarAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.infrastructure.observability.FielAuditPublisher;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.SolicitacaoAjusteCadastralEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.SolicitacaoAjusteCadastralJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case para solicitação e decisão de ajustes cadastrais pelos próprios fiéis. */
@Service
public class GerenciarAjusteCadastralUseCase {

    private static final String STATUS_PENDENTE = "PENDENTE";

    private final UsuarioJpaRepository usuarios;
    private final FielJpaRepository fieis;
    private final SolicitacaoAjusteCadastralJpaRepository ajustes;
    private final GerenciarNotificacaoUseCase notificacoes;
    private final FielAuditPublisher auditoria;

    public GerenciarAjusteCadastralUseCase(
            UsuarioJpaRepository usuarios,
            FielJpaRepository fieis,
            SolicitacaoAjusteCadastralJpaRepository ajustes,
            GerenciarNotificacaoUseCase notificacoes,
            FielAuditPublisher auditoria) {
        this.usuarios = usuarios;
        this.fieis = fieis;
        this.ajustes = ajustes;
        this.notificacoes = notificacoes;
        this.auditoria = auditoria;
    }

    @Transactional
    public UUID solicitar(UUID usuarioId, SolicitarAjusteCadastralRequest request) {
        var usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        var fiel = fieis.findById(usuario.getFielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));

        var solicitacao = new SolicitacaoAjusteCadastralEntity();
        solicitacao.setId(UUID.randomUUID());
        solicitacao.setUsuarioSolicitanteId(usuarioId);
        solicitacao.setFielId(usuario.getFielId());
        solicitacao.setEmail(request.email());
        solicitacao.setCelular(request.celular());
        solicitacao.setTelefoneFixo(request.telefoneFixo());
        solicitacao.setEnderecoLogradouro(request.endereco().logradouro());
        solicitacao.setEnderecoNumero(request.endereco().numero());
        solicitacao.setEnderecoComplemento(request.endereco().complemento());
        solicitacao.setEnderecoBairro(request.endereco().bairro());
        solicitacao.setEnderecoCep(request.endereco().cep());
        solicitacao.setEnderecoCidade(request.endereco().cidade());
        solicitacao.setEnderecoEstado(request.endereco().estado());
        solicitacao.setStatus(STATUS_PENDENTE);
        solicitacao.setCriadoEmUtc(Instant.now());

        var salva = ajustes.save(solicitacao);
        auditoria.publicarSolicitacaoAjuste(
                salva.getId(),
                fiel.getId(),
                usuarioId,
                fiel.getUnidadeTerritorialId());
        return salva.getId();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAjusteCadastralResponse> pendentes() {
        return ajustes.findByStatusOrderByCriadoEmUtcAsc(STATUS_PENDENTE).stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAjusteCadastralResponse> pendentesDoFiel(UUID fielId) {
        return ajustes.findByFielIdAndStatusOrderByCriadoEmUtcDesc(fielId, STATUS_PENDENTE).stream()
                .map(this::paraResposta)
                .toList();
    }

    private SolicitacaoAjusteCadastralResponse paraResposta(SolicitacaoAjusteCadastralEntity entity) {
        return new SolicitacaoAjusteCadastralResponse(
                entity.getId(),
                entity.getFielId(),
                entity.getStatus(),
                entity.getEmail(),
                entity.getCelular(),
                entity.getTelefoneFixo(),
                new EnderecoDto(
                        entity.getEnderecoCep(),
                        entity.getEnderecoLogradouro(),
                        entity.getEnderecoNumero(),
                        entity.getEnderecoComplemento(),
                        entity.getEnderecoBairro(),
                        entity.getEnderecoCidade(),
                        entity.getEnderecoEstado()),
                entity.getCriadoEmUtc());
    }

    @Transactional
    public void decidir(UUID id, UUID analista, boolean aprovar, String justificativa) {
        var solicitacao = ajustes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));
        if (!STATUS_PENDENTE.equals(solicitacao.getStatus())) {
            throw new IllegalStateException("Solicitação já analisada.");
        }
        var fiel = fieis.findById(solicitacao.getFielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));

        if (aprovar) {
            fiel.setEmail(solicitacao.getEmail());
            fiel.setCelular(solicitacao.getCelular());
            fiel.setTelefoneFixo(solicitacao.getTelefoneFixo());
            fiel.setEnderecoLogradouro(solicitacao.getEnderecoLogradouro());
            fiel.setEnderecoNumero(solicitacao.getEnderecoNumero());
            fiel.setEnderecoComplemento(solicitacao.getEnderecoComplemento());
            fiel.setEnderecoBairro(solicitacao.getEnderecoBairro());
            fiel.setEnderecoCep(solicitacao.getEnderecoCep());
            fiel.setEnderecoCidade(solicitacao.getEnderecoCidade());
            fiel.setEnderecoEstado(solicitacao.getEnderecoEstado());
            fieis.save(fiel);
        }

        solicitacao.setStatus(aprovar ? "APROVADO" : "REJEITADO");
        solicitacao.setJustificativaAnalise(justificativa);
        solicitacao.setAnalisadoPorUsuarioId(analista);
        solicitacao.setAnalisadoEmUtc(Instant.now());
        ajustes.save(solicitacao);

        auditoria.publicarDecisaoAjuste(
                solicitacao.getId(),
                fiel.getId(),
                analista,
                fiel.getUnidadeTerritorialId(),
                aprovar);

        notificacoes.publicar(
                solicitacao.getUsuarioSolicitanteId(),
                aprovar ? "Sua solicitação de ajuste cadastral foi aprovada."
                        : "Sua solicitação de ajuste cadastral foi rejeitada.");
    }
}
