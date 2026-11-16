package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.infrastructure.persistence.entity.DispositivoUsuarioEntity;

public interface DispositivoUsuarioJpaRepository extends JpaRepository<DispositivoUsuarioEntity, Long> {

	Optional<DispositivoUsuarioEntity> findByTokenFcm(String tokenFcm);

	List<DispositivoUsuarioEntity> findByIdUsuarioAndAtivoTrue(UUID idUsuario);

	@Query("select distinct d.idUsuario from DispositivoUsuarioEntity d where d.ativo = true and d.alertasStatus = true")
	List<UUID> findUsuariosComAlertasStatus();

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from DispositivoUsuarioEntity d where d.tokenFcm = :token and d.idUsuario = :idUsuario")
	int excluirPorTokenEUsuario(@Param("token") String token, @Param("idUsuario") UUID idUsuario);

}
