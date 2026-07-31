package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroUseCaseTest {

    @Mock
    private MembroJpaRepository repository;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private OrganizacaoJpaRepository organizacaoRepository;

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
    private UUID usuarioId;
    private UUID organizacaoId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        request = new MembroRequest(
                usuarioId,
                organizacaoId,
                "MEMBRO",
                LocalDate.now(),
                null,
                true
        );

        entity = new MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setPapel("MEMBRO");

        response = new MembroResponse(
                entity.getId(),
                usuarioId,
                "João da Silva",
                "joao@example.com",
                "11999999999",
                organizacaoId,
                "Pastoral da Música",
                "MEMBRO",
                LocalDate.now(),
                true,
                null
        );
    }

    @Test
    @DisplayName("Deve criar um membro com sucesso")
    void deveCriarMembro() {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(usuarioId);
        user.setNome("João da Silva");

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(user));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));
        when(repository.findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(usuarioId,
                organizacaoId)).thenReturn(Optional.empty());

        when(repository.save(any(MembroEntity.class))).thenReturn(entity);
        when(mapper.toResponse(any(MembroEntity.class))).thenReturn(response);

        MembroResponse result = createUseCase.execute(request, "admin");

        assertNotNull(result);
        assertEquals(usuarioId, result.usuarioId());
        verify(repository).save(any(MembroEntity.class));
        verify(auditPublisher).publish(eq("admin"), eq("create"), anyString(), eq("success"));
    }

    @Test
    @DisplayName("Deve atualizar um membro com sucesso")
    void deveAtualizarMembro() {
        UUID id = entity.getId();
        UsuarioEntity user = new UsuarioEntity();
        user.setId(usuarioId);
        user.setNome("João da Silva");

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(user));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));
        when(repository.findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(usuarioId,
                organizacaoId)).thenReturn(Optional.empty());

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
