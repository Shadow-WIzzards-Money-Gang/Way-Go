package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.infrastructure.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

	@Query("select u from UsuarioEntity u where lower(u.email) = :email")
	Optional<UsuarioEntity> findByEmailNormalizado(@Param("email") String emailEmMinusculas);

	@Query("select count(u) > 0 from UsuarioEntity u where lower(u.email) = :email")
	boolean existsByEmailNormalizado(@Param("email") String emailEmMinusculas);

}
