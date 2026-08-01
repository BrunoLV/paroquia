package br.com.nsfatima.gestao.patrimonio.integration;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
    private OrganizacaoJpaRepository organizacaoRepository;

    @Autowired
    private br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository
            alocacaoRepository;

    private final UUID organizacaoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        alocacaoRepository.deleteAll();
        ativoRepository.deleteAll();
    }

    @Test
    void deveCriarAtivoComTombamentoAutogeradoEStatusDisponivel() throws Exception {
        garantirOrganizacaoExiste(organizacaoId);

        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "organizacaoId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(organizacaoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.numeroTombamento")
                        .value(org.hamcrest.Matchers.startsWith("PAT-" + Year.now().getValue())));
    }

    @Test
    void deveRejeitarCriacaoParaOrganizacaoDoTipoPastoral() throws Exception {
        OrganizacaoEntity pastoral = new OrganizacaoEntity();
        pastoral.setId(organizacaoId);
        pastoral.setNome("Pastoral Teste " + organizacaoId);
        pastoral.setTipo(TipoOrganizacao.PASTORAL);
        pastoral.setAtivo(true);
        organizacaoRepository.save(pastoral);

        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "organizacaoId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(organizacaoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveNegarCriacaoParaCoordenadorDeOutraOrganizacao() throws Exception {
        UUID outraOrganizacaoId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/ativos")
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "TV 55 polegadas",
                                  "categoria": "ELETRONICO",
                                  "organizacaoId": "%s",
                                  "reservavel": true,
                                  "dataAquisicao": "2024-01-10",
                                  "valorEstimado": 3500.00
                                }
                                """.formatted(outraOrganizacaoId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearTransferenciaDeAtivoAlocado() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.ALOCADO, organizacaoId);
        UUID novaOrganizacaoId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/ativos/" + ativo.getId() + "/transferir")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "novaOrganizacaoId": "%s",
                                  "justificativa": "Necessidade de reorganização do inventário"
                                }
                                """.formatted(novaOrganizacaoId)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveAlocarAtivoDisponivelParaEventoDaMesmaOrganizacao() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        LocalEntity local = criarLocal(organizacaoId);
        EventoEntity evento = criarEvento(local.getId());

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
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
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
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
    void deveCriarPendenciaDeAprovacaoParaEventoDeOutraOrganizacao() throws Exception {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        LocalEntity local = criarLocal(UUID.randomUUID());
        EventoEntity evento = criarEvento(local.getId());

        mockMvc.perform(post("/api/v1/ativos/" + ativo.getId() + "/alocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
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
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.ALOCADO, organizacaoId);
        criarOcupacaoConfirmada(ativo.getId(), organizacaoId);

        mockMvc.perform(patch("/api/v1/ativos/" + ativo.getId() + "/desalocar")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));
    }

    @Test
    void deveListarAtivosPorOrganizacao() throws Exception {
        criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        criarAtivo(AtivoStatus.DISPONIVEL, UUID.randomUUID());

        mockMvc.perform(get("/api/v1/ativos/organizacao/" + organizacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private AtivoPatrimonialEntity criarAtivo(AtivoStatus status, UUID organizacaoId) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(UUID.randomUUID());
        ativo.setNome("Projetor");
        ativo.setCategoria(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria.ELETRONICO);
        ativo.setStatus(status);
        ativo.setOrganizacaoId(organizacaoId);
        ativo.setNumeroTombamento("PAT-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8));
        ativo.setCriadoEmUtc(Instant.now());
        return ativoRepository.save(ativo);
    }

    private void criarOcupacaoConfirmada(UUID ativoId, UUID organizacaoId) {
        br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity ocupacao =
                new br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setOrganizacaoOrigemId(organizacaoId);
        ocupacao.setDataInicio(java.time.LocalDate.now().minusDays(1));
        ocupacao.setDataFim(java.time.LocalDate.now());
        ocupacao.setStatus(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus.CONFIRMADA);
        ocupacao.setCriadoEmUtc(Instant.now());
        alocacaoRepository.save(ocupacao);
    }

    private LocalEntity criarLocal(UUID organizacaoId) {
        garantirOrganizacaoExiste(organizacaoId);
        LocalEntity local = new LocalEntity();
        local.setId(UUID.randomUUID());
        local.setNome("Salão " + UUID.randomUUID());
        local.setTipo("SALAO");
        local.setOrganizacaoId(organizacaoId);
        return localRepository.save(local);
    }

    private void garantirOrganizacaoExiste(UUID organizacaoId) {
        if (organizacaoRepository.existsById(organizacaoId)) {
            return;
        }
        OrganizacaoEntity organizacao = new OrganizacaoEntity();
        organizacao.setId(organizacaoId);
        organizacao.setNome("Organização " + organizacaoId);
        organizacao.setTipo(TipoOrganizacao.COMUNIDADE);
        organizacao.setAtivo(true);
        organizacaoRepository.save(organizacao);
    }

    private EventoEntity criarEvento(UUID localId) {
        garantirOrganizacaoExiste(organizacaoId);
        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Encontro de Catequistas");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.now());
        evento.setFimUtc(Instant.now().plusSeconds(3600));
        evento.setStatus("CONFIRMADO");
        evento.setLocalId(localId);
        return eventoRepository.save(evento);
    }
}
