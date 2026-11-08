-- =====================================================================
-- Carga inicial da tabela LINHA_STATUS
-- Insere um registro por linha existente, com status NORMAL.
-- =====================================================================

INSERT INTO LINHA_STATUS (ID_LINHA, STATUS, DESCRICAO, ORIGEM, ATUALIZADO_EM)
SELECT ID, 'NORMAL', NULL, 'ADMINISTRADOR', SYSTIMESTAMP
FROM LINHA_TRANSPORTE;
