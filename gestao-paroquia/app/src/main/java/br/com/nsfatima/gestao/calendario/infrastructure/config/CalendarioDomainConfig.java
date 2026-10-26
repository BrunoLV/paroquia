package br.com.nsfatima.gestao.calendario.infrastructure.config;

import br.com.nsfatima.gestao.calendario.application.service.ValidadorPrioridadeAgenda;
import br.com.nsfatima.gestao.calendario.domain.policy.CleroCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.CompatibilidadePapelOrganizacionalPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ConselhoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.MovimentoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PastoralCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ServicoParoquialCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.IntegridadeCalendarioPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.AnoParoquialFechadoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.AutorObservacaoSistemaPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.AutorTecnicoObservacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.MutacaoObservacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.NotaManualMutacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.HierarquiaPrioridadeAgendaPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PrioridadeAgendaPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.AutorizacaoCancelamentoEventoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PadraoAutorizacaoCancelamentoEventoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.EventoPatchAuthorizationPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PadraoEventoPatchAuthorizationPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/** Registra Strategies puras do calendário sem contaminar a camada de domínio com Spring. */
@Configuration
public class CalendarioDomainConfig {

    /** Compõe consulta e persistência no limite da aplicação, mantendo a Strategy sem I/O. */
    @Bean
    public ValidadorPrioridadeAgenda validadorPrioridadeAgenda(
            EventoJpaRepository eventoJpaRepository,
            PrioridadeAgendaPolicy prioridadeAgendaPolicy) {
        return new ValidadorPrioridadeAgenda(eventoJpaRepository, prioridadeAgendaPolicy);
    }

    @Bean
    public PrioridadeAgendaPolicy prioridadeAgendaPolicy() {
        return new HierarquiaPrioridadeAgendaPolicy();
    }

    @Bean
    public MutacaoObservacaoPolicy mutacaoObservacaoPolicy() {
        return new NotaManualMutacaoPolicy();
    }

    @Bean
    public AutorObservacaoSistemaPolicy autorObservacaoSistemaPolicy() {
        return new AutorTecnicoObservacaoPolicy();
    }

    @Bean
    public BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy(ZoneId outputZoneId) {
        return new AnoParoquialFechadoPolicy(outputZoneId);
    }

    @Bean
    public IntegridadeCalendarioPolicy integridadeCalendarioPolicy() {
        return new IntegridadeCalendarioPolicy();
    }

    @Bean
    public PeriodoOperacionalPolicy periodoOperacionalPolicy() {
        return new PeriodoOperacionalPolicy();
    }

    @Bean
    public CompatibilidadePapelOrganizacionalPolicy cleroCompatibilidadePapelPolicy() {
        return new CleroCompatibilidadePapelPolicy();
    }

    @Bean
    public CompatibilidadePapelOrganizacionalPolicy pastoralCompatibilidadePapelPolicy() {
        return new PastoralCompatibilidadePapelPolicy();
    }

    @Bean
    public CompatibilidadePapelOrganizacionalPolicy movimentoCompatibilidadePapelPolicy() {
        return new MovimentoCompatibilidadePapelPolicy();
    }

    @Bean
    public CompatibilidadePapelOrganizacionalPolicy conselhoCompatibilidadePapelPolicy() {
        return new ConselhoCompatibilidadePapelPolicy();
    }

    @Bean
    public CompatibilidadePapelOrganizacionalPolicy servicoParoquialCompatibilidadePapelPolicy() {
        return new ServicoParoquialCompatibilidadePapelPolicy();
    }

    @Bean
    public AutorizacaoCancelamentoEventoPolicy autorizacaoCancelamentoEventoPolicy() {
        return new PadraoAutorizacaoCancelamentoEventoPolicy();
    }

    @Bean
    public EventoPatchAuthorizationPolicy eventoPatchAuthorizationPolicy() {
        return new PadraoEventoPatchAuthorizationPolicy();
    }

    @Bean
    public EventoDomainService eventoDomainService(IntegridadeCalendarioPolicy integridadeCalendarioPolicy) {
        return new EventoDomainService(integridadeCalendarioPolicy);
    }
}
