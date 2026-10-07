package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.List;
import java.util.UUID;

/**
 * Representa um membro efetivo de uma organização exibido no organograma ou na listagem de membros —
 * abrangendo tanto vínculos formais ({@code MembroGrupo}) quanto o coordenador/vice-coordenador registrados
 * como texto livre. {@code apto} e {@code pendencias} resumem a idoneidade pastoral do agente para a função
 * que ocupa; são nulos nos itens legados sem vínculo formal.
 */
public record OrganizacaoMembroResponse(
        UUID vinculoId,
        UUID fielId,
        String fielNome,
        String papel,
        boolean ativo,
        Boolean apto,
        List<String> pendencias
) {
}
