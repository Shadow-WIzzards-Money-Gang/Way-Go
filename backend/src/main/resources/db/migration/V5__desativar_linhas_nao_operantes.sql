-- ============================================
-- Desativa as linhas que ainda não estão em operação
-- ============================================

UPDATE LINHA_TRANSPORTE
   SET ATIVA = 0
 WHERE CODIGO IN ('16', '19', '20', '22', 'alphaville-c-limpo');
