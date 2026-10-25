package br.com.nsfatima.gestao.conselho.infrastructure.acl;

import br.com.nsfatima.gestao.clero.application.usecase.GerenciarClerigoUseCase;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.conselho.application.dto.ClerigoAtivo;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.domain.model.FuncaoPresenca;
import br.com.nsfatima.gestao.fiel.application.usecase.ObterFielUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.ObterUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListarOrganizacaoMembrosUseCase;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Camada anticorrupção sobre os contextos de fiéis e organizações. Passa pelos casos de uso públicos desses
 * contextos, nunca pela persistência deles, e devolve ao conselho só identificadores e nomes.
 */
@Component
public class PessoasAdapter implements PessoasPort {

    private final ListarOrganizacaoMembrosUseCase membros;
    private final ObterFielUseCase fieis;
    private final ObterUsuarioUseCase usuarios;
    private final GerenciarClerigoUseCase clero;

    public PessoasAdapter(ListarOrganizacaoMembrosUseCase membros, ObterFielUseCase fieis,
                          ObterUsuarioUseCase usuarios, GerenciarClerigoUseCase clero) {
        this.membros = membros;
        this.fieis = fieis;
        this.usuarios = usuarios;
        this.clero = clero;
    }

    @Override
    public Optional<UUID> fielDoUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(usuarios.execute(usuarioId).fielId());
        } catch (UsuarioNaoEncontradoException naoEncontrado) {
            return Optional.empty();
        }
    }

    @Override
    public Set<UUID> membrosAtivos(UUID organizacaoId) {
        return membros.execute(organizacaoId).stream()
                .filter(OrganizacaoMembroResponse::ativo)
                .map(OrganizacaoMembroResponse::fielId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public List<ClerigoAtivo> clerigosAtivos() {
        return clero.diretorio().stream().map(c -> new ClerigoAtivo(c.clerigoId(), c.nomeEclesiastico(),
                funcaoNaReuniao(c.funcaoAtual()), ehParoco(c.funcaoAtual()))).toList();
    }

    @Override
    public Map<UUID, String> nomes(Collection<UUID> ids) {
        Map<UUID, String> nomes = new HashMap<>();
        Map<UUID, String> clerigos = null;
        for (UUID id : ids) {
            try {
                nomes.put(id, fieis.executeContato(id).nome());
            } catch (IllegalArgumentException naoEhFiel) {
                if (clerigos == null) {
                    clerigos = clerigosAtivos().stream()
                            .collect(Collectors.toMap(ClerigoAtivo::id, ClerigoAtivo::nome));
                }
                if (clerigos.containsKey(id)) {
                    nomes.put(id, clerigos.get(id));
                }
            }
        }
        return nomes;
    }

    private static boolean ehParoco(FuncaoEclesiastica funcao) {
        return funcao == FuncaoEclesiastica.PAROCO || funcao == FuncaoEclesiastica.ADMINISTRADOR_PAROQUIAL;
    }

    private static String funcaoNaReuniao(FuncaoEclesiastica funcao) {
        if (funcao == null) {
            return null;
        }
        return switch (funcao) {
            case PAROCO, ADMINISTRADOR_PAROQUIAL -> FuncaoPresenca.PAROCO.getRotulo();
            case VIGARIO_PAROQUIAL -> FuncaoPresenca.VIGARIO_PAROQUIAL.getRotulo();
            case DIACONO_AUXILIAR -> FuncaoPresenca.DIACONO.getRotulo();
            case BISPO_DIOCESANO, EMERITO -> null;
        };
    }
}
