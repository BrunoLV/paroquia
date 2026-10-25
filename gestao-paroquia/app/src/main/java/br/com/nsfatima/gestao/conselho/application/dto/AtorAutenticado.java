package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Usuário autenticado que age sobre as atas: identificador de login (nunca nome pessoal), papéis globais e
 * os papéis que exerce em cada organização a que está vinculado. É o retrato de segurança que a camada de
 * aplicação usa para decidir leitura, escopo e autorização.
 */
public record AtorAutenticado(
        String login,
        UUID usuarioId,
        Set<PapelGlobal> papeisGlobais,
        Map<UUID, Set<PapelOrganizacional>> papeisPorOrganizacao) {

    private static final int PERFIL_MAX = 64;

    public AtorAutenticado {
        Objects.requireNonNull(login, "O login do ator é obrigatório.");
        papeisGlobais = Set.copyOf(papeisGlobais);
        Map<UUID, Set<PapelOrganizacional>> copia = new HashMap<>();
        papeisPorOrganizacao.forEach((organizacao, papeis) -> copia.put(organizacao, Set.copyOf(papeis)));
        papeisPorOrganizacao = Collections.unmodifiableMap(copia);
    }

    /** Papéis do ator na organização informada; vazio sem vínculo. */
    public Set<PapelOrganizacional> papeisNa(UUID organizacaoId) {
        return organizacaoId == null ? Set.of() : papeisPorOrganizacao.getOrDefault(organizacaoId, Set.of());
    }

    /** Organizações em que o ator tem algum vínculo ativo; base do escopo de leitura. */
    public Set<UUID> organizacoesComVinculo() {
        return papeisPorOrganizacao.keySet();
    }

    /** Perfil registrado na trilha: papéis globais e, se houver, os do ator na organização da ata. */
    public String perfil(UUID organizacaoId) {
        List<String> nomes = new ArrayList<>();
        papeisGlobais.forEach(papel -> nomes.add(papel.name()));
        papeisNa(organizacaoId).forEach(papel -> nomes.add(papel.name()));
        Collections.sort(nomes);
        String perfil = String.join(",", nomes);
        return perfil.length() > PERFIL_MAX ? perfil.substring(0, PERFIL_MAX) : perfil;
    }
}
