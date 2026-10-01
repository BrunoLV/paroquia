package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.HistoricoEntrada;
import br.com.nsfatima.gestao.sacramento.domain.repository.HistoricoAssentoRepository;
import br.com.nsfatima.gestao.sacramento.infrastructure.observability.SacramentoAuditPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Grava a trilha estrita do assento (Cân. 535 §4) na mesma transação da mutação: autor, perfil, IP,
 * ação e valores prévios/posteriores. O conteúdo vive apenas na tabela de histórico; o log global
 * recebe somente identificadores opacos (zero PII).
 */
@Service
public class SacramentoAuditoriaService {

    private final HistoricoAssentoRepository historico;
    private final SacramentoAuditPublisher publisher;
    private final ObjectMapper objectMapper;

    public SacramentoAuditoriaService(HistoricoAssentoRepository historico, SacramentoAuditPublisher publisher,
                                      ObjectMapper objectMapper) {
        this.historico = historico;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    /** Estado auditável do assento: o teor e tudo o que o complementa. */
    private record Estado(Object status, Object visibilidade, Object teor, Object averbacoes, Object ressalvas,
                          Object notificacoes, String homologadoPor, String motivoCancelamento) {
    }

    /**
     * Captura o estado auditável antes de uma mutação (o agregado é alterado in-place).
     *
     * @param assento agregado ainda não mutado
     * @return estado serializado
     */
    public String capturar(AssentoSacramental assento) {
        return objectMapper.writeValueAsString(new Estado(assento.getStatus(), assento.getVisibilidade(),
                assento.getTeor(), assento.getAverbacoes(), assento.getRessalvas(), assento.getNotificacoes(),
                assento.getHomologadoPor(), assento.getMotivoCancelamento()));
    }

    /**
     * Registra a ação na trilha do assento e no log global (identificadores opacos).
     *
     * @param antes estado capturado antes da mutação, ou {@code null} na lavratura
     * @param depois agregado já mutado
     */
    public void registrar(EventoActorContext ator, String acao, String antes, AssentoSacramental depois) {
        UUID assentoId = depois.getId().value();
        historico.registrar(new HistoricoEntrada(UUID.randomUUID(), assentoId, acao, ator.usuarioId(), ator.actor(),
                ator.role(), enderecoIp(), antes, capturar(depois), Instant.now()));
        publisher.publish(ator.actor(), acao, assentoId, depois.getTipo().name());
    }

    private static String enderecoIp() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes servlet ? servlet.getRequest().getRemoteAddr() : null;
    }
}
