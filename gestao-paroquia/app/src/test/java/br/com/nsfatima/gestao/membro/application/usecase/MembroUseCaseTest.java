package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    private MembroGrupoJpaRepository repository;

    @Mock
    private OrganizacaoJpaRepository organizacaoRepository;

    @Mock
    private FielJpaRepository fielRepository;

    @Mock
    private MembroMapper mapper;

    @Mock
    private MembroAuditPublisher auditPublisher;

    @InjectMocks
    private AdicionarMembroUseCase adicionarUseCase;

    @InjectMocks
    private UpdateMembroUseCase updateUseCase;

    @InjectMocks
    private GetMembroUseCase getUseCase;

    private MembroRequest request;
    private MembroGrupoEntity entity;
    private MembroResponse response;
    private UUID fielId;
    private UUID organizacaoId;

    @BeforeEach
    void setUp() {
        fielId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        request = new MembroRequest(
                fielId,
                organizacaoId,
                "MEMBRO_PASTORAL",
                LocalDate.now(),
                null,
                true
        );

        entity = new MembroGrupoEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        entity.setPapel("MEMBRO_PASTORAL");

        response = new MembroResponse(
                entity.getId(),
                fielId,
                "João da Silva",
                "joao@example.com",
                "11999999999",
                organizacaoId,
                "Pastoral da Música",
                "MEMBRO_PASTORAL",
                LocalDate.now(),
                true,
                null
        );
    }

    @Test
    @DisplayName("Deve criar um membro com sucesso")
    void deveCriarMembro() {
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("João da Silva");

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);
        org.setTipo(TipoOrganizacao.PASTORAL);

        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));
        when(repository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId,
                organizacaoId)).thenReturn(Optional.empty());

        when(repository.save(any(MembroGrupoEntity.class))).thenReturn(entity);
        when(mapper.toResponse(any(MembroGrupoEntity.class))).thenReturn(response);

        MembroResponse result = adicionarUseCase.execute(request, "admin");

        assertNotNull(result);
        assertEquals(fielId, result.fielId());
        verify(repository).save(any(MembroGrupoEntity.class));
        verify(auditPublisher).publish(eq("admin"), eq("criar"), anyString(), eq("sucesso"));
    }

    @Test
    @DisplayName("Deve atualizar um membro com sucesso")
    void deveAtualizarMembro() {
        UUID id = entity.getId();
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("João da Silva");

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);
        org.setTipo(TipoOrganizacao.PASTORAL);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));
        when(repository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId,
                organizacaoId)).thenReturn(Optional.empty());

        when(repository.save(any(MembroGrupoEntity.class))).thenReturn(entity);
        when(mapper.toResponse(any(MembroGrupoEntity.class))).thenReturn(response);

        MembroResponse result = updateUseCase.execute(id, request, "admin");

        assertNotNull(result);
        verify(repository).save(entity);
        verify(auditPublisher).publish(eq("admin"), eq("atualizar"), eq(id.toString()), eq("sucesso"));
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
        verify(auditPublisher).publish(eq("admin"), eq("desativar"), eq(id.toString()), eq("sucesso"));
    }

    @Test
    @DisplayName("Nunca depende de nada do pacote iam/usuario (AC2)")
    void nuncaDependeDeUsuario() {
        // AdicionarMembroUseCase deve permanecer capaz de operar sem nunca ler/criar/alterar UsuarioEntity —
        // fixa esse invariante para que um futuro refactor não reintroduza silenciosamente essa dependência.
        var tiposDosCampos = java.util.Arrays.stream(AdicionarMembroUseCase.class.getDeclaredFields())
                .map(f -> f.getType().getName())
                .toList();

        assertThat(tiposDosCampos).noneMatch(tipo -> tipo.contains(".iam."));
    }
}
