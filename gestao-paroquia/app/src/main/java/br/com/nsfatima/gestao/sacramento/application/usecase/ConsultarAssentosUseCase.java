package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResumoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.HistoricoResponse;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import br.com.nsfatima.gestao.sacramento.domain.repository.HistoricoAssentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Consultas de leitura. O livro reservado só aparece a quem tem alçada sobre ele. */
@Service
public class ConsultarAssentosUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final AssentoSacramentalRepository repository;
    private final HistoricoAssentoRepository historicoRepository;

    public ConsultarAssentosUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                    AssentoSacramentalRepository repository, HistoricoAssentoRepository historico) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.repository = repository;
        this.historicoRepository = historico;
    }

    @Transactional(readOnly = true)
    public List<AssentoResumoResponse> listar(TipoSacramento tipo) {
        EventoActorContext ator = actorResolver.resolveRequired();
        acesso.exigir(ator, OperacaoSacramental.LER, Visibilidade.COMUM);
        return repository.listar(tipo, acesso.incluirReservados(ator)).stream().map(AssentoResumoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AssentoResponse buscar(UUID id) {
        EventoActorContext ator = actorResolver.resolveRequired();
        return AssentoResponse.de(acesso.carregar(id, ator, OperacaoSacramental.LER));
    }

    /** Visão 360º: assentos vinculados a um fiel ou clérigo. */
    @Transactional(readOnly = true)
    public List<AssentoResumoResponse> porPessoa(UUID pessoaId) {
        EventoActorContext ator = actorResolver.resolveRequired();
        acesso.exigir(ator, OperacaoSacramental.LER, Visibilidade.COMUM);
        return repository.buscarPorVinculo(pessoaId, acesso.incluirReservados(ator)).stream()
                .map(a -> AssentoResumoResponse.de(a, pessoaId)).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> historico(UUID id) {
        EventoActorContext ator = actorResolver.resolveRequired();
        acesso.carregar(id, ator, OperacaoSacramental.HOMOLOGAR);
        return historicoRepository.listarPorAssento(id).stream().map(HistoricoResponse::de).toList();
    }
}
