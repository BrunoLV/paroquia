-- ==============================================================================
-- Migração V017: Estrutura de Horário de Evento (Dia Inteiro, Períodos e Duração)
-- Adiciona suporte explícito a agendamentos semânticos no calendário paroquial.
-- ==============================================================================

ALTER TABLE calendario.eventos 
ADD COLUMN IF NOT EXISTS tipo_horario VARCHAR(32) NOT NULL DEFAULT 'PERSONALIZADO';

COMMENT ON COLUMN calendario.eventos.tipo_horario IS 'Formato de horário do evento: PERSONALIZADO, DIA_INTEIRO, MADRUGADA, MANHA, TARDE, NOITE';
