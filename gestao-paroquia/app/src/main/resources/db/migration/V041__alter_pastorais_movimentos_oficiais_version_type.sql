-- Alter version column type to BIGINT to match JPA entity Long type
ALTER TABLE calendario.pastorais_movimentos_oficiais ALTER COLUMN version TYPE BIGINT;
