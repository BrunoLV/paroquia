package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.api.v1.dto.AtualizarProjetoRequest;
import br.com.nsfatima.gestao.projeto.application.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProvedorEventoProjeto;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarProjetoUseCaseTest {

    private static final Instant INICIO = Instant.parse("2026-03-01T03:00:00Z");
    private static final Instant FIM = Instant.parse("2026-09-01T03:00:00Z");
    private static final UUID ORG_ATUAL = UUID.randomUUID();
    private static final UUID ORG_DESTINO = UUID.randomUUID();

    private ProjetoEventoJpaRepository repository;
    private ProjetoAuthorizationService authorization;
    private ProvedorEventoProjeto eventos;
    private OrganizacaoJpaRepository organizacoes;
    private AtualizarProjetoUseCase useCase;
    private ProjetoEventoEntity projeto;
    private final EventoActorContext ator = new EventoActorContext("coord", "COORDENADOR", "PASTORAL", ORG_ATUAL,
            null, null);

    @BeforeEach
    void setUp() {
        repository = mock(ProjetoEventoJpaRepository.class);
        authorization = mock(ProjetoAuthorizationService.class);
        eventos = mock(ProvedorEventoProjeto.class);
        organizacoes = mock(OrganizacaoJpaRepository.class);
        EventoActorContextResolver resolver = mock(EventoActorContextResolver.class);
        when(resolver.resolveRequired()).thenReturn(ator);
        useCase = new AtualizarProjetoUseCase(repository, new ProjetoMapper(), mock(ProjetoAuditPublisher.class),
                resolver, authorization, eventos, organizacoes);

        projeto = new ProjetoEventoEntity();
        projeto.setId(UUID.randomUUID());
        projeto.setNome("Reforma");
        projeto.setStatus(ProjetoStatus.ABERTO);
        projeto.setOrganizacaoResponsavelId(ORG_ATUAL);
        projeto.setInicioUtc(INICIO);
        projeto.setFimUtc(FIM);
        when(repository.findById(projeto.getId())).thenReturn(Optional.of(projeto));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private AtualizarProjetoRequest request(UUID org, Instant inicio, Instant fim) {
        return new AtualizarProjetoRequest(null, null, org, inicio, fim, null);
    }

    private OrganizacaoEntity organizacao(boolean ativa) {
        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(ORG_DESTINO);
        org.setNome("Destino");
        org.setAtivo(ativa);
        return org;
    }

    @Test
    @DisplayName("RN-PROJ-03: encurtar o periodo com evento vinculado fora dele e recusado")
    void recusaPeriodoQueDeixaEventosForaDoProjeto() {
        Instant novoFim = Instant.parse("2026-06-01T03:00:00Z");
        when(eventos.contarEventosForaDoPeriodo(projeto.getId(), INICIO, novoFim)).thenReturn(2L);

        assertThatThrownBy(() -> useCase.execute(projeto.getId(), request(null, null, novoFim)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 evento(s)");

        verify(repository, never()).save(any());
        assertThat(projeto.getFimUtc()).isEqualTo(FIM);
    }

    @Test
    @DisplayName("Periodo que continua contendo os eventos vinculados e aceito")
    void aceitaPeriodoQueContemOsEventos() {
        Instant novoFim = Instant.parse("2026-12-01T03:00:00Z");
        when(eventos.contarEventosForaDoPeriodo(projeto.getId(), INICIO, novoFim)).thenReturn(0L);

        useCase.execute(projeto.getId(), request(null, null, novoFim));

        assertThat(projeto.getFimUtc()).isEqualTo(novoFim);
        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Sem mudanca de periodo nao consulta os eventos vinculados")
    void semMudancaDePeriodoNaoRevalida() {
        useCase.execute(projeto.getId(), new AtualizarProjetoRequest("Novo nome", null, null, null, null, null));

        verify(eventos, never()).contarEventosForaDoPeriodo(any(), any(), any());
    }

    @Test
    @DisplayName("Transferir o projeto exige alcada tambem sobre a organizacao de destino")
    void transferenciaAutorizaOrganizacaoDestino() {
        when(organizacoes.findById(ORG_DESTINO)).thenReturn(Optional.of(organizacao(true)));
        doThrow(new AccessDeniedException("sem alcada")).when(authorization).assertCanEdit(ator, ORG_DESTINO);

        assertThatThrownBy(() -> useCase.execute(projeto.getId(), request(ORG_DESTINO, null, null)))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository, never()).save(any());
        assertThat(projeto.getOrganizacaoResponsavelId()).isEqualTo(ORG_ATUAL);
    }

    @Test
    @DisplayName("Organizacao de destino inexistente ou inativa e recusada")
    void transferenciaExigeDestinoExistenteEAtivo() {
        when(organizacoes.findById(ORG_DESTINO)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(projeto.getId(), request(ORG_DESTINO, null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("destino");

        when(organizacoes.findById(ORG_DESTINO)).thenReturn(Optional.of(organizacao(false)));
        assertThatThrownBy(() -> useCase.execute(projeto.getId(), request(ORG_DESTINO, null, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("inativa");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Transferencia valida autoriza origem e destino e grava a nova organizacao")
    void transferenciaValida() {
        when(organizacoes.findById(ORG_DESTINO)).thenReturn(Optional.of(organizacao(true)));

        useCase.execute(projeto.getId(), request(ORG_DESTINO, null, null));

        verify(authorization).assertCanEdit(ator, ORG_ATUAL);
        verify(authorization).assertCanEdit(ator, ORG_DESTINO);
        assertThat(projeto.getOrganizacaoResponsavelId()).isEqualTo(ORG_DESTINO);
    }
}
