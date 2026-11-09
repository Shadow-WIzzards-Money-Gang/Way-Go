package com.bytestorm.waygo.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;

public interface EstacaoJpaRepository extends JpaRepository<EstacaoEntity, Long> {

}
