/**
 * MIGRAÇÃO: V019__adicionar_formato_evento.sql
 * CONTEXTO: Adiciona o formato de realização do evento (Presencial, Online, Híbrido).
 * REGRA: Eventos podem ser presenciais, remotos (videoconferência) ou híbridos.
 *        A presença ou ausência de reservas físicas é independente do formato.
 */

ALTER TABLE calendario.eventos
    ADD COLUMN IF NOT EXISTS formato VARCHAR(32) NOT NULL DEFAULT 'PRESENCIAL';

COMMENT ON COLUMN calendario.eventos.formato IS 'Formato de realização do evento: PRESENCIAL, ONLINE, HIBRIDO';
