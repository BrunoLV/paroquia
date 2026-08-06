package br.com.nsfatima.gestao.sdd;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.CreateFielUseCase;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SddCenario2ConsultaFielUsuarioIntegrationTest {

    @Autowired
    private CreateFielUseCase createFielUseCase;

    @Autowired
    private AdicionarMembroUseCase adicionarMembroUseCase;

    @Autowired
    private ListMembrosUseCase listMembrosUseCase;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Test
    @DisplayName("Cenário 2 BDD: Consulta de membros de GrupoParoquial cruza fielId com papéis de Liderança")
    void deveListarMembrosCruzandoFielELideranca() {
        // 1. Criar e salvar GrupoParoquial de teste
        GrupoParoquialEntity org = new GrupoParoquialEntity();
        org.setId(UUID.randomUUID());
        org.setNome("Pastoral de Teste " + UUID.randomUUID());
        org.setTipo(TipoGrupo.PASTORAL);
        org.setAtivo(true);
        grupoParoquialRepository.saveAndFlush(org);

        // 2. Cadastrar 2 Fiéis
        CreateFielRequest reqCoord = new CreateFielRequest(
                "Coordenador da Pastoral " + UUID.randomUUID(),
                "111.222.333-44",
                LocalDate.of(1980, 1, 1),
                "coord@paroquia.org",
                "(11) 97777-6666",
                "Rua B, 200",
                Vocacao.LEIGO,
                true, true, true, true
        );
        FielResponse fielCoord = createFielUseCase.execute(reqCoord, "test-actor");

        CreateFielRequest reqMembro = new CreateFielRequest(
                "Membro da Pastoral " + UUID.randomUUID(),
                "555.666.777-88",
                LocalDate.of(1992, 3, 15),
                "membro@paroquia.org",
                "(11) 96666-5555",
                "Rua C, 300",
                Vocacao.LEIGO,
                true, true, false, false
        );
        FielResponse fielMembro = createFielUseCase.execute(reqMembro, "test-actor");

        // 3. Vincular como Coordenador Pastoral e Membro comum
        adicionarMembroUseCase.execute(new MembroRequest(
                fielCoord.id(),
                org.getId(),
                "COORD_PASTORAL",
                LocalDate.now(),
                null,
                true
        ), "test-actor");

        adicionarMembroUseCase.execute(new MembroRequest(
                fielMembro.id(),
                org.getId(),
                "MEMBRO_PASTORAL",
                LocalDate.now(),
                null,
                true
        ), "test-actor");

        // 4. Listar membros e verificar cruzamento correto
        Page<MembroResponse> page = listMembrosUseCase.execute(org.getId(), null, true, PageRequest.of(0, 100));
        List<MembroResponse> membros = page.getContent();

        assertThat(membros).isNotEmpty();

        MembroResponse coordMembro = membros.stream()
                .filter(m -> m.fielId().equals(fielCoord.id()))
                .findFirst()
                .orElseThrow();

        assertThat(coordMembro.papel()).isEqualTo("coord-pastoral");

        MembroResponse comumMembro = membros.stream()
                .filter(m -> m.fielId().equals(fielMembro.id()))
                .findFirst()
                .orElseThrow();

        assertThat(comumMembro.papel()).isEqualTo("membro-pastoral");
    }
}
