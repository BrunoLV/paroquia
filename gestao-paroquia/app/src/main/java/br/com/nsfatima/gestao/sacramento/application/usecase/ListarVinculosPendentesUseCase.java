package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResumoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.VinculoPendenteResponse;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Fila de vínculos propostos pela Secretaria, consumida pela tela de Aprovações (exclusiva do Pároco). */
@Service
public class ListarVinculosPendentesUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final AssentoSacramentalRepository repository;
    private final ConsultaPessoaPort pessoas;

    public ListarVinculosPendentesUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                          AssentoSacramentalRepository repository, ConsultaPessoaPort pessoas) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.repository = repository;
        this.pessoas = pessoas;
    }

    @Transactional(readOnly = true)
    public List<VinculoPendenteResponse> execute() {
        EventoActorContext ator = actorResolver.resolveRequired();
        acesso.exigir(ator, OperacaoSacramental.APROVAR_VINCULO, Visibilidade.COMUM);
        return repository.listarComVinculoPendente(acesso.incluirReservados(ator)).stream()
                .flatMap(a -> a.getTeor().participantes().stream().filter(Participante::vinculoPendente)
                        .map(p -> paraResposta(a, p)))
                .toList();
    }

    private VinculoPendenteResponse paraResposta(AssentoSacramental a, Participante p) {
        boolean fiel = p.fielId() != null;
        var pessoaId = fiel ? p.fielId() : p.clerigoId();
        String nome = (fiel ? pessoas.nomeDoFiel(pessoaId) : pessoas.nomeDoClerigo(pessoaId)).orElse(null);
        AssentoResumoResponse resumo = AssentoResumoResponse.de(a);
        return new VinculoPendenteResponse(a.getId().value(), a.getTipo().name(), resumo.sujeito(),
                resumo.livro(), resumo.folha(), resumo.termo(), p.id(), p.papel().name(), p.nomeHistorico(),
                fiel ? "FIEL" : "CLERIGO", pessoaId, nome);
    }
}
