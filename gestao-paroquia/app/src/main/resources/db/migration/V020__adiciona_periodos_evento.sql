ALTER TABLE calendario.eventos
    ADD COLUMN IF NOT EXISTS periodos VARCHAR(128);

COMMENT ON COLUMN calendario.eventos.periodos IS 'Lista separada por vírgula de turnos/períodos do dia selecionados para o evento (ex: MANHA,NOITE).';
