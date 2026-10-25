package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.api.v1.dto.CriarSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ResponderSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitacaoDpoResponse;
import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.SolicitacaoDpoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.SolicitacaoDpoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarSolicitacoesDpoUseCaseTest {

    @Mock
    private SolicitacaoDpoJpaRepository repository;

    private GerenciarSolicitacoesDpoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarSolicitacoesDpoUseCase(repository);
    }

    @Test
    @DisplayName("Deve registrar solicitacao ao DPO com protocolo UUID e prazo legal de 15 dias")
    void deveRegistrarSolicitacaoAoDpoComPrazoDe15Dias() {
        CriarSolicitacaoDpoRequest req = new CriarSolicitacaoDpoRequest(
                "Lucas Ferreira",
                "lucas@exemplo.com",
                "12345678900",
                "CONFIRMACAO_TRATAMENTO",
                "Gostaria de confirmar se possuo dados cadastrados nesta paroquia."
        );

        when(repository.save(any(SolicitacaoDpoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SolicitacaoDpoResponse resp = useCase.criarSolicitacao(req);

        assertThat(resp.protocolo()).isNotBlank();
        assertThat(resp.titularNome()).isEqualTo("Lucas Ferreira");
        assertThat(resp.status()).isEqualTo(StatusSolicitacaoDpo.RECEBIDA);
        assertThat(resp.prazoRespostaUtc()).isAfter(Instant.now());

        ArgumentCaptor<SolicitacaoDpoEntity> captor = ArgumentCaptor.forClass(SolicitacaoDpoEntity.class);
        verify(repository).save(captor.capture());
        SolicitacaoDpoEntity salvo = captor.getValue();
        assertThat(salvo.getProtocolo()).isEqualTo(resp.protocolo());
    }

    @Test
    @DisplayName("Deve responder solicitacao com sucesso atualizando status e resposta")
    void deveResponderSolicitacaoComSucesso() {
        UUID id = UUID.randomUUID();
        SolicitacaoDpoEntity entity = new SolicitacaoDpoEntity();
        entity.setId(id);
        entity.setProtocolo(UUID.randomUUID().toString());
        entity.setStatus(StatusSolicitacaoDpo.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(SolicitacaoDpoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponderSolicitacaoDpoRequest req = new ResponderSolicitacaoDpoRequest(
                "Confirmamos o tratamento dos dados conforme registros pastorais.",
                StatusSolicitacaoDpo.ATENDIDA
        );

        SolicitacaoDpoResponse resp = useCase.responder(id, req);

        assertThat(resp.status()).isEqualTo(StatusSolicitacaoDpo.ATENDIDA);
        assertThat(resp.resposta()).contains("Confirmamos o tratamento");
        assertThat(resp.respondidoEmUtc()).isNotNull();
    }
}
