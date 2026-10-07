-- Tipologia eclesial das organizações: serviço paroquial, subtipo de conselho e reconhecimento de estatutos.
ALTER TABLE calendario.organizacao ADD COLUMN subtipo_conselho character varying(16);
COMMENT ON COLUMN calendario.organizacao.subtipo_conselho IS 'Natureza do conselho (CPP, CPC ou CAEP); preenchida somente para organizações do tipo CONSELHO. Pastorais, movimentos e serviços só se articulam a conselhos pastorais (CPP/CPC), nunca ao CAEP.';
COMMENT ON COLUMN calendario.organizacao.tipo IS 'Tipologia eclesial: PASTORAL (orgânica), MOVIMENTO (apostólico, exige estatuto reconhecido), SERVICO_PAROQUIAL ou CONSELHO.';
COMMENT ON COLUMN calendario.organizacao.setor_pastoral IS 'Múnus a que a organização serve (ensinar, santificar ou governar); obrigatório para pastorais e serviços paroquiais.';

-- Valor legado MINISTERIO passa a se chamar SERVICO_PAROQUIAL.
UPDATE calendario.organizacao SET tipo = 'SERVICO_PAROQUIAL' WHERE tipo IN ('MINISTERIO', 'OUTRO');
UPDATE calendario.membro_grupo SET tipo_organizacao = 'SERVICO_PAROQUIAL' WHERE tipo_organizacao IN ('MINISTERIO', 'OUTRO');
UPDATE calendario.aprovacoes SET solicitante_tipo_organizacao = 'SERVICO_PAROQUIAL' WHERE solicitante_tipo_organizacao IN ('MINISTERIO', 'OUTRO');
UPDATE calendario.projeto_solicitacoes_aprovacao SET solicitante_tipo_organizacao = 'SERVICO_PAROQUIAL' WHERE solicitante_tipo_organizacao IN ('MINISTERIO', 'OUTRO');

-- Subtipo dos conselhos existentes inferido do nome (revisar no cadastro): econômico = CAEP, comunitário = CPC, demais = CPP.
UPDATE calendario.organizacao SET subtipo_conselho = CASE
        WHEN nome ILIKE '%econ%' THEN 'CAEP'
        WHEN nome ILIKE '%comunit%' THEN 'CPC'
        ELSE 'CPP' END
WHERE tipo = 'CONSELHO' AND subtipo_conselho IS NULL;

ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN ambito character varying(16);
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN estatuto_autoridade character varying(150);
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN estatuto_data date;
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN estatuto_referencia character varying(255);
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.ambito IS 'Âmbito de origem do organismo (PAROQUIAL, DIOCESANO, NACIONAL ou INTERNACIONAL).';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.estatuto_autoridade IS 'Autoridade eclesiástica que reconheceu os estatutos (Cân. 299 e 304); exigida para movimentos apostólicos.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.estatuto_data IS 'Data do reconhecimento formal dos estatutos.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.estatuto_referencia IS 'Decreto, documento ou protocolo que formaliza o reconhecimento dos estatutos.';
