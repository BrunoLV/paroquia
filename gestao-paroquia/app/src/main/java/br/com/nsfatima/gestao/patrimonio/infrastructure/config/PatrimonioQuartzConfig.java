package br.com.nsfatima.gestao.patrimonio.infrastructure.config;

import br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling.SincronizarStatusReservadoJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agenda no Quartz a varredura diária de reconciliação do status {@code RESERVADO} dos ativos
 * patrimoniais (madrugada). O listener de eventos cobre as transições imediatas de reserva; este
 * job cobre o simples decorrer do tempo — início e fim de período de uma reserva confirmada, que
 * não têm transição de estado disparando evento.
 */
@Configuration
public class PatrimonioQuartzConfig {

    private static final String CRON_DIARIO_MADRUGADA = "0 15 3 * * ?";

    @Bean
    public JobDetail sincronizarStatusReservadoJobDetail() {
        return JobBuilder.newJob(SincronizarStatusReservadoJob.class)
                .withIdentity("sincronizarStatusReservadoJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger sincronizarStatusReservadoJobTrigger(JobDetail sincronizarStatusReservadoJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(sincronizarStatusReservadoJobDetail)
                .withIdentity("sincronizarStatusReservadoJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_DIARIO_MADRUGADA))
                .build();
    }
}
