package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaRequestDTO;
import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaResponseDTO;
import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.SolenidadeLiturgicaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class SolenidadeLiturgicaServiceTest {

    @Mock
    private SolenidadeLiturgicaJpaRepository repository;

    private SolenidadeLiturgicaService service;

    @BeforeEach
    void setUp() {
        service = new SolenidadeLiturgicaService(repository);
    }

    @Test
    @DisplayName("Deve listar todas as solenidades cadastradas no banco")
    void testShouldListAllSolenidades() {
        SolenidadeLiturgicaEntity e = new SolenidadeLiturgicaEntity();
        e.setId(UUID.randomUUID());
        e.setChave("MAE_DE_DEUS");
        e.setNome("Santa Maria, Mãe de Deus");
        e.setTipoSolenidade(TipoSolenidade.FIXA);
        e.setMesFixo(1);
        e.setDiaFixo(1);
        e.setCorLiturgica("BRANCO");

        when(repository.findAll()).thenReturn(List.of(e));

        List<SolenidadeLiturgicaResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).chave()).isEqualTo("MAE_DE_DEUS");
    }

    @Test
    @DisplayName("Deve permitir cadastrar nova solenidade")
    void testShouldCreateSolenidadeWhenParoco() {
        SolenidadeLiturgicaRequestDTO req = new SolenidadeLiturgicaRequestDTO(
                "SAO_PADRE_PIO",
                "São Padre Pio",
                TipoSolenidade.FIXA,
                9,
                23,
                null,
                "BRANCO",
                2.5,
                2,
                "Solenidade de São Padre Pio",
                true
        );

        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SolenidadeLiturgicaResponseDTO created = service.create(req);

        assertThat(created).isNotNull();
        assertThat(created.chave()).isEqualTo("SAO_PADRE_PIO");
        verify(repository).save(any(SolenidadeLiturgicaEntity.class));
    }
}
