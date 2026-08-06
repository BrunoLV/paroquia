package br.com.nsfatima.gestao.calendario.support;

import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;

import java.time.Instant;
import java.util.UUID;

public final class AprovacaoFlowTestFixtures {

    private AprovacaoFlowTestFixtures() {
    }

    public static EventoEntity confirmedEvento(UUID eventoId, String titulo, UUID grupoParoquialId, Instant inicio,
                                               Instant fim) {
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo(titulo);
        evento.setGrupoParoquialResponsavelId(grupoParoquialId);
        evento.setInicioUtc(inicio);
        evento.setFimUtc(fim);
        evento.setStatus("CONFIRMADO");
        return evento;
    }

    public static AprovacaoEntity pendenteAprovacao(UUID aprovacaoId, UUID eventoId, String tipoSolicitacao,
                                                   AprovadorPapel aprovadorPapel) {
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao(tipoSolicitacao);
        aprovacao.setAprovadorPapel(aprovadorPapel);
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        return aprovacao;
    }
}
