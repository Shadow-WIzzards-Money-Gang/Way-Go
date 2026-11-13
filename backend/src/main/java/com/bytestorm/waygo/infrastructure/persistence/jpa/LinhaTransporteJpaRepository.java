package com.bytestorm.waygo.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaTransporteEntity;

public interface LinhaTransporteJpaRepository extends JpaRepository<LinhaTransporteEntity, Long> {

	boolean existsByCodigoIgnoreCase(String codigo);

}
