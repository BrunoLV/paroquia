package br.com.nsfatima.gestao.governanca.domain.model;

import br.com.nsfatima.gestao.governanca.domain.service.FeriadosNacionais;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio para incidentes de segurança (Art. 48 LGPD). Concentra as invariantes que antes viviam
 * no caso de uso: a máquina de estados de {@link StatusIncidente}, a linha do tempo coerente (detecção não
 * pode anteceder a ocorrência), a exigência de notificação à ANPD para o estado {@code NOTIFICADO} e o prazo
 * de comunicação de 3 dias úteis (sem fins de semana e feriados nacionais) a partir da detecção. É imutável: cada transição devolve nova instância.
 *
 * <p>O construtor canônico só reconstitui o que está persistido (não revalida regras de negócio, para que
 * registros legados continuem legíveis); as regras valem em {@link #registrar} e {@link #atualizar}.
 */
public record IncidenteSeguranca(
        UUID id,
        String titulo,
        String descricao,
        String tipoDadoAfetado,
        GravidadeIncidente categoriaGravidade,
        Instant dataOcorrencia,
        Instant dataDeteccao,
        String medidasMitigacao,
        boolean notificadoAnpd,
        Instant dataNotificacaoAnpd,
        String protocoloAnpd,
        boolean notificadoTitulares,
        Instant dataNotificacaoTitulares,
        StatusIncidente status
) {

    private static final int DIAS_UTEIS_PRAZO_COMUNICACAO = 3;

    public IncidenteSeguranca {
        Objects.requireNonNull(id, "Identificador do incidente é obrigatório.");
        Objects.requireNonNull(status, "Status do incidente é obrigatório.");
    }

    /**
     * Registra um novo incidente em {@code EM_ANALISE}.
     *
     * @throws IllegalArgumentException se a detecção anteceder a ocorrência
     */
    public static IncidenteSeguranca registrar(
            UUID id, String titulo, String descricao, String tipoDadoAfetado, GravidadeIncidente gravidade,
            Instant dataOcorrencia, Instant dataDeteccao, String medidasMitigacao) {
        Objects.requireNonNull(dataOcorrencia, "Data da ocorrência é obrigatória.");
        Objects.requireNonNull(dataDeteccao, "Data de detecção é obrigatória.");
        if (dataDeteccao.isBefore(dataOcorrencia)) {
            throw new IllegalArgumentException("A detecção não pode anteceder a ocorrência do incidente.");
        }
        return new IncidenteSeguranca(id, titulo, descricao, tipoDadoAfetado, gravidade, dataOcorrencia,
                dataDeteccao, medidasMitigacao, false, null, null, false, null, StatusIncidente.EM_ANALISE);
    }

    /**
     * Aplica uma atualização de status e de despacho de notificações. Campos {@code null} preservam o valor
     * atual. Ao chegar em {@code NOTIFICADO} a ANPD precisa constar como notificada; se a data da notificação
     * não for informada, assume {@code agora}.
     *
     * @throws IllegalStateException se a transição de estado for inválida
     * @throws IllegalArgumentException se {@code NOTIFICADO} for pretendido sem notificação à ANPD
     */
    public IncidenteSeguranca atualizar(
            StatusIncidente novoStatus, Boolean anpdNotificada, Instant dataAnpd, String protocolo,
            Boolean titularesNotificados, Instant dataTitulares, String medidas, Instant agora) {
        if (!status.podeTransitarPara(novoStatus)) {
            throw new IllegalStateException(
                    "Transição de status de incidente inválida: " + status + " para " + novoStatus + ".");
        }
        boolean anpd = anpdNotificada != null ? anpdNotificada : notificadoAnpd;
        if (novoStatus == StatusIncidente.NOTIFICADO && !anpd) {
            throw new IllegalArgumentException(
                    "O status NOTIFICADO exige que a notificação à ANPD esteja registrada.");
        }
        Instant dataAnpdFinal = dataAnpd != null ? dataAnpd : dataNotificacaoAnpd;
        if (anpd && dataAnpdFinal == null) {
            dataAnpdFinal = agora;
        }
        boolean titulares = titularesNotificados != null ? titularesNotificados : notificadoTitulares;
        Instant dataTitularesFinal = dataTitulares != null ? dataTitulares : dataNotificacaoTitulares;
        return new IncidenteSeguranca(id, titulo, descricao, tipoDadoAfetado, categoriaGravidade, dataOcorrencia,
                dataDeteccao, medidas != null ? medidas : medidasMitigacao, anpd, dataAnpdFinal,
                protocolo != null ? protocolo : protocoloAnpd, titulares, dataTitularesFinal, novoStatus);
    }

    /**
     * Prazo de comunicação à ANPD e aos titulares: 3 dias úteis após a detecção (Art. 48; RN-LGP-08), excluindo fins de semana e feriados nacionais; feriados estaduais/municipais não são considerados.
     */
    public Instant prazoComunicacao() {
        ZonedDateTime prazo = dataDeteccao.atZone(ZoneOffset.UTC);
        int restantes = DIAS_UTEIS_PRAZO_COMUNICACAO;
        while (restantes > 0) {
            prazo = prazo.plusDays(1);
            if (FeriadosNacionais.ehDiaUtil(prazo.toLocalDate())) {
                restantes--;
            }
        }
        return prazo.toInstant();
    }

    /**
     * Indica se o prazo de comunicação já passou sem a ANPD ter sido notificada e sem o incidente ter sido
     * encerrado (encerrar registra a decisão de que não havia risco relevante a comunicar).
     */
    public boolean prazoVencido(Instant agora) {
        return !notificadoAnpd && status != StatusIncidente.ENCERRADO && agora.isAfter(prazoComunicacao());
    }
}
