package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.infrastructure.persistence.entity.RoleEntity;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

	List<RoleEntity> findByNomeIn(Collection<Role> nomes);

}
