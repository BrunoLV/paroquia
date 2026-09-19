package br.com.nsfatima.gestao.clero.api.v1.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EnderecoClerigoDto;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarAjusteClericalUseCase;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarClerigoUseCase;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarRelacionamentoFamiliarUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClerigoControllerTest {

    @Mock private GerenciarClerigoUseCase useCase;
    @Mock private GerenciarAjusteClericalUseCase ajusteUseCase;
    @Mock private GerenciarRelacionamentoFamiliarUseCase familiaUseCase;
    @Mock private EventoActorContextResolver actor;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final EnderecoClerigoDto enderecoPadrao =
            new EnderecoClerigoDto("08000-000", "Rua 1", "10", null, "Centro", "São Paulo", "SP");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        ClerigoController controller = new ClerigoController(useCase, ajusteUseCase, familiaUseCase, actor);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveListarClerigos() throws Exception {
        when(useCase.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/clerigos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarDiretorioInstitucional() throws Exception {
        when(useCase.diretorio()).thenReturn(List.of(new ClerigoInstitucionalResponse(
                UUID.randomUUID(), "Padre João", GrauOrdem.PADRE_DIOCESANO, FuncaoEclesiastica.PAROCO, "Paróquia")));
        mockMvc.perform(get("/api/v1/clerigos/institucional"))
                .andExpect(status().isOk());
    }

    @Test
    void deveObterClerigoPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.obter(id)).thenReturn(new ClerigoResponse(
                id, "João", "Padre João", LocalDate.of(1980, 1, 1), "BRA",
                "123.456.789-01", null, null, "padre@test.com", "(11) 99999-8888",
                true, null, enderecoPadrao, GrauOrdem.PADRE_DIOCESANO, "Diocese",
                LocalDate.of(2005, 1, 1), LocalDate.of(2006, 1, 1), null,
                FuncaoEclesiastica.PAROCO, true));

        mockMvc.perform(get("/api/v1/clerigos/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void deveCriarClerigo() throws Exception {
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("admin", "ROLE_ADMIN", "PAROQUIA", null, userId, null));

        ClerigoRequest request = new ClerigoRequest(
                "João da Silva",
                "Padre João",
                LocalDate.of(1980, 1, 1),
                "BRA",
                "12345678901",
                null,
                null,
                "padre@test.com",
                "11999998888",
                true,
                null,
                enderecoPadrao,
                GrauOrdem.PADRE_DIOCESANO,
                "Diocese",
                LocalDate.of(2005, 1, 1),
                LocalDate.of(2006, 1, 1),
                null,
                FuncaoEclesiastica.PAROCO);

        mockMvc.perform(post("/api/v1/clerigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveInativarClerigo() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("admin", "ROLE_ADMIN", "PAROQUIA", null, userId, null));

        mockMvc.perform(delete("/api/v1/clerigos/" + id))
                .andExpect(status().isNoContent());

        verify(useCase).inativar(id, "admin", userId);
    }

    @Test
    void deveListarHistoricoEncargos() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.listarEncargos(id)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/clerigos/" + id + "/historico-encargos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarSolicitacoesAjuste() throws Exception {
        when(ajusteUseCase.listarPendentes()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/clerigos/solicitacoes-ajuste"))
                .andExpect(status().isOk());
    }

    @Test
    void deveObterPerfilProprio() throws Exception {
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("padre", "ROLE_CLERIGO", "PAROQUIA", null, userId, null));
        when(useCase.obterPorUsuario(userId)).thenReturn(new ClerigoResponse(
                UUID.randomUUID(), "João", "Padre João", LocalDate.of(1980, 1, 1), "BRA",
                "123.456.789-01", null, null, "padre@test.com", "(11) 99999-8888",
                true, null, enderecoPadrao, GrauOrdem.PADRE_DIOCESANO, "Diocese",
                LocalDate.of(2005, 1, 1), LocalDate.of(2006, 1, 1), null,
                FuncaoEclesiastica.PAROCO, true));

        mockMvc.perform(get("/api/v1/clerigos/meu-perfil"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarClerigo() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("admin", "ROLE_ADMIN", "PAROQUIA", null, userId, null));

        ClerigoRequest request = new ClerigoRequest(
                "João da Silva",
                "Padre João",
                LocalDate.of(1980, 1, 1),
                "BRA",
                "12345678901",
                null,
                null,
                "padre@test.com",
                "11999998888",
                true,
                null,
                enderecoPadrao,
                GrauOrdem.PADRE_DIOCESANO,
                "Diocese",
                LocalDate.of(2005, 1, 1),
                LocalDate.of(2006, 1, 1),
                null,
                FuncaoEclesiastica.PAROCO);

        mockMvc.perform(put("/api/v1/clerigos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveSolicitarAjuste() throws Exception {
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("padre", "ROLE_CLERIGO", "PAROQUIA", null, userId, null));

        mockMvc.perform(post("/api/v1/clerigos/solicitacoes-ajuste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"novo@test.com\",\"celular\":\"11999998888\","
                                + "\"endereco\":\"Casa Paroquial\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void deveDecidirAjuste() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("admin", "ROLE_ADMIN", "PAROQUIA", null, userId, null));

        mockMvc.perform(post("/api/v1/clerigos/solicitacoes-ajuste/" + id + "/decisao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovar\":true}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveGerenciarFamiliaresDoClerigo() throws Exception {
        UUID clerigoId = UUID.randomUUID();
        UUID relacaoId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(actor.resolveRequired()).thenReturn(
                new EventoActorContext("admin", "ROLE_ADMIN", "PAROQUIA", null, userId, null));

        mockMvc.perform(get("/api/v1/clerigos/" + clerigoId + "/familiares"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/clerigos/" + clerigoId + "/familiares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pessoaDestinoId\":\"" + UUID.randomUUID() + "\",\"tipo\":\"GENITOR\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/clerigos/" + clerigoId + "/familiares/" + relacaoId))
                .andExpect(status().isNoContent());
    }
}
