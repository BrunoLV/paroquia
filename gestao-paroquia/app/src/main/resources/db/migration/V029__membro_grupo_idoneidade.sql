-- Idoneidade e envio dos agentes: formação, vivência sacramental, proteção de menores e missão conferida pelo Pároco.
ALTER TABLE calendario.membro_grupo ADD COLUMN formacao_concluida_em date;
ALTER TABLE calendario.membro_grupo ADD COLUMN vivencia_sacramental_atestada_em date;
ALTER TABLE calendario.membro_grupo ADD COLUMN termo_protecao_menores_em date;
ALTER TABLE calendario.membro_grupo ADD COLUMN termo_protecao_menores_valido_ate date;
ALTER TABLE calendario.membro_grupo ADD COLUMN enviado_em date;
ALTER TABLE calendario.membro_grupo ADD COLUMN enviado_por character varying(128);

COMMENT ON COLUMN calendario.membro_grupo.formacao_concluida_em IS 'Data de conclusão da formação doutrinária básica do agente; exigida de quem coordena.';
COMMENT ON COLUMN calendario.membro_grupo.vivencia_sacramental_atestada_em IS 'Data em que o Pároco atestou a vivência sacramental condizente com a função litúrgica ou catequética.';
COMMENT ON COLUMN calendario.membro_grupo.termo_protecao_menores_em IS 'Data de assinatura do termo de compromisso de proteção de menores e vulneráveis; apenas a data é guardada, nunca o documento.';
COMMENT ON COLUMN calendario.membro_grupo.termo_protecao_menores_valido_ate IS 'Último dia de validade do termo de proteção de menores; nulo significa vigente desde a assinatura.';
COMMENT ON COLUMN calendario.membro_grupo.enviado_em IS 'Data do envio (missão) conferido pelo Pároco ao agente.';
COMMENT ON COLUMN calendario.membro_grupo.enviado_por IS 'Papel/identificador de quem conferiu o envio, sem nome pessoal (LGPD).';
