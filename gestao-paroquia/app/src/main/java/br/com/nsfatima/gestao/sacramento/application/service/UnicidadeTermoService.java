package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoInvalidoException;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Garante que livro/folha/termo não repetem um termo vigente do mesmo sacramento. */
@Service
public class UnicidadeTermoService {

    private final AssentoSacramentalRepository repository;

    public UnicidadeTermoService(AssentoSacramentalRepository repository) {
        this.repository = repository;
    }

    public void garantir(TipoSacramento tipo, TeorAssento teor, UUID ignorarId) {
        if (teor.referenciaLivro().completa() && repository.existeReferencia(tipo, teor.referenciaLivro().livro(),
                teor.referenciaLivro().folha(), teor.referenciaLivro().termo(), ignorarId)) {
            throw new AssentoInvalidoException(List.of("Livro, folha e termo já utilizados por outro assento vigente"));
        }
    }
}
