package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoManutencaoResponse;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Linha do tempo de manutenções de um ativo, da mais recente para a mais antiga. */
@Service
public class ListarHistoricoManutencaoAtivoUseCase {

    private final AtivoManutencaoRepository repository;

    public ListarHistoricoManutencaoAtivoUseCase(AtivoManutencaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AtivoManutencaoResponse> execute(UUID ativoId) {
        return repository.findPorAtivoOrdenadoPorInicioDesc(new AtivoPatrimonialId(ativoId)).stream()
                .map(AtivoManutencaoResponse::de)
                .toList();
    }
}
