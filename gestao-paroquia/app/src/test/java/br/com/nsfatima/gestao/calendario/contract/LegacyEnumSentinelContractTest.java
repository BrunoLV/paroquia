package br.com.nsfatima.gestao.calendario.contract;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LegacyEnumSentinelContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldProjectUnknownLegacyValueOnRead() throws Exception {
        ObservacaoEventoEntity legacyObservation = new ObservacaoEventoEntity();
        legacyObservation.setId(UUID.randomUUID());
        legacyObservation.setEventoId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        legacyObservation.setUsuarioId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        legacyObservation.setTipo("ANOTACAO_LEGADA");
        legacyObservation.setConteudo("Valor legado desconhecido");
        legacyObservation.setCriadoEmUtc(Instant.now());
        legacyObservation.setRemovida(false);
        observacaoEventoJpaRepository.save(legacyObservation);

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-0000000000aa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("UNKNOWN_LEGACY"));
    }
}
