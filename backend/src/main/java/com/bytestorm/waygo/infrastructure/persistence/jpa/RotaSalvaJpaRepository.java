package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.RotaSalvaEntity;

public interface RotaSalvaJpaRepository extends JpaRepository<RotaSalvaEntity, Long> {

	Optional<RotaSalvaEntity> findByIdAndIdUsuario(Long id, UUID idUsuario);

	List<RotaSalvaEntity> findByIdUsuarioOrderByCriadoEmDesc(UUID idUsuario);

}
