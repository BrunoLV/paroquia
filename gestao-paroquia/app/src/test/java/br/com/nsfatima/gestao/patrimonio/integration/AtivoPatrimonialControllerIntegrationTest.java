package br.com.nsfatima.gestao.patrimonio.integration;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.Year;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AtivoPatrimonialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Autowired
    private LocalJpaRepository localRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    @Autowired
    private br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository
            alocacaoRepository;

    private final UUID grupoParoquialId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        alocacaoRepository.deleteAll();
        ativoRepository.deleteAll();
    }

    @Test
    void deveCriarAtivoComTombamentoAutogeradoEStatusDisponivel() throws Exception {
        garantirGrupoParoquialExiste(grupoParoquialId);

        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "grupoParoquialId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(grupoParoquialId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.numeroTombamento")
                        .value(org.hamcrest.Matchers.startsWith("PAT-" + Year.now().getValue())));
    }

    @Test
    void deveRejeitarCriacaoParaGrupoParoquialDoTipoPastoral() throws Exception {
        GrupoParoquialEntity pastoral = new GrupoParoquialEntity();
        pastoral.setId(grupoParoquialId);
        pastoral.setNome("Pastoral Teste " + grupoParoquialId);
        pastoral.setTipo(TipoGrupo.PASTORAL);
        pastoral.setAtivo(true);
        grupoParoquialRepository.save(pastoral);

        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "grupoParoquialId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(grupoParoquialId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNegarCriacaoParaCoordenadorDeOutraGrupoParoquial() throws Exception {
        UUID outraGrupoParoquialId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "grupoParoquialId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(outraGrupoParoquialId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTransferenciaDeAtivoAlocado() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.ALOCADO, grupoParoquialId);
        UUID novaGrupoParoquialId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/ativos/" + ativo.getId() + "/transferir")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "novaGrupoParoquialId": "%s",
                                  "justificativa": "Necessidade de reorganização do inventário"
                                }
                                """.formatted(novaGrupoParoquialId)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveAlocarAtivoDisponivelParaEventoDaMesmaGrupoParoquial() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, grupoParoquialId);
        LocalEntity local = criarLocal(grupoParoquialId);
        EventoEntity evento = criarEvento(local.getId());

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventoId": "%s",
                                  "justificativaAlocacao": "Utilização para exibição de slides de formação",
                                  "dataInicio": "2026-09-01",
                                  "dataFim": "2026-09-03"
                                }
                                """.formatted(evento.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALOCADO"));
    }

    @Test
    void deveReservarAtivoSemEventoInformandoApenasJustificativa() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, grupoParoquialId);

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "justificativaAlocacao": "Reserva para uso futuro sem evento definido",
                                  "dataInicio": "2026-09-01",
                                  "dataFim": "2026-09-03"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALOCADO"));
    }

    @Test
    void deveCriarPendenciaDeAprovacaoParaEventoDeOutraGrupoParoquial() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, grupoParoquialId);
        LocalEntity local = criarLocal(UUID.randomUUID());
        EventoEntity evento = criarEvento(local.getId());

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventoId": "%s",
                                  "justificativaAlocacao": "Utilização para exibição de slides de formação",
                                  "dataInicio": "2026-09-01",
                                  "dataFim": "2026-09-03"
                                }
                                """.formatted(evento.getId())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveDesalocarAtivoAlocado() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.ALOCADO, grupoParoquialId);
        criarOcupacaoConfirmada(ativo.getId(), grupoParoquialId);

        mockMvc.perform(patch("/api/v1/ativos/" + ativo.getId() + "/desalocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));
    }

    @Test
    void deveListarAtivosPorGrupoParoquial() throws Exception {
        criarAtivo(AtivoStatus.DISPONIVEL, grupoParoquialId);
        criarAtivo(AtivoStatus.DISPONIVEL, UUID.randomUUID());

        mockMvc.perform(get("/api/v1/ativos/grupoParoquial/" + grupoParoquialId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private AtivoPatrimonialEntity criarAtivo(AtivoStatus status, UUID grupoParoquialId) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(UUID.randomUUID());
        ativo.setNome("Projetor");
        ativo.setCategoria(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria.ELETRONICO);
        ativo.setStatus(status);
        ativo.setGrupoParoquialId(grupoParoquialId);
        ativo.setNumeroTombamento("PAT-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8));
        ativo.setCriadoEmUtc(Instant.now());
        return ativoRepository.save(ativo);
    }

    private void criarOcupacaoConfirmada(UUID ativoId, UUID grupoParoquialId) {
        br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity ocupacao =
                new br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setGrupoParoquialOrigemId(grupoParoquialId);
        ocupacao.setDataInicio(java.time.LocalDate.now().minusDays(1));
        ocupacao.setDataFim(java.time.LocalDate.now());
        ocupacao.setStatus(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus.CONFIRMADA);
        ocupacao.setCriadoEmUtc(Instant.now());
        alocacaoRepository.save(ocupacao);
    }

    private LocalEntity criarLocal(UUID grupoParoquialId) {
        garantirGrupoParoquialExiste(grupoParoquialId);
        LocalEntity local = new LocalEntity();
        local.setId(UUID.randomUUID());
        local.setNome("Salão " + UUID.randomUUID());
        local.setTipo("SALAO");
        local.setGrupoParoquialId(grupoParoquialId);
        return localRepository.save(local);
    }

    private void garantirGrupoParoquialExiste(UUID grupoParoquialId) {
        // Ativos patrimoniais só podem pertencer a uma UnidadeTerritorial MATRIZ/COMUNIDADE, track
        // linguagem_ubiqua_dominio (ver AtivoGrupoParoquialPolicy)
        if (unidadeTerritorialRepository.existsById(grupoParoquialId)) {
            return;
        }
        UnidadeTerritorialEntity unidade = new UnidadeTerritorialEntity();
        unidade.setId(grupoParoquialId);
        unidade.setNome("Unidade " + grupoParoquialId);
        unidade.setTipo(TipoUnidadeTerritorial.COMUNIDADE);
        unidade.setAtivo(true);
        unidadeTerritorialRepository.save(unidade);
    }

    private EventoEntity criarEvento(UUID localId) {
        garantirGrupoParoquialExiste(grupoParoquialId);
        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Encontro de Catequistas");
        evento.setGrupoParoquialResponsavelId(grupoParoquialId);
        evento.setInicioUtc(Instant.now());
        evento.setFimUtc(Instant.now().plusSeconds(3600));
        evento.setStatus("CONFIRMADO");
        evento.setLocalId(localId);
        return eventoRepository.save(evento);
    }
}
