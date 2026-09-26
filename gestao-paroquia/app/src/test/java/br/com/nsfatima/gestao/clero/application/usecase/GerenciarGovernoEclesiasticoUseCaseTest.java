package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoResponse;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.DioceseEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.GovernoEclesiasticoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.DioceseJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.GovernoEclesiasticoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Caso de Uso: GerenciarGovernoEclesiasticoUseCase")
class GerenciarGovernoEclesiasticoUseCaseTest {

    @Mock
    private GovernoEclesiasticoJpaRepository repository;

    @Mock
    private DioceseJpaRepository dioceseRepository;

    @Mock
    private ClerigoJpaRepository clerigoRepository;

    @Mock
    private AuditoriaPersistenceService auditLogService;

    private GerenciarGovernoEclesiasticoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarGovernoEclesiasticoUseCase(repository, dioceseRepository, clerigoRepository, auditLogService);
    }

    @Test
    @DisplayName("Deve salvar governo eclesiástico com sucesso")
    void deveSalvarGovernoEclesiastico() {
        UUID dioceseId = UUID.randomUUID();
        UUID parocoId = UUID.randomUUID();

        DioceseEntity diocese = new DioceseEntity();
        diocese.setId(dioceseId);
        diocese.setNome("Diocese de Santos");
        diocese.setBispoDiocesano("Dom Tarcísio");

        when(dioceseRepository.findById(dioceseId)).thenReturn(Optional.of(diocese));
        when(clerigoRepository.existsById(parocoId)).thenReturn(true);
        when(repository.findFirstByOrderByCriadoEmUtcDesc()).thenReturn(Optional.empty());
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setNomeEclesiastico("Pe. Pedro da Silva");
        when(clerigoRepository.findById(parocoId)).thenReturn(Optional.of(clerigo));

        GovernoEclesiasticoRequest request = new GovernoEclesiasticoRequest(
                dioceseId,
                parocoId,
                LocalDate.of(2022, 1, 1),
                "Decreto 01/2022",
                LocalDate.of(1950, 10, 12),
                "01/1950"
        );

        GovernoEclesiasticoResponse response = useCase.salvar(request);

        assertThat(response.dioceseId()).isEqualTo(dioceseId);
        assertThat(response.dioceseNome()).isEqualTo("Diocese de Santos");
        assertThat(response.bispoDiocesano()).isEqualTo("Dom Tarcísio");
        assertThat(response.parocoId()).isEqualTo(parocoId);
        assertThat(response.parocoNome()).isEqualTo("Pe. Pedro da Silva");
        assertThat(response.parocoNomeEclesiastico()).isEqualTo("Pe. Pedro da Silva");
        assertThat(response.decretoErecaoParoquia()).isEqualTo("Decreto 01/2022");

        verify(repository).save(any(GovernoEclesiasticoEntity.class));
    }

    @Test
    @DisplayName("Deve falhar ao vincular diocese inexistente")
    void deveFalharDioceseInexistente() {
        UUID dioceseId = UUID.randomUUID();
        when(dioceseRepository.findById(dioceseId)).thenReturn(Optional.empty());

        GovernoEclesiasticoRequest request = new GovernoEclesiasticoRequest(
                dioceseId, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.salvar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Diocese não encontrada");
    }
}
