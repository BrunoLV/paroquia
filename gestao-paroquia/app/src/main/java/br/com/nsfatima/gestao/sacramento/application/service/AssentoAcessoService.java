package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoNaoEncontradoException;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.policy.AlcadaSacramentalPolicy;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Carrega assentos aplicando a matriz de alçadas. Assento de livro reservado inacessível ao ator
 * responde como inexistente, para não revelar sequer a existência do termo (matrimônio secreto).
 */
@Service
public class AssentoAcessoService {

    private final AssentoSacramentalRepository repository;
    private final AlcadaSacramentalPolicy alcada;

    public AssentoAcessoService(AssentoSacramentalRepository repository, AlcadaSacramentalPolicy alcada) {
        this.repository = repository;
        this.alcada = alcada;
    }

    public AssentoSacramental carregar(UUID id, EventoActorContext ator, OperacaoSacramental operacao) {
        AssentoSacramental assento = repository.findById(new AssentoSacramentalId(id))
                .filter(a -> a.getVisibilidade() == Visibilidade.COMUM || alcada.acessaLivroReservado(ator.role()))
                .orElseThrow(() -> new AssentoNaoEncontradoException("Assento sacramental não encontrado."));
        exigir(ator, operacao, assento.getVisibilidade());
        return assento;
    }

    public void exigir(EventoActorContext ator, OperacaoSacramental operacao, Visibilidade visibilidade) {
        if (!alcada.permitida(ator.role(), operacao, visibilidade)) {
            throw new OperacaoProibidaException("Perfil sem alçada para a operação sacramental " + operacao + ".");
        }
    }

    /** Indica se o ator aprova vínculos (Pároco); os vínculos da Secretaria ficam pendentes até a aprovação. */
    public boolean podeAprovarVinculo(EventoActorContext ator, Visibilidade visibilidade) {
        return alcada.permitida(ator.role(), OperacaoSacramental.APROVAR_VINCULO, visibilidade);
    }

    public boolean incluirReservados(EventoActorContext ator) {
        return alcada.acessaLivroReservado(ator.role());
    }
}
