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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

        assertThat(resp.protocolo()).matches("DPO-\\d{8}-[A-Z2-9]{12}");
        assertThat(resp.titularNome()).isEqualTo("L*** F***");
        assertThat(resp.titularEmail()).isEqualTo("l***s@exemplo.com");
        assertThat(resp.titularCpf()).isEqualTo("***.***.***-00");
        assertThat(resp.prazoVencido()).isFalse();
        assertThat(resp.diasRestantesPrazo()).isBetween(14L, 15L);
        assertThat(resp.status()).isEqualTo(StatusSolicitacaoDpo.RECEBIDA);
        assertThat(resp.prazoRespostaUtc()).isAfter(Instant.now());

        ArgumentCaptor<SolicitacaoDpoEntity> captor = ArgumentCaptor.forClass(SolicitacaoDpoEntity.class);
        verify(repository).save(captor.capture());
        SolicitacaoDpoEntity salvo = captor.getValue();
        assertThat(salvo.getProtocolo()).isEqualTo(resp.protocolo());
        assertThat(salvo.getTitularCpf()).isEqualTo("12345678900");
    }

    @Test
    @DisplayName("Consulta publica por protocolo mascara CPF, e-mail e nome; a visao do DPO traz o completo")
    void consultaPublicaMascaraDadosPessoais() {
        SolicitacaoDpoEntity entity = solicitacaoEm(StatusSolicitacaoDpo.RECEBIDA);
        when(repository.findByProtocolo("DPO-1")).thenReturn(Optional.of(entity));
        when(repository.findAllByOrderByCriadoEmUtcDesc()).thenReturn(List.of(entity));

        SolicitacaoDpoResponse publica = useCase.obterPorProtocolo("DPO-1");
        SolicitacaoDpoResponse completa = useCase.listarTodas().getFirst();

        assertThat(publica.titularCpf()).isEqualTo("***.***.***-25").doesNotContain("529");
        assertThat(publica.titularEmail()).isEqualTo("m***a@exemplo.com");
        assertThat(publica.titularNome()).isEqualTo("M*** S***");
        assertThat(completa.titularCpf()).isEqualTo("52998224725");
        assertThat(completa.titularEmail()).isEqualTo("maria@exemplo.com");
    }

    @Test
    @DisplayName("Solicitacao concluida nao pode ser reaberta nem respondida novamente")
    void concluidaNaoReabre() {
        UUID id = UUID.randomUUID();
        SolicitacaoDpoEntity entity = solicitacaoEm(StatusSolicitacaoDpo.ATENDIDA);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> useCase.responder(id, new ResponderSolicitacaoDpoRequest(
                "Reabrindo", StatusSolicitacaoDpo.EM_ANALISE)))
                .isInstanceOf(IllegalStateException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("RECEBIDA nao e destino valido de uma resposta; EM_ANALISE nao registra data de conclusao")
    void recebidaNaoEDestinoEAnaliseNaoConclui() {
        UUID id = UUID.randomUUID();
        SolicitacaoDpoEntity entity = solicitacaoEm(StatusSolicitacaoDpo.RECEBIDA);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> useCase.responder(id, new ResponderSolicitacaoDpoRequest(
                "x", StatusSolicitacaoDpo.RECEBIDA))).isInstanceOf(IllegalStateException.class);

        when(repository.save(any(SolicitacaoDpoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        SolicitacaoDpoResponse resp = useCase.responder(id, new ResponderSolicitacaoDpoRequest(
                "Em analise", StatusSolicitacaoDpo.EM_ANALISE));

        assertThat(resp.status()).isEqualTo(StatusSolicitacaoDpo.EM_ANALISE);
        assertThat(resp.respondidoEmUtc()).isNull();
    }

    @Test
    @DisplayName("Solicitacao em aberto apos o prazo de 15 dias aparece como vencida")
    void prazoVencidoSinalizado() {
        SolicitacaoDpoEntity entity = solicitacaoEm(StatusSolicitacaoDpo.EM_ANALISE);
        entity.setPrazoRespostaUtc(Instant.now().minus(2, ChronoUnit.DAYS));
        when(repository.findAllByOrderByCriadoEmUtcDesc()).thenReturn(List.of(entity));

        SolicitacaoDpoResponse resp = useCase.listarTodas().getFirst();

        assertThat(resp.prazoVencido()).isTrue();
        assertThat(resp.diasRestantesPrazo()).isNegative();
    }

    private SolicitacaoDpoEntity solicitacaoEm(StatusSolicitacaoDpo status) {
        SolicitacaoDpoEntity entity = new SolicitacaoDpoEntity();
        entity.setId(UUID.randomUUID());
        entity.setProtocolo("DPO-1");
        entity.setTitularNome("Maria Silva");
        entity.setTitularEmail("maria@exemplo.com");
        entity.setTitularCpf("52998224725");
        entity.setStatus(status);
        entity.setPrazoRespostaUtc(Instant.now().plus(10, ChronoUnit.DAYS));
        return entity;
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
