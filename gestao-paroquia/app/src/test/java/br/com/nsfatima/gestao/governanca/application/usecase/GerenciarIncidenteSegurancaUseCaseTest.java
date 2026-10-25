package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.AtualizarStatusIncidenteRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.IncidenteSegurancaResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.RegistrarIncidenteRequest;
import br.com.nsfatima.gestao.governanca.domain.model.GravidadeIncidente;
import br.com.nsfatima.gestao.governanca.domain.model.StatusIncidente;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.IncidenteSegurancaEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.IncidenteSegurancaJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciarIncidenteSegurancaUseCase - Testes Unitários de Governança LGPD")
class GerenciarIncidenteSegurancaUseCaseTest {

    @Mock
    private IncidenteSegurancaJpaRepository repository;

    private GerenciarIncidenteSegurancaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarIncidenteSegurancaUseCase(repository);
    }

    @Test
    @DisplayName("Deve registrar novo incidente de segurança com status inicial EM_ANALISE")
    void deveRegistrarIncidenteComSucesso() {
        RegistrarIncidenteRequest request = new RegistrarIncidenteRequest(
                "Acesso não autorizado a prontuário",
                "Tentativa de exportação de dados pastorais sem credenciais",
                "Dados de fiéis e catequese",
                GravidadeIncidente.ALTA,
                Instant.now().minusSeconds(3600),
                Instant.now(),
                "Credenciais bloqueadas preventivamente"
        );

        when(repository.save(any(IncidenteSegurancaEntity.class))).thenAnswer(inv -> {
            IncidenteSegurancaEntity entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        IncidenteSegurancaResponse response = useCase.registrar(request);

        assertThat(response).isNotNull();
        assertThat(response.titulo()).isEqualTo(request.titulo());
        assertThat(response.status()).isEqualTo(StatusIncidente.EM_ANALISE);
        assertThat(response.categoriaGravidade()).isEqualTo(GravidadeIncidente.ALTA);
        assertThat(response.notificadoAnpd()).isFalse();

        ArgumentCaptor<IncidenteSegurancaEntity> captor = ArgumentCaptor.forClass(IncidenteSegurancaEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTitulo()).isEqualTo("Acesso não autorizado a prontuário");
    }

    @Test
    @DisplayName("Deve atualizar status e notificação à ANPD de incidente existente")
    void deveAtualizarStatusIncidenteExistente() {
        UUID id = UUID.randomUUID();
        IncidenteSegurancaEntity entity = new IncidenteSegurancaEntity();
        entity.setId(id);
        entity.setTitulo("Vazamento de planilha");
        entity.setDescricao("Arquivo compartilhado por engano");
        entity.setTipoDadoAfetado("Telefones de dizimistas");
        entity.setCategoriaGravidade(GravidadeIncidente.MEDIA);
        entity.setStatus(StatusIncidente.EM_ANALISE);
        entity.setDataOcorrencia(Instant.now().minusSeconds(7200));
        entity.setDataDeteccao(Instant.now().minusSeconds(3600));

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(IncidenteSegurancaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarStatusIncidenteRequest request = new AtualizarStatusIncidenteRequest(
                StatusIncidente.NOTIFICADO,
                true,
                Instant.now(),
                "ANPD-REQ-2026-987",
                true,
                Instant.now(),
                "Titulares comunicados por e-mail e canal oficial ANPD notificado"
        );

        IncidenteSegurancaResponse response = useCase.atualizarStatus(id, request);

        assertThat(response.status()).isEqualTo(StatusIncidente.NOTIFICADO);
        assertThat(response.notificadoAnpd()).isTrue();
        assertThat(response.protocoloAnpd()).isEqualTo("ANPD-REQ-2026-987");
        assertThat(response.notificadoTitulares()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar despachar incidente inexistente")
    void deveLancarExcecaoQuandoIncidenteNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        AtualizarStatusIncidenteRequest request = new AtualizarStatusIncidenteRequest(
                StatusIncidente.ENCERRADO,
                false,
                null,
                null,
                false,
                null,
                "Incidente não confirmado"
        );

        assertThatThrownBy(() -> useCase.atualizarStatus(id, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Deve listar todos os incidentes ordenados por detecção descendente")
    void deveListarIncidentesOrdenados() {
        IncidenteSegurancaEntity i1 = new IncidenteSegurancaEntity();
        i1.setId(UUID.randomUUID());
        i1.setTitulo("Incidente 1");
        i1.setDescricao("Desc");
        i1.setTipoDadoAfetado("Dados");
        i1.setCategoriaGravidade(GravidadeIncidente.BAIXA);
        i1.setStatus(StatusIncidente.ENCERRADO);
        i1.setDataOcorrencia(Instant.now());
        i1.setDataDeteccao(Instant.now());

        when(repository.findAllByOrderByDataDeteccaoDesc()).thenReturn(List.of(i1));

        List<IncidenteSegurancaResponse> lista = useCase.listarTodos();

        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().titulo()).isEqualTo("Incidente 1");
    }
}
