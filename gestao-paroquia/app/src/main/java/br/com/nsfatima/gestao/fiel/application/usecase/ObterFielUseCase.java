package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Recupera os dados completos ou ficha de contato de um Fiel registrado na paróquia.
 *
 * REGRA DE NEGÓCIO (RN-FIE-01):
 * Fornece a visão detalhada canônica e civil respeitando as permissões de acesso
 * e anonimizando dados sensíveis conforme o perfil do operador.
 */
@Service
public class ObterFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public ObterFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional(readOnly = true)
    public FielResponse execute(UUID id) {
        return fielRepository.findById(id)
                .map(fielMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
    }

    /**
     * Busca os dados de contato sem lançar exceção quando o fiel não existe. Quem consulta por um id que pode
     * não ser de fiel (o clero, por exemplo) deve usar este método: uma exceção que atravessa um método
     * transacional marca a transação do chamador como "rollback-only" mesmo que ele a capture.
     *
     * @param id identificador do fiel
     * @return os dados de contato, ou vazio se não houver fiel com o id
     */
    @Transactional(readOnly = true)
    public Optional<FielContatoResponse> buscarContato(UUID id) {
        return fielRepository.findById(id).map(fielMapper::toContatoResponse);
    }

    @Transactional(readOnly = true)
    public FielContatoResponse executeContato(UUID id) {
        return fielRepository.findById(id)
                .map(fielMapper::toContatoResponse)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
    }
}
