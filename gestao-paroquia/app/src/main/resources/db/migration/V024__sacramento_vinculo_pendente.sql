/**
 * MIGRAÇÃO: V024__sacramento_vinculo_pendente.sql
 * CONTEXTO: Vínculo tardio proposto pela Secretaria só passa a valer após aprovação do Pároco.
 * REGRA: Expand & Contract; coluna NOT NULL com DEFAULT false (vínculos existentes seguem aprovados).
 */
ALTER TABLE calendario.assento_participantes
    ADD COLUMN vinculo_pendente boolean DEFAULT false NOT NULL;
COMMENT ON COLUMN calendario.assento_participantes.vinculo_pendente IS 'Vínculo a fiel/clérigo proposto pela Secretaria e ainda não aprovado pelo Pároco.';
