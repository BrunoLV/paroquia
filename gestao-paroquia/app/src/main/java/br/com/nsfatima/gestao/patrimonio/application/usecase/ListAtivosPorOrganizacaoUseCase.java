package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListAtivosPorOrganizacaoUseCase {

    private final AtivoPatrimonialJpaRepository repository;

    public ListAtivosPorOrganizacaoUseCase(AtivoPatrimonialJpaRepository repository) {
        this.repository = repository;
    }

    public List<AtivoPatrimonialResponse> execute(UUID organizacaoId) {
        return repository.findByOrganizacaoIdOrderByNomeAsc(organizacaoId).stream()
                .map(CreateAtivoPatrimonialUseCase::toResponse)
                .toList();
    }
}
