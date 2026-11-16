package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.infrastructure.persistence.entity.TokenRecuperacaoSenhaEntity;

public interface TokenRecuperacaoSenhaJpaRepository extends JpaRepository<TokenRecuperacaoSenhaEntity, Long> {

	Optional<TokenRecuperacaoSenhaEntity> findFirstByIdUsuarioOrderByCriadoEmDescIdDesc(UUID idUsuario);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update TokenRecuperacaoSenhaEntity t set t.usadoEm = :agora "
			+ "where t.idUsuario = :idUsuario and t.usadoEm is null")
	int invalidarAtivos(@Param("idUsuario") UUID idUsuario, @Param("agora") LocalDateTime agora);

}
