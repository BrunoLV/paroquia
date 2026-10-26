package br.com.nsfatima.gestao.conselho.infrastructure.security;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.port.out.AtorAutenticadoPort;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno.Vinculo;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Traduz a autenticação do Spring Security para o {@link AtorAutenticado} do conselho. Os papéis globais vêm
 * das authorities ({@code ROLE_<PAPEL>} ou {@code ROLE_GLOBAL_<PAPEL>}); os papéis por organização vêm
 * apenas dos vínculos ativos do usuário, nunca das authorities genéricas, que não carregam escopo e deixariam
 * um secretário de um conselho agir em todos.
 */
@Component
public class AtorAutenticadoAdapter implements AtorAutenticadoPort {

    private static final String PREFIXO_GLOBAL = "ROLE_GLOBAL_";
    private static final String PREFIXO_ROLE = "ROLE_";

    @Override
    public AtorAutenticado atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new OperacaoProibidaException("Usuário não autenticado.");
        }
        UUID usuarioId = null;
        Map<UUID, Set<PapelOrganizacional>> papeisPorOrganizacao = new HashMap<>();
        if (autenticacao.getPrincipal() instanceof UsuarioDetails detalhes) {
            usuarioId = detalhes.getUsuarioId();
            for (Vinculo vinculo : detalhes.getVinculos()) {
                registrar(papeisPorOrganizacao, vinculo);
            }
        }
        return new AtorAutenticado(autenticacao.getName(), usuarioId, papeisGlobais(autenticacao),
                papeisPorOrganizacao);
    }

    private static void registrar(Map<UUID, Set<PapelOrganizacional>> destino, Vinculo vinculo) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(vinculo.role());
        if (papel != null && vinculo.organizationId() != null) {
            destino.computeIfAbsent(vinculo.organizationId(), id -> new HashSet<>()).add(papel);
        }
    }

    private static Set<PapelGlobal> papeisGlobais(Authentication autenticacao) {
        Set<PapelGlobal> papeis = EnumSet.noneOf(PapelGlobal.class);
        for (GrantedAuthority autoridade : autenticacao.getAuthorities()) {
            PapelGlobal papel = PapelGlobal.fromStoredValue(semPrefixo(autoridade.getAuthority()));
            if (papel != null) {
                papeis.add(papel);
            }
        }
        return papeis;
    }

    private static String semPrefixo(String autoridade) {
        if (autoridade.startsWith(PREFIXO_GLOBAL)) {
            return autoridade.substring(PREFIXO_GLOBAL.length());
        }
        return autoridade.startsWith(PREFIXO_ROLE) ? autoridade.substring(PREFIXO_ROLE.length()) : autoridade;
    }
}
