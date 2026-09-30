package br.com.nsfatima.gestao.sdd;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CriarFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.CriarFielUseCase;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListarMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
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
    private CriarFielUseCase createFielUseCase;

    @Autowired
    private AdicionarMembroUseCase adicionarMembroUseCase;

    @Autowired
    private ListarMembrosUseCase listMembrosUseCase;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

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

        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(UUID.randomUUID());
        matriz.setNome("Matriz de Teste " + UUID.randomUUID());
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        unidadeTerritorialRepository.saveAndFlush(matriz);

        // 2. Cadastrar 2 Fiéis leigos
        CriarFielRequest reqCoord = new CriarFielRequest(
                "Coordenador da Pastoral " + UUID.randomUUID(),
                Sexo.MASCULINO,
                LocalDate.of(1980, 1, 1),
                "BRA", "52998224725", null, null, null,
                "coord@paroquia.org",
                "11977776666", true, null,
                new EnderecoDto("01000-000", "Rua B", "200", null, null, null, null),
                matriz.getId(), false, false
        );
        FielResponse fielCoord = createFielUseCase.execute(reqCoord, "test-actor");

        CriarFielRequest reqMembro = new CriarFielRequest(
                "Membro da Pastoral " + UUID.randomUUID(),
                Sexo.FEMININO,
                LocalDate.of(1992, 3, 15),
                "BRA", "11144477735", null, null, null,
                "membro@paroquia.org",
                "11966665555", true, null,
                new EnderecoDto("01000-000", "Rua C", "300", null, null, null, null),
                matriz.getId(), false, false
        );
        FielResponse fielMembro = createFielUseCase.execute(reqMembro, "test-actor");

        // 3. Vincular como Coordenador Pastoral e Membro comum
        adicionarMembroUseCase.execute(new MembroRequest(
                fielCoord.id(),
                org.getId(),
                "COORDENADOR",
                LocalDate.now(),
                null,
                true
        ), "test-actor");

        adicionarMembroUseCase.execute(new MembroRequest(
                fielMembro.id(),
                org.getId(),
                "MEMBRO_ATIVO",
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

        assertThat(coordMembro.papel()).isEqualTo("coordenador");

        MembroResponse comumMembro = membros.stream()
                .filter(m -> m.fielId().equals(fielMembro.id()))
                .findFirst()
                .orElseThrow();

        assertThat(comumMembro.papel()).isEqualTo("membro-ativo");
    }
}
