package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.application.usecase.observacao.RegisterSystemObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SystemObservacaoFallbackAuthorIntegrationTest {

    @Autowired
    private RegisterSystemObservacaoUseCase registerSystemObservacaoUseCase;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldUseTechnicalFallbackWhenSystemFlowHasNoHumanActor() {
        UUID eventoId = UUID.randomUUID();

        registerSystemObservacaoUseCase.execute(
                eventoId,
                TipoObservacaoInput.CANCELAMENTO,
                "Cancelamento automático",
                null,
                "system",
                "cancelamento");

        assertThat(observacaoEventoJpaRepository.findByEventoId(eventoId))
                .singleElement()
                .extracting(obs -> obs.getUsuarioId().toString())
                .isEqualTo("00000000-0000-0000-0000-000000000001");
    }
}
