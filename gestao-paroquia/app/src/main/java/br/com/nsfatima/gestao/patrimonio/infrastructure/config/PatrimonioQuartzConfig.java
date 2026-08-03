package br.com.nsfatima.gestao.patrimonio.infrastructure.config;

import br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling.AtivoAlocacaoAgendaJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agenda no Quartz a execução diária do job que atualiza o status das ocupações de ativos
 * patrimoniais (ex.: marcar como atrasadas as que ultrapassaram a data de fim), disparado
 * diariamente de madrugada.
 */
@Configuration
public class PatrimonioQuartzConfig {

    private static final String CRON_DIARIO_MADRUGADA = "0 10 0 * * ?";

    /**
     * Define o job do Quartz que executa {@link AtivoAlocacaoAgendaJob}, marcado como durável
     * para poder existir sem um trigger associado no momento do registro.
     *
     * @return o detalhe do job registrado no scheduler
     */
    @Bean
    public JobDetail ativoAlocacaoAgendaJobDetail() {
        return JobBuilder.newJob(AtivoAlocacaoAgendaJob.class)
                .withIdentity("ativoAlocacaoAgendaJob")
                .storeDurably()
                .build();
    }

    /**
     * Define o gatilho cron que dispara o job diariamente de madrugada.
     *
     * @param ativoAlocacaoAgendaJobDetail job a ser disparado
     * @return o trigger registrado no scheduler
     */
    @Bean
    public Trigger ativoAlocacaoAgendaJobTrigger(JobDetail ativoAlocacaoAgendaJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ativoAlocacaoAgendaJobDetail)
                .withIdentity("ativoAlocacaoAgendaJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_DIARIO_MADRUGADA))
                .build();
    }
}
