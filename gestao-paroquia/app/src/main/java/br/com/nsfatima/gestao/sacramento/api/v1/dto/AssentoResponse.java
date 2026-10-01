package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Averbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.NotificacaoCanonica;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.Ressalva;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Visão completa do assento para operadores autorizados (Pároco/Secretaria); não é uma certidão. */
public record AssentoResponse(
        UUID id,
        TipoSacramento tipo,
        StatusAssento status,
        Visibilidade visibilidade,
        LocalDate dataLavratura,
        String livro,
        String folha,
        String termo,
        String paroquiaCustodiante,
        LocalDateTime dataCelebracao,
        String localCelebracao,
        boolean dadoSensivel,
        Map<String, String> dadosEspecificos,
        List<ParticipanteResponse> participantes,
        List<AverbacaoResponse> averbacoes,
        List<RessalvaResponse> ressalvas,
        List<NotificacaoResponse> notificacoes,
        String homologadoPor,
        Instant homologadoEmUtc,
        String motivoCancelamento) {

    /** Participante com snapshot textual e ponteiros opcionais de vínculo. */
    public record ParticipanteResponse(UUID id, String papel, String nomeHistorico, String documento,
                                       String localNascimento, Boolean catolico, Boolean crismado,
                                       Integer idadeNaCelebracao, String condicaoCanonica, UUID fielId,
                                       UUID clerigoId, boolean vinculoPendente) {
        static ParticipanteResponse de(Participante p) {
            return new ParticipanteResponse(p.id(), p.papel().name(), p.nomeHistorico(), p.documento(),
                    p.localNascimento(), p.catolico(), p.crismado(), p.idadeNaCelebracao(), p.condicaoCanonica(),
                    p.fielId(), p.clerigoId(), p.vinculoPendente());
        }
    }

    /** Nota marginal do assento de Batismo. */
    public record AverbacaoResponse(UUID id, String tipoEvento, LocalDate dataAverbacao, String numeroDocumento,
                                    String teorNota, boolean reservada, String status, String inseridaPor,
                                    String homologadaPor) {
        static AverbacaoResponse de(Averbacao a) {
            return new AverbacaoResponse(a.id(), a.tipoEvento().name(), a.dataAverbacao(), a.numeroDocumento(),
                    a.teorNota(), a.reservada(), a.status().name(), a.inseridaPor(), a.homologadaPor());
        }
    }

    /** Ressalva de retificação material. */
    public record RessalvaResponse(UUID id, String campo, String valorOriginal, String valorCorrigido,
                                   String justificativa, String atestadaPor, Instant atestadaEmUtc) {
        static RessalvaResponse de(Ressalva r) {
            return new RessalvaResponse(r.id(), r.campo(), r.valorOriginal(), r.valorCorrigido(), r.justificativa(),
                    r.atestadaPor(), r.atestadaEmUtc());
        }
    }

    /** Notificação canônica à paróquia de batismo. */
    public record NotificacaoResponse(UUID id, String destinatarioParoquia, LocalDate emitidaEm,
                                      LocalDate enviadaEm) {
        static NotificacaoResponse de(NotificacaoCanonica n) {
            return new NotificacaoResponse(n.id(), n.destinatarioParoquia(), n.emitidaEm(), n.enviadaEm());
        }
    }

    public static AssentoResponse de(AssentoSacramental a) {
        return new AssentoResponse(a.getId().value(), a.getTipo(), a.getStatus(), a.getVisibilidade(),
                a.getDataLavratura(), a.getTeor().referenciaLivro().livro(), a.getTeor().referenciaLivro().folha(),
                a.getTeor().referenciaLivro().termo(), a.getTeor().paroquiaCustodiante(),
                a.getTeor().dataCelebracao(), a.getTeor().localCelebracao(), a.getTeor().dadoSensivel(),
                a.getTeor().dadosEspecificos(),
                a.getTeor().participantes().stream().map(ParticipanteResponse::de).toList(),
                a.getAverbacoes().stream().map(AverbacaoResponse::de).toList(),
                a.getRessalvas().stream().map(RessalvaResponse::de).toList(),
                a.getNotificacoes().stream().map(NotificacaoResponse::de).toList(),
                a.getHomologadoPor(), a.getHomologadoEmUtc(), a.getMotivoCancelamento());
    }
}
