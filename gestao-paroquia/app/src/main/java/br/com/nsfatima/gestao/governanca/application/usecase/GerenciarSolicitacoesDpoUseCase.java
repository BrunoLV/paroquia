package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.CriarSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ResponderSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitacaoDpoResponse;
import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;
import br.com.nsfatima.gestao.governanca.domain.service.MascaraDadosPessoais;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.SolicitacaoDpoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.SolicitacaoDpoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para gestão das solicitações de direitos dos titulares encaminhadas ao DPO (Arts. 18 e 19).
 *
 * <p>O protocolo tem a forma {@code DPO-AAAAMMDD-XXXXXXXXXXXX}: data de abertura mais 12 caracteres aleatórios
 * (SecureRandom). O sufixo longo é deliberado — a consulta por protocolo é pública e quem o conhece lê a
 * mensagem e a resposta; um sufixo curto e sequencial seria enumerável. O prazo de resposta é de 15 dias a
 * partir da abertura e a solicitação segue a máquina de estados de {@link StatusSolicitacaoDpo}.
 */
@Service
@Transactional
public class GerenciarSolicitacoesDpoUseCase {

    private static final int PRAZO_RESPOSTA_DIAS = 15;
    private static final int TAMANHO_SUFIXO_PROTOCOLO = 12;
    private static final String ALFABETO_PROTOCOLO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DATA_PROTOCOLO = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SolicitacaoDpoJpaRepository repository;

    public GerenciarSolicitacoesDpoUseCase(SolicitacaoDpoJpaRepository repository) {
        this.repository = repository;
    }

    /** Abre a solicitação e devolve a visão pública (dados pessoais mascarados). */
    public SolicitacaoDpoResponse criarSolicitacao(CriarSolicitacaoDpoRequest request) {
        Instant agora = Instant.now();
        SolicitacaoDpoEntity entity = new SolicitacaoDpoEntity();
        entity.setId(UUID.randomUUID());
        entity.setProtocolo(novoProtocolo(agora));
        entity.setTitularNome(request.titularNome());
        entity.setTitularEmail(request.titularEmail());
        entity.setTitularCpf(request.titularCpf());
        entity.setTipoSolicitacao(request.tipoSolicitacao());
        entity.setMensagem(request.mensagem());
        entity.setPrazoRespostaUtc(agora.plus(PRAZO_RESPOSTA_DIAS, ChronoUnit.DAYS));

        return mapPublico(repository.save(entity), agora);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoDpoResponse> listarTodas() {
        Instant agora = Instant.now();
        return repository.findAllByOrderByCriadoEmUtcDesc()
                .stream()
                .map(e -> mapCompleto(e, agora))
                .toList();
    }

    /** Consulta pública por protocolo: nome, e-mail e CPF saem mascarados. */
    @Transactional(readOnly = true)
    public SolicitacaoDpoResponse obterPorProtocolo(String protocolo) {
        return repository.findByProtocolo(protocolo)
                .map(e -> mapPublico(e, Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação não encontrada para o protocolo informado."));
    }

    /**
     * Registra a resposta do DPO. Uma solicitação concluída ({@code ATENDIDA}/{@code REJEITADA}) não é
     * reaberta nem respondida de novo; {@code RECEBIDA} não é destino válido. A data de resposta só é
     * gravada na conclusão.
     *
     * @throws IllegalStateException se a transição de status não for permitida
     */
    public SolicitacaoDpoResponse responder(UUID id, ResponderSolicitacaoDpoRequest request) {
        SolicitacaoDpoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));
        if (!entity.getStatus().podeTransitarPara(request.status())) {
            throw new IllegalStateException("Transição de status da solicitação inválida: "
                    + entity.getStatus() + " para " + request.status() + ".");
        }
        Instant agora = Instant.now();
        entity.setResposta(request.resposta());
        entity.setStatus(request.status());
        if (request.status().isFinal()) {
            entity.setRespondidoEmUtc(agora);
        }
        return mapCompleto(repository.save(entity), agora);
    }

    private String novoProtocolo(Instant agora) {
        StringBuilder sufixo = new StringBuilder(TAMANHO_SUFIXO_PROTOCOLO);
        for (int i = 0; i < TAMANHO_SUFIXO_PROTOCOLO; i++) {
            sufixo.append(ALFABETO_PROTOCOLO.charAt(RANDOM.nextInt(ALFABETO_PROTOCOLO.length())));
        }
        return "DPO-" + DATA_PROTOCOLO.format(agora.atZone(ZoneOffset.UTC)) + "-" + sufixo;
    }

    private SolicitacaoDpoResponse mapCompleto(SolicitacaoDpoEntity e, Instant agora) {
        return mapResponse(e, e.getTitularNome(), e.getTitularEmail(), e.getTitularCpf(), agora);
    }

    private SolicitacaoDpoResponse mapPublico(SolicitacaoDpoEntity e, Instant agora) {
        return mapResponse(e, MascaraDadosPessoais.nome(e.getTitularNome()),
                MascaraDadosPessoais.email(e.getTitularEmail()), MascaraDadosPessoais.cpf(e.getTitularCpf()), agora);
    }

    private SolicitacaoDpoResponse mapResponse(
            SolicitacaoDpoEntity e, String nome, String email, String cpf, Instant agora) {
        boolean concluida = e.getStatus() != null && e.getStatus().isFinal();
        Instant prazo = e.getPrazoRespostaUtc();
        boolean vencido = !concluida && prazo != null && agora.isAfter(prazo);
        Long diasRestantes = concluida || prazo == null ? null : ChronoUnit.DAYS.between(agora, prazo);
        return new SolicitacaoDpoResponse(
                e.getId(),
                e.getProtocolo(),
                nome,
                email,
                cpf,
                e.getTipoSolicitacao(),
                e.getMensagem(),
                e.getStatus(),
                e.getResposta(),
                prazo,
                e.getRespondidoEmUtc(),
                e.getCriadoEmUtc(),
                vencido,
                diasRestantes);
    }
}
