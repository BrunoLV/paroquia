package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Materializa uma única ocorrência de série recorrente: detecta conflito, grava o evento, copia locais e
 * envolvidos e cria as reservas. Vive em bean separado do {@link GeradorRecorrenciaAnualJob} para que a
 * fronteira transacional possa ser <strong>por ocorrência</strong>:
 * <ul>
 *   <li>{@link #materializarIsolada}: transação própria ({@code REQUIRES_NEW}); a falha de uma ocorrência
 *       (por exemplo, uma reserva tomada por outra requisição entre a checagem e a gravação) desfaz só
 *       aquela ocorrência e a rotina anual segue com as demais. Só serve quando a série já está
 *       confirmada no banco, que é o caso da rotina anual.</li>
 *   <li>{@link #materializar}: participa da transação de quem chama; usada logo após criar a série, que
 *       ainda não foi confirmada e não é visível a outra transação. Ali o conflito é erro para o usuário e
 *       a criação inteira é desfeita.</li>
 * </ul>
 * Não se usa savepoint ({@code NESTED}) porque o {@code JpaTransactionManager} com Hibernate não o suporta.
 */
@Component
public class MaterializadorOcorrenciaRecorrencia {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaterializadorOcorrenciaRecorrencia.class);

    private final EventoJpaRepository eventoRepository;
    private final EventoLocalJpaRepository eventoLocalRepository;
    private final EventoEnvolvidoJpaRepository envolvidoRepository;
    private final DetectorConflitoEventoService detectorConflito;
    private final EventoReservaIntegrationService reservaIntegration;

    public MaterializadorOcorrenciaRecorrencia(
            EventoJpaRepository eventoRepository,
            EventoLocalJpaRepository eventoLocalRepository,
            EventoEnvolvidoJpaRepository envolvidoRepository,
            DetectorConflitoEventoService detectorConflito,
            EventoReservaIntegrationService reservaIntegration) {
        this.eventoRepository = eventoRepository;
        this.eventoLocalRepository = eventoLocalRepository;
        this.envolvidoRepository = envolvidoRepository;
        this.detectorConflito = detectorConflito;
        this.reservaIntegration = reservaIntegration;
    }

    /**
     * Materializa a ocorrência. Conflito de agenda detectado não é erro: a ocorrência é pulada.
     *
     * @param modelo fotografia do evento-base (locais, ativos e envolvidos já carregados)
     * @param regra série à qual a ocorrência pertence
     * @param inicio início da ocorrência
     * @param fim fim da ocorrência
     * @return {@code true} se a ocorrência foi gravada; {@code false} se foi pulada por conflito
     */
    @Transactional
    public boolean materializar(ModeloOcorrencia modelo, EventoRecorrenciaEntity regra, Instant inicio, Instant fim) {
        return gravar(modelo, regra, inicio, fim);
    }

    private boolean gravar(ModeloOcorrencia modelo, EventoRecorrenciaEntity regra, Instant inicio, Instant fim) {
        EventoEntity base = modelo.base();
        var conflitos = detectorConflito.detectarConflitos(inicio, fim, base.getLocalId(), modelo.locais(),
                modelo.ativos(), base.getCategoria(), null, base.getTipoHorario(), base.getIgrejaVinculoId(),
                base.getPeriodosList());
        if (!conflitos.isEmpty()) {
            LOGGER.warn("Ocorrência de '{}' em {} não materializada: {} conflito(s) de agenda na série {}",
                    base.getTitulo(), inicio, conflitos.size(), regra.getId());
            return false;
        }

        EventoEntity instancia = eventoRepository.save(novaInstancia(base, regra, inicio, fim));
        copiarLocais(instancia, modelo.locais());
        copiarEnvolvidos(instancia, modelo.envolvidos());
        reservaIntegration.criarReservasEvento(instancia.getId(), instancia.getTitulo(), instancia.getLocalId(),
                modelo.locais(), modelo.ativos(), null, instancia.getOrganizacaoResponsavelId(), inicio, fim,
                base.getPeriodosList());
        return true;
    }

    /**
     * Igual a {@link #materializar}, mas em transação própria: a falha desta ocorrência não contamina a
     * transação do chamador.
     *
     * @param modelo fotografia do evento-base
     * @param regra série (já confirmada no banco) à qual a ocorrência pertence
     * @param inicio início da ocorrência
     * @param fim fim da ocorrência
     * @return {@code true} se gravada; {@code false} se pulada por conflito
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean materializarIsolada(
            ModeloOcorrencia modelo, EventoRecorrenciaEntity regra, Instant inicio, Instant fim) {
        return gravar(modelo, regra, inicio, fim);
    }

    private EventoEntity novaInstancia(EventoEntity base, EventoRecorrenciaEntity regra, Instant inicio, Instant fim) {
        EventoEntity instancia = new EventoEntity();
        instancia.setId(UUID.randomUUID());
        instancia.setTitulo(base.getTitulo());
        instancia.setDescricao(base.getDescricao());
        instancia.setCategoria(base.getCategoria());
        instancia.setLocalId(base.getLocalId());
        instancia.setIgrejaVinculoId(base.getIgrejaVinculoId());
        instancia.setProjetoId(base.getProjetoId());
        instancia.setOrganizacaoResponsavelId(base.getOrganizacaoResponsavelId());
        instancia.setStatus(base.getStatus());
        instancia.setInicioUtc(inicio);
        instancia.setFimUtc(fim);
        instancia.setRecorrenciaId(regra.getId());
        instancia.setTipoHorario(base.getTipoHorario());
        instancia.setPeriodos(base.getPeriodos());
        instancia.setFormato(base.getFormato());
        return instancia;
    }

    private void copiarLocais(EventoEntity instancia, List<UUID> locais) {
        if (!locais.isEmpty()) {
            eventoLocalRepository.saveAll(
                    locais.stream().map(locId -> new EventoLocalEntity(instancia.getId(), locId)).toList());
        } else if (instancia.getLocalId() != null) {
            eventoLocalRepository.save(new EventoLocalEntity(instancia.getId(), instancia.getLocalId()));
        }
    }

    private void copiarEnvolvidos(EventoEntity instancia, List<EventoEnvolvidoEntity> envolvidosBase) {
        if (envolvidosBase.isEmpty()) {
            return;
        }
        envolvidoRepository.saveAll(envolvidosBase.stream().map(origem -> {
            EventoEnvolvidoEntity copia = new EventoEnvolvidoEntity();
            copia.setEventoId(instancia.getId());
            copia.setOrganizacaoId(origem.getOrganizacaoId());
            copia.setPapelParticipacao(origem.getPapelParticipacao());
            return copia;
        }).toList());
    }

    /** Fotografia do evento-base reaproveitada por todas as ocorrências da série (evita N+1). */
    public record ModeloOcorrencia(
            EventoEntity base, List<UUID> locais, List<UUID> ativos, List<EventoEnvolvidoEntity> envolvidos) {
    }
}
