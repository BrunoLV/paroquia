-- Os papéis que um vínculo pode ter em cada tipo de organização moram no domínio (PapeisPorTipoOrganizacao).
-- Esta migração repete a regra no banco, para que carga direta fora da aplicação não grave uma combinação
-- inválida, e normaliza os vínculos antigos que já a violam, registrando o que mudou.

CREATE OR REPLACE FUNCTION calendario.papel_vinculo_valido(p_tipo text, p_papel text) RETURNS boolean
    LANGUAGE sql IMMUTABLE
AS $$
    SELECT CASE p_tipo
        WHEN 'CONSELHO' THEN lower(replace(p_papel, '_', '-')) IN (
            'secretario-conselho', 'membro-conselho-pastoral', 'membro-conselho-economico',
            'coordenador', 'secretario-grupo')
        WHEN 'PASTORAL' THEN lower(replace(p_papel, '_', '-')) IN (
            'coordenador', 'secretario-grupo', 'membro-ativo', 'voluntario')
        WHEN 'MOVIMENTO' THEN lower(replace(p_papel, '_', '-')) IN (
            'coordenador', 'secretario-grupo', 'membro-ativo', 'voluntario')
        WHEN 'SERVICO_PAROQUIAL' THEN lower(replace(p_papel, '_', '-')) IN (
            'coordenador', 'secretario-grupo', 'membro-ativo', 'voluntario')
        ELSE FALSE
    END;
$$;

COMMENT ON FUNCTION calendario.papel_vinculo_valido(text, text) IS 'Indica se o tipo de organização admite o papel de vínculo.';

-- Papel de membro comum do tipo, para onde vai o vínculo que tinha um papel inválido (o de menor privilégio).
CREATE OR REPLACE FUNCTION calendario.papel_vinculo_de_membro(p_tipo text, p_subtipo text) RETURNS text
    LANGUAGE sql IMMUTABLE
AS $$
    SELECT CASE
        WHEN p_tipo <> 'CONSELHO' THEN 'membro-ativo'
        WHEN p_subtipo = 'CAEP' THEN 'membro-conselho-economico'
        ELSE 'membro-conselho-pastoral'
    END;
$$;

COMMENT ON FUNCTION calendario.papel_vinculo_de_membro(text, text) IS 'Papel de membro comum do tipo de organização (e do subtipo, no conselho).';

CREATE TABLE calendario.membro_grupo_papel_normalizado (
    membro_grupo_id uuid NOT NULL,
    organizacao_id uuid NOT NULL,
    papel_anterior character varying(64) NOT NULL,
    papel_novo character varying(64) NOT NULL,
    normalizado_em_utc timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT membro_grupo_papel_normalizado_pkey PRIMARY KEY (membro_grupo_id, normalizado_em_utc)
);

COMMENT ON TABLE calendario.membro_grupo_papel_normalizado IS 'Vínculos cujo papel era inválido para o tipo da organização e foi trocado pelo de membro comum.';
COMMENT ON COLUMN calendario.membro_grupo_papel_normalizado.membro_grupo_id IS 'Vínculo normalizado.';
COMMENT ON COLUMN calendario.membro_grupo_papel_normalizado.organizacao_id IS 'Organização do vínculo.';
COMMENT ON COLUMN calendario.membro_grupo_papel_normalizado.papel_anterior IS 'Papel que o vínculo tinha antes da normalização.';
COMMENT ON COLUMN calendario.membro_grupo_papel_normalizado.papel_novo IS 'Papel de membro comum atribuído.';
COMMENT ON COLUMN calendario.membro_grupo_papel_normalizado.normalizado_em_utc IS 'Instante UTC da normalização.';

INSERT INTO calendario.membro_grupo_papel_normalizado (membro_grupo_id, organizacao_id, papel_anterior, papel_novo)
SELECT m.id, o.id, m.papel, calendario.papel_vinculo_de_membro(o.tipo, o.subtipo_conselho)
FROM calendario.membro_grupo m
JOIN calendario.organizacao o ON o.id = m.organizacao_id
WHERE NOT calendario.papel_vinculo_valido(o.tipo, m.papel);

UPDATE calendario.membro_grupo m
SET papel = calendario.papel_vinculo_de_membro(o.tipo, o.subtipo_conselho)
FROM calendario.organizacao o
WHERE o.id = m.organizacao_id AND NOT calendario.papel_vinculo_valido(o.tipo, m.papel);

CREATE OR REPLACE FUNCTION calendario.validar_papel_vinculo() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    tipo_org text;
BEGIN
    SELECT tipo INTO tipo_org FROM calendario.organizacao WHERE id = NEW.organizacao_id;
    -- organização ainda ausente (carga em lote com chaves adiadas): a chave estrangeira cobrirá a falta
    IF tipo_org IS NOT NULL AND NOT calendario.papel_vinculo_valido(tipo_org, NEW.papel) THEN
        RAISE EXCEPTION 'O papel % não existe em organização do tipo %', NEW.papel, tipo_org
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION calendario.validar_papel_vinculo() IS 'Impede vínculo com papel que o tipo da organização não admite.';

CREATE TRIGGER trg_membro_grupo_papel_valido
    BEFORE INSERT OR UPDATE OF papel, organizacao_id ON calendario.membro_grupo
    FOR EACH ROW EXECUTE FUNCTION calendario.validar_papel_vinculo();
