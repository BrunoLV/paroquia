package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

/**
 * Rejeita a atribuição de papéis globais (`PAROCO`, `VIGARIO`, `ADMIN`) via {@code MembroGrupo} — esses
 * papéis só podem ser concedidos via {@code AtribuirPapelGlobalUseCase} (track
 * {@code roles_globais_vs_escopo}). `MESA_CPP`/`MEMBRO_CPP` e os demais papéis escopados continuam liberados
 * normalmente.
 */
final class PapelGlobalValidation {

    private PapelGlobalValidation() {
    }

    static void rejectIfGlobal(PapelOrganizacional papel) {
        if (papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.VIGARIO
                || papel == PapelOrganizacional.ADMIN) {
            throw new IllegalArgumentException(
                    "Papel " + papel + " é global — atribua via AtribuirPapelGlobalUseCase, não MembroGrupo");
        }
    }
}
