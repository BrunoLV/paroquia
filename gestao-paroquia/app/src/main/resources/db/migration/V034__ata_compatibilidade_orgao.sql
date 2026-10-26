-- Trava de defesa em profundidade: o tipo da reunião precisa ser compatível com o órgão que a convoca.
-- A regra mora no domínio (RegrasTipoReuniaoPolicy); este gatilho a repete para que carga direta no banco,
-- fora da aplicação, não grave uma ata de tipo incompatível com o seu órgão.

CREATE OR REPLACE FUNCTION calendario.validar_compatibilidade_ata_orgao() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    convocante RECORD;
    associada RECORD;
    compativel boolean;
BEGIN
    SELECT tipo, subtipo_conselho INTO convocante FROM calendario.organizacao WHERE id = NEW.organizacao_convocante_id;

    compativel := CASE NEW.tipo
        WHEN 'REUNIAO_CPP' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPP'
        WHEN 'REUNIAO_CPC' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPC'
        WHEN 'REUNIAO_CAEP' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CAEP'
        WHEN 'REUNIAO_CONJUNTA_CPP_CAEP' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPP'
        WHEN 'ASSEMBLEIA_PAROQUIAL' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPP'
        WHEN 'PLANEJAMENTO_PASTORAL' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPP'
        WHEN 'ASSEMBLEIA_COMUNITARIA' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho = 'CPC'
        WHEN 'REUNIAO_PASTORAL' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho IN ('CPP', 'CPC')
        WHEN 'REUNIAO_COORDENADORES' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho IN ('CPP', 'CPC')
        WHEN 'REUNIAO_ANIMADORES' THEN convocante.tipo = 'CONSELHO' AND convocante.subtipo_conselho IN ('CPP', 'CPC')
        WHEN 'REUNIAO_ORGANISMO' THEN convocante.tipo IN ('PASTORAL', 'MOVIMENTO', 'SERVICO_PAROQUIAL')
        WHEN 'COMISSAO_ESPECIAL' THEN TRUE
        ELSE FALSE
    END;
    IF NOT COALESCE(compativel, FALSE) THEN
        RAISE EXCEPTION 'O órgão convocante não pode convocar reunião do tipo %', NEW.tipo
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.tipo = 'REUNIAO_CONJUNTA_CPP_CAEP' THEN
        SELECT o.tipo, o.subtipo_conselho, o.parent_id INTO associada
            FROM calendario.organizacao o WHERE o.id = NEW.organizacao_associada_id;
        IF NEW.organizacao_associada_id IS NULL OR associada.tipo IS DISTINCT FROM 'CONSELHO'
                OR associada.subtipo_conselho IS DISTINCT FROM 'CAEP'
                OR associada.parent_id IS DISTINCT FROM (
                    SELECT parent_id FROM calendario.organizacao WHERE id = NEW.organizacao_convocante_id) THEN
            RAISE EXCEPTION 'A reunião conjunta exige o CAEP da mesma unidade do órgão convocante'
                USING ERRCODE = 'check_violation';
        END IF;
    ELSIF NEW.organizacao_associada_id IS NOT NULL THEN
        RAISE EXCEPTION 'Este tipo de reunião não admite organização associada'
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION calendario.validar_compatibilidade_ata_orgao() IS 'Impede ata de tipo de reunião incompatível com o órgão convocante ou associado.';

CREATE TRIGGER trg_ata_reuniao_compatibilidade_orgao
    BEFORE INSERT OR UPDATE OF tipo, organizacao_convocante_id, organizacao_associada_id ON calendario.ata_reuniao
    FOR EACH ROW EXECUTE FUNCTION calendario.validar_compatibilidade_ata_orgao();
