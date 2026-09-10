-- V085: Remove o subsistema de alocação de ativo a evento / empréstimo intercomunitário.
--   Todo uso temporário de bem passa a ser gerido pelo módulo de Reservas (tabela calendario.reservas).
--   O valor de enum TipoSolicitacaoInput.ALOCACAO_ATIVO_EMPRESTIMO é preservado (histórico), apenas
--   as solicitações ainda abertas são encerradas, pois a estratégia de execução foi removida.

UPDATE calendario.aprovacoes
SET status = 'REPROVADA',
    decidido_em_utc = NOW(),
    decision_observacao = 'Encerrada automaticamente: fluxo de empréstimo de ativo descontinuado (V085).'
WHERE tipo_solicitacao = 'ALOCACAO_ATIVO_EMPRESTIMO'
  AND status = 'PENDENTE';

DROP TABLE IF EXISTS calendario.alocacoes_ativos;

DELETE FROM calendario.job_locks WHERE name = 'ATIVO_ALOCACAO_AGENDA_JOB';
