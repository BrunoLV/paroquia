package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Participante do assento. Se {@code fielId}/{@code clerigoId} forem informados e o nome vier em
 * branco, o snapshot do nome é preenchido a partir do cadastro; o vínculo é sempre opcional.
 */
public record ParticipanteRequest(
        UUID id,
        @NotNull PapelParticipante papel,
        @Size(max = 255) String nomeHistorico,
        @Size(max = 255) String documento,
        @Size(max = 255) String localNascimento,
        Boolean catolico,
        Boolean crismado,
        Integer idadeNaCelebracao,
        @Size(max = 16) String condicaoCanonica,
        UUID fielId,
        UUID clerigoId) {
}
