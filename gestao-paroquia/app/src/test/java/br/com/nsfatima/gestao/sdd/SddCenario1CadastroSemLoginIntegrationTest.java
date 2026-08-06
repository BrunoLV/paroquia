package br.com.nsfatima.gestao.sdd;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.CreateFielUseCase;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SddCenario1CadastroSemLoginIntegrationTest {

    @Autowired
    private CreateFielUseCase createFielUseCase;

    @Autowired
    private AdicionarMembroUseCase adicionarMembroUseCase;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Test
    @DisplayName("Cenário 1 BDD: Fiel é cadastrado e vinculado a GrupoParoquial sem criação de conta de usuário (IAM)")
    void deveCadastrarFielEAdicionarComoMembroSemCriarUsuario() {
        long initialUserCount = usuarioJpaRepository.count();

        // 1. Cadastra Fiel sem conta de usuário
        CreateFielRequest createRequest = new CreateFielRequest(
                "João Sem Login " + UUID.randomUUID(),
                "987.654.321-00",
                LocalDate.of(1985, 5, 20),
                "joao.sem.login@paroquia.org",
                "(11) 98888-7777",
                "Rua das Flores, 123",
                Vocacao.LEIGO,
                true,
                true,
                false,
                false
        );

        FielResponse fiel = createFielUseCase.execute(createRequest, "test-actor");

        assertThat(fiel).isNotNull();
        assertThat(fiel.id()).isNotNull();

        // 2. Cria e salva um GrupoParoquial de teste
        GrupoParoquialEntity org = new GrupoParoquialEntity();
        org.setId(UUID.randomUUID());
        org.setNome("Pastoral de Teste " + UUID.randomUUID());
        org.setTipo(TipoGrupo.PASTORAL);
        org.setAtivo(true);
        grupoParoquialRepository.saveAndFlush(org);

        // 3. Adiciona o Fiel como membro comum no GrupoParoquial
        MembroRequest request = new MembroRequest(
                fiel.id(),
                org.getId(),
                "MEMBRO_PASTORAL",
                LocalDate.now(),
                null,
                true
        );

        MembroResponse membro = adicionarMembroUseCase.execute(request, "test-actor");

        assertThat(membro).isNotNull();
        assertThat(membro.fielId()).isEqualTo(fiel.id());
        assertThat(membro.grupoParoquialId()).isEqualTo(org.getId());

        // 4. Verifica explicitamente que nenhuma conta foi criada na tabela de usuarios
        long finalUserCount = usuarioJpaRepository.count();
        assertThat(finalUserCount).isEqualTo(initialUserCount);
    }
}
