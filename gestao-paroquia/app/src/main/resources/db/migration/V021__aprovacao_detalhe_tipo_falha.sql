ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS tipo_falha_execucao VARCHAR(32),
    ADD COLUMN IF NOT EXISTS detalhe_erro_execucao TEXT;

COMMENT ON COLUMN calendario.aprovacoes.tipo_falha_execucao IS 'Classificação da falha de execução da aprovação (NEGOCIO_CADASTRAL ou SISTEMICA_APLICACAO).';
COMMENT ON COLUMN calendario.aprovacoes.detalhe_erro_execucao IS 'Detalhamento analítico e técnico da falha de execução para fins de troubleshooting e suporte.';
