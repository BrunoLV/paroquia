package br.com.nsfatima.gestao.calendario.support;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import java.time.Instant;
import java.util.UUID;

public final class ApprovalFlowTestFixtures {

    private ApprovalFlowTestFixtures() {
    }

    public static EventoEntity confirmedEvento(UUID eventoId, String titulo, UUID organizacaoId, Instant inicio,
            Instant fim) {
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo(titulo);
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(inicio);
        evento.setFimUtc(fim);
        evento.setStatus("CONFIRMADO");
        return evento;
    }

    public static AprovacaoEntity pendingAprovacao(UUID aprovacaoId, UUID eventoId, String tipoSolicitacao,
            String aprovadorPapel) {
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao(tipoSolicitacao);
        aprovacao.setAprovadorPapel(aprovadorPapel);
        aprovacao.setStatus("PENDENTE");
        aprovacao.setCriadoEmUtc(Instant.now());
        return aprovacao;
    }
}
