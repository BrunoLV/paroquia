package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseResponse;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.DioceseEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.DioceseJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Caso de Uso: GerenciarDioceseUseCase")
class GerenciarDioceseUseCaseTest {

    @Mock
    private DioceseJpaRepository repository;

    @Mock
    private AuditoriaPersistenceService auditLogService;

    private GerenciarDioceseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarDioceseUseCase(repository, auditLogService);
    }

    @Test
    @DisplayName("Deve criar diocese com sucesso e registrar auditoria")
    void deveCriarDioceseComSucesso() {
        DioceseRequest request = new DioceseRequest(
                "Diocese de Santos",
                "Dom Tarcísio",
                "12.345.678/0001-90",
                "Santos",
                "SP",
                "contato@santos.org",
                true
        );

        when(repository.findByNomeIgnoreCase("Diocese de Santos")).thenReturn(Optional.empty());

        DioceseResponse response = useCase.criar(request);

        assertThat(response.nome()).isEqualTo("Diocese de Santos");
        assertThat(response.bispoDiocesano()).isEqualTo("Dom Tarcísio");
        assertThat(response.cidade()).isEqualTo("Santos");
        assertThat(response.uf()).isEqualTo("SP");

        ArgumentCaptor<DioceseEntity> captor = ArgumentCaptor.forClass(DioceseEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Diocese de Santos");
    }

    @Test
    @DisplayName("Deve impedir criação de diocese com nome duplicado")
    void deveImpedirNomeDuplicado() {
        DioceseRequest request = new DioceseRequest(
                "Diocese de Santos",
                "Dom Tarcísio",
                null, null, null, null, true
        );

        DioceseEntity existente = new DioceseEntity();
        existente.setId(UUID.randomUUID());
        existente.setNome("Diocese de Santos");

        when(repository.findByNomeIgnoreCase("Diocese de Santos")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> useCase.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma diocese cadastrada com o nome");
    }

    @Test
    @DisplayName("Deve listar dioceses cadastradas")
    void deveListarDioceses() {
        DioceseEntity e = new DioceseEntity();
        e.setId(UUID.randomUUID());
        e.setNome("Diocese 1");
        e.setBispoDiocesano("Bispo 1");
        e.setAtivo(true);

        when(repository.findAll()).thenReturn(List.of(e));

        List<DioceseResponse> lista = useCase.listar();
        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().nome()).isEqualTo("Diocese 1");
    }
}
