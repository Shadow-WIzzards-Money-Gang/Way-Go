package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaStatusEntity;

public interface LinhaStatusJpaRepository extends JpaRepository<LinhaStatusEntity, Long> {

	Optional<LinhaStatusEntity> findFirstByLinhaIdOrderByIdDesc(Long linhaId);

	@Query("""
			select s from LinhaStatusEntity s
			where s.id in (select max(s2.id) from LinhaStatusEntity s2 group by s2.linhaId)
			""")
	List<LinhaStatusEntity> findMaisRecentePorLinha();

}
