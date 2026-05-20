package br.com.nsfatima.gestao.membro.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembroUseCaseTest {

    @Mock
    private MembroJpaRepository repository;

    @Mock
    private MembroMapper mapper;

    @Mock
    private MembroAuditPublisher auditPublisher;

    @InjectMocks
    private CreateMembroUseCase createUseCase;

    @InjectMocks
    private UpdateMembroUseCase updateUseCase;

    @InjectMocks
    private GetMembroUseCase getUseCase;

    private MembroRequest request;
    private MembroEntity entity;
    private MembroResponse response;

    @BeforeEach
    void setUp() {
        request = new MembroRequest(
                "João da Silva",
                LocalDate.of(1990, 1, 1),
                "joao@example.com",
                "11999999999",
                "Rua A, 123",
                null,
                null, null, null, null
        );

        entity = new MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setNomeCompleto(request.nomeCompleto());

        response = new MembroResponse(
                entity.getId(),
                entity.getNomeCompleto(),
                request.dataNascimento(),
                request.email(),
                request.telefone(),
                request.endereco(),
                null,
                true,
                null, null, null, null
        );
    }

    @Test
    @DisplayName("Deve criar um membro com sucesso")
    void deveCriarMembro() {
        when(repository.save(any(MembroEntity.class))).thenReturn(entity);
        when(mapper.toResponse(any(MembroEntity.class))).thenReturn(response);

        MembroResponse result = createUseCase.execute(request, "admin");

        assertNotNull(result);
        assertEquals("João da Silva", result.nomeCompleto());
        verify(repository).save(any(MembroEntity.class));
        verify(auditPublisher).publish(eq("admin"), eq("create"), anyString(), eq("success"));
    }

    @Test
    @DisplayName("Deve atualizar um membro com sucesso")
    void deveAtualizarMembro() {
        UUID id = entity.getId();
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(MembroEntity.class))).thenReturn(entity);
        when(mapper.toResponse(any(MembroEntity.class))).thenReturn(response);

        MembroResponse result = updateUseCase.execute(id, request, "admin");

        assertNotNull(result);
        verify(repository).save(entity);
        verify(auditPublisher).publish(eq("admin"), eq("update"), eq(id.toString()), eq("success"));
    }

    @Test
    @DisplayName("Deve obter um membro por ID")
    void deveObterMembro() {
        UUID id = entity.getId();
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        MembroResponse result = getUseCase.execute(id);

        assertNotNull(result);
        assertEquals(id, result.id());
    }

    @Test
    @DisplayName("Deve alternar status ativo do membro")
    void deveAlternarStatus() {
        UUID id = entity.getId();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        updateUseCase.toggleAtivo(id, false, "admin");

        assertFalse(entity.isAtivo());
        verify(repository).save(entity);
        verify(auditPublisher).publish(eq("admin"), eq("deactivate"), eq(id.toString()), eq("success"));
    }
}
