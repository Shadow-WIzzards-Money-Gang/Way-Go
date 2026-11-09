package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaStatusEntity;

public interface LinhaStatusJpaRepository extends JpaRepository<LinhaStatusEntity, Long> {

	Optional<LinhaStatusEntity> findFirstByLinhaIdOrderByAtualizadoEmDescIdDesc(Long linhaId);

}
