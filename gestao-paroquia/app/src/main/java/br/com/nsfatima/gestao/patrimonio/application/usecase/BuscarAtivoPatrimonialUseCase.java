package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Recupera o detalhe de um ativo patrimonial não excluído. */
@Service
public class BuscarAtivoPatrimonialUseCase {

    private final AtivoPatrimonialRepository repository;

    public BuscarAtivoPatrimonialUseCase(AtivoPatrimonialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AtivoPatrimonialResponse execute(UUID ativoId) {
        return repository.findById(new AtivoPatrimonialId(ativoId))
                .filter(a -> !a.estaExcluido())
                .map(AtivoPatrimonialResponse::de)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException("Ativo não encontrado: " + ativoId));
    }
}
