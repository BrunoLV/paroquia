package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.CriarSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ResponderSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitacaoDpoResponse;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.SolicitacaoDpoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.SolicitacaoDpoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para gestão das solicitações de direitos dos titulares encaminhadas ao DPO (Arts. 18 e 19).
 */
@Service
@Transactional
public class GerenciarSolicitacoesDpoUseCase {

    private final SolicitacaoDpoJpaRepository repository;

    public GerenciarSolicitacoesDpoUseCase(SolicitacaoDpoJpaRepository repository) {
        this.repository = repository;
    }

    public SolicitacaoDpoResponse criarSolicitacao(CriarSolicitacaoDpoRequest request) {
        SolicitacaoDpoEntity entity = new SolicitacaoDpoEntity();
        entity.setId(UUID.randomUUID());
        entity.setProtocolo(UUID.randomUUID().toString());
        entity.setTitularNome(request.titularNome());
        entity.setTitularEmail(request.titularEmail());
        entity.setTitularCpf(request.titularCpf());
        entity.setTipoSolicitacao(request.tipoSolicitacao());
        entity.setMensagem(request.mensagem());
        entity.setPrazoRespostaUtc(Instant.now().plus(15, ChronoUnit.DAYS));

        return mapResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoDpoResponse> listarTodas() {
        return repository.findAllByOrderByCriadoEmUtcDesc()
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoDpoResponse obterPorProtocolo(String protocolo) {
        return repository.findByProtocolo(protocolo)
                .map(this::mapResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação não encontrada para o protocolo informado."));
    }

    public SolicitacaoDpoResponse responder(UUID id, ResponderSolicitacaoDpoRequest request) {
        SolicitacaoDpoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        entity.setResposta(request.resposta());
        entity.setStatus(request.status());
        entity.setRespondidoEmUtc(Instant.now());

        return mapResponse(repository.save(entity));
    }

    private SolicitacaoDpoResponse mapResponse(SolicitacaoDpoEntity e) {
        return new SolicitacaoDpoResponse(
                e.getId(),
                e.getProtocolo(),
                e.getTitularNome(),
                e.getTitularEmail(),
                e.getTitularCpf(),
                e.getTipoSolicitacao(),
                e.getMensagem(),
                e.getStatus(),
                e.getResposta(),
                e.getPrazoRespostaUtc(),
                e.getRespondidoEmUtc(),
                e.getCriadoEmUtc());
    }
}
