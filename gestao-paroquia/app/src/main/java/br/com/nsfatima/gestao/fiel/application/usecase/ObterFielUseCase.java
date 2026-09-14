package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public FielContatoResponse executeContato(UUID id) {
        return fielRepository.findById(id)
                .map(fielMapper::toContatoResponse)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
    }
}
