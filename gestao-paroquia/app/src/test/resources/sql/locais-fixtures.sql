DELETE
FROM calendario.eventos;
DELETE
FROM calendario.locais;

INSERT INTO calendario.locais (id, nome, tipo, capacidade, ativo)
VALUES ('00000000-0000-0000-0000-000000000010', 'Salão Paroquial', 'SALAO', 200, TRUE);
