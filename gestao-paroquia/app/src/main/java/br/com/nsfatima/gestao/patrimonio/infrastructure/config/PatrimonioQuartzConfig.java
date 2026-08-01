package br.com.nsfatima.gestao.patrimonio.infrastructure.config;

import br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling.AtivoAlocacaoAgendaJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatrimonioQuartzConfig {

    private static final String CRON_DIARIO_MADRUGADA = "0 10 0 * * ?";

    @Bean
    public JobDetail ativoAlocacaoAgendaJobDetail() {
        return JobBuilder.newJob(AtivoAlocacaoAgendaJob.class)
                .withIdentity("ativoAlocacaoAgendaJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger ativoAlocacaoAgendaJobTrigger(JobDetail ativoAlocacaoAgendaJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ativoAlocacaoAgendaJobDetail)
                .withIdentity("ativoAlocacaoAgendaJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_DIARIO_MADRUGADA))
                .build();
    }
}
