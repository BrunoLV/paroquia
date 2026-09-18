/**
 * MIGRAÇÃO: V015__padronizacao_endereco_clero.sql
 * CONTEXTO: Padroniza o endereço de Clérigos com dados estruturados alinhados ao módulo de Locais e Fiéis.
 * REGRA: Dados residenciais civis do clero são segregados e protegidos por criptografia em repouso (LGPD).
 */

ALTER TABLE calendario.clerigos
    ADD COLUMN IF NOT EXISTS endereco_cep character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_logradouro character varying(500),
    ADD COLUMN IF NOT EXISTS endereco_numero character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_complemento character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_bairro character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_cidade character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_uf character varying(2);

COMMENT ON COLUMN calendario.clerigos.endereco_cep IS 'CEP do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_logradouro IS 'Logradouro do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_numero IS 'Número predial do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_complemento IS 'Complemento do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_bairro IS 'Bairro do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_cidade IS 'Cidade do endereço residencial do clérigo (criptografado em repouso).';
COMMENT ON COLUMN calendario.clerigos.endereco_uf IS 'Unidade Federativa (UF) do endereço residencial do clérigo (2 letras).';
