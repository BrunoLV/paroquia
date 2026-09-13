package br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaEntityTest {

    @Test
    @DisplayName("Construtor completo preenche todos os campos corretamente")
    void deveConstruirComTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID pastoralId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID aprovadorId = UUID.randomUUID();
        UUID canceladoPorId = UUID.randomUUID();
        Instant now = Instant.now();

        ReservaEntity entity = new ReservaEntity(
                id, "LOCAL", itemId, now, now.plusSeconds(3600), 15, "descricao",
                solicitanteId, pastoralId, eventoId, grupoId, "CONFIRMADA", aprovadorId, now,
                "motivo", canceladoPorId, "check", now, now, null, 2L);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTipoItem()).isEqualTo("LOCAL");
        assertThat(entity.getItemId()).isEqualTo(itemId);
        assertThat(entity.getDataHoraInicio()).isEqualTo(now);
        assertThat(entity.getDataHoraFim()).isEqualTo(now.plusSeconds(3600));
        assertThat(entity.getBufferMinutos()).isEqualTo(15);
        assertThat(entity.getDescricao()).isEqualTo("descricao");
        assertThat(entity.getSolicitanteId()).isEqualTo(solicitanteId);
        assertThat(entity.getPastoralMovimentoId()).isEqualTo(pastoralId);
        assertThat(entity.getEventoId()).isEqualTo(eventoId);
        assertThat(entity.getGrupoRecorrenciaId()).isEqualTo(grupoId);
        assertThat(entity.getStatus()).isEqualTo("CONFIRMADA");
        assertThat(entity.getAprovadorId()).isEqualTo(aprovadorId);
        assertThat(entity.getAprovadoEm()).isEqualTo(now);
        assertThat(entity.getMotivoCancelamento()).isEqualTo("motivo");
        assertThat(entity.getCanceladoPorId()).isEqualTo(canceladoPorId);
        assertThat(entity.getCheckAvarias()).isEqualTo("check");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Construtor vazio e setters preenchem todos os campos individualmente")
    void deveConstruirVazioEUsarSetters() {
        ReservaEntity entity = new ReservaEntity();
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID pastoralId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        UUID grupoId = UUID.randomUUID();
        UUID aprovadorId = UUID.randomUUID();
        UUID canceladoPorId = UUID.randomUUID();
        Instant now = Instant.now();

        entity.setId(id);
        entity.setTipoItem("ATIVO");
        entity.setItemId(itemId);
        entity.setDataHoraInicio(now);
        entity.setDataHoraFim(now.plusSeconds(1800));
        entity.setBufferMinutos(10);
        entity.setDescricao("outra descricao");
        entity.setSolicitanteId(solicitanteId);
        entity.setPastoralMovimentoId(pastoralId);
        entity.setEventoId(eventoId);
        entity.setGrupoRecorrenciaId(grupoId);
        entity.setStatus("PENDENTE");
        entity.setAprovadorId(aprovadorId);
        entity.setAprovadoEm(now);
        entity.setMotivoCancelamento("motivo x");
        entity.setCanceladoPorId(canceladoPorId);
        entity.setCheckAvarias("check x");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(now);
        entity.setVersion(5L);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getTipoItem()).isEqualTo("ATIVO");
        assertThat(entity.getItemId()).isEqualTo(itemId);
        assertThat(entity.getDataHoraInicio()).isEqualTo(now);
        assertThat(entity.getDataHoraFim()).isEqualTo(now.plusSeconds(1800));
        assertThat(entity.getBufferMinutos()).isEqualTo(10);
        assertThat(entity.getDescricao()).isEqualTo("outra descricao");
        assertThat(entity.getSolicitanteId()).isEqualTo(solicitanteId);
        assertThat(entity.getPastoralMovimentoId()).isEqualTo(pastoralId);
        assertThat(entity.getEventoId()).isEqualTo(eventoId);
        assertThat(entity.getGrupoRecorrenciaId()).isEqualTo(grupoId);
        assertThat(entity.getStatus()).isEqualTo("PENDENTE");
        assertThat(entity.getAprovadorId()).isEqualTo(aprovadorId);
        assertThat(entity.getAprovadoEm()).isEqualTo(now);
        assertThat(entity.getMotivoCancelamento()).isEqualTo("motivo x");
        assertThat(entity.getCanceladoPorId()).isEqualTo(canceladoPorId);
        assertThat(entity.getCheckAvarias()).isEqualTo("check x");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(5L);
    }
}
