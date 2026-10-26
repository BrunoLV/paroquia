/**
 * MIGRAÇÃO EVOLUTIVA: V042__historico_consentimento_append_only.sql
 * CONTEXTO: A trilha de consentimento (Arts. 8º, § 2º e 6º, X da LGPD) é prova e deve ser imutável.
 * REGRA: o banco bloqueia DELETE e TRUNCATE e permite UPDATE somente para ANONIMIZAR dados pessoais
 * (IP, user-agent e dados do responsável legal passando a NULL) e atualizar carimbos de auditoria. Qualquer
 * outra alteração (evento, termo, flags de autorização, titular) é rejeitada. Executa após V041, que
 * cifra os dados legados da própria trilha.
 */

CREATE OR REPLACE FUNCTION calendario.fn_historico_consentimento_append_only()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP <> 'UPDATE' THEN
        RAISE EXCEPTION 'historico_consentimentos é append-only: % não é permitido', TG_OP
            USING ERRCODE = 'integrity_constraint_violation';
    END IF;

    IF (to_jsonb(NEW) - 'ip_origem' - 'user_agent' - 'responsavel_menor_nome' - 'responsavel_menor_cpf'
            - 'atualizado_em_utc' - 'atualizado_por')
       IS DISTINCT FROM
       (to_jsonb(OLD) - 'ip_origem' - 'user_agent' - 'responsavel_menor_nome' - 'responsavel_menor_cpf'
            - 'atualizado_em_utc' - 'atualizado_por')
       OR NOT (NEW.ip_origem IS NULL OR NEW.ip_origem IS NOT DISTINCT FROM OLD.ip_origem)
       OR NOT (NEW.user_agent IS NULL OR NEW.user_agent IS NOT DISTINCT FROM OLD.user_agent)
       OR NOT (NEW.responsavel_menor_nome IS NULL
               OR NEW.responsavel_menor_nome IS NOT DISTINCT FROM OLD.responsavel_menor_nome)
       OR NOT (NEW.responsavel_menor_cpf IS NULL
               OR NEW.responsavel_menor_cpf IS NOT DISTINCT FROM OLD.responsavel_menor_cpf) THEN
        RAISE EXCEPTION 'historico_consentimentos é append-only: apenas a anonimização de dados pessoais é permitida'
            USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION calendario.fn_historico_consentimento_append_only() IS
    'Bloqueia DELETE e qualquer UPDATE da trilha de consentimento que não seja anonimização de PII (LGPD).';

DROP TRIGGER IF EXISTS trg_historico_consentimento_append_only ON calendario.historico_consentimentos;
CREATE TRIGGER trg_historico_consentimento_append_only
    BEFORE UPDATE OR DELETE ON calendario.historico_consentimentos
    FOR EACH ROW EXECUTE FUNCTION calendario.fn_historico_consentimento_append_only();

COMMENT ON TRIGGER trg_historico_consentimento_append_only ON calendario.historico_consentimentos IS
    'Imutabilidade da trilha de consentimento: só permite anonimizar PII.';
