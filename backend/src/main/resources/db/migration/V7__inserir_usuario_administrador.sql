-- ============================================
-- Usuário administrador inicial
-- E-mail: admin@gmail.com
-- Senha:  Admin@123
-- ============================================

-- Cria a conta só se o e-mail ainda não estiver cadastrado.
INSERT INTO USUARIO (ID, EMAIL, SENHA_HASH, CRIADO_EM)
SELECT '00000000-0000-0000-0000-000000000001',
       'admin@gmail.com',
       '$2a$10$bRMN.Maj3cTrT7vA4HmMDejy1W3MOObBBBsIHuA2JmVuB.3micE4G',
       SYSTIMESTAMP
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM USUARIO WHERE LOWER(EMAIL) = 'admin@gmail.com');

-- Garante as roles USUARIO e ADMINISTRADOR (também se a conta já existia).
INSERT INTO USUARIO_ROLE (ID_USUARIO, ID_ROLE)
SELECT U.ID, R.ID
  FROM USUARIO U
  JOIN ROLE R ON R.NOME IN ('USUARIO', 'ADMINISTRADOR')
 WHERE LOWER(U.EMAIL) = 'admin@gmail.com'
   AND NOT EXISTS (
       SELECT 1 FROM USUARIO_ROLE UR WHERE UR.ID_USUARIO = U.ID AND UR.ID_ROLE = R.ID
   );
