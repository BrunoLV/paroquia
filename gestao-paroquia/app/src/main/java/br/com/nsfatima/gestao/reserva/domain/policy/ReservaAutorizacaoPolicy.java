package br.com.nsfatima.gestao.reserva.domain.policy;

import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato puro de política para a matriz de permissões e alçadas de aprovação do módulo de Reservas.
 */
public interface ReservaAutorizacaoPolicy {

    /**
     * Determina se a reserva nasce com status CONFIRMADA ou PENDENTE com base no perfil do solicitante.
     */
    StatusReserva resolveStatusInicial(UsuarioActor solicitante, UUID pastoralMovimentoId);

    /**
     * Valida se o operador tem autorização para aprovar/rejeitar a solicitação pendente.
     */
    void assertAutorizacaoAprovacao(UsuarioActor operador, Reserva reserva);

    /**
     * Valida se o operador pode cancelar a reserva conforme a alçada de cada perfil.
     */
    void assertAutorizacaoCancelamento(UsuarioActor operador, Reserva reserva, Instant agora);

    /**
     * Valida se o operador pode encerrar o uso de um local reservado: quem tem alçada administrativa, o
     * coordenador da pastoral ou movimento e o próprio solicitante.
     */
    void assertAutorizacaoFinalizacao(UsuarioActor operador, Reserva reserva);
}
