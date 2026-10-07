package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista o inventário de ativos não excluídos, por unidade territorial proprietária ou completo,
 * ordenado por nome.
 */
@Service
public class ListarAtivosPorOrganizacaoUseCase {

    private final AtivoPatrimonialRepository repository;

    public ListarAtivosPorOrganizacaoUseCase(AtivoPatrimonialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AtivoPatrimonialResponse> executePorGrupo(UUID organizacaoId) {
        return repository.findByOrganizacao(organizacaoId).stream()
                .map(AtivoPatrimonialResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AtivoPatrimonialResponse> executeTodos() {
        return repository.findTodos().stream()
                .map(AtivoPatrimonialResponse::de)
                .toList();
    }
}
