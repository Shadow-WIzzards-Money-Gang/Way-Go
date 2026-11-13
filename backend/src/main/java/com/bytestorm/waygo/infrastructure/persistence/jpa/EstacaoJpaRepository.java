package com.bytestorm.waygo.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;

public interface EstacaoJpaRepository extends JpaRepository<EstacaoEntity, Long> {

	String FILTRO = """
			where (:busca is null or cast(function('TRANSLATE', lower(e.nome),
					'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc') as String) like concat('%', cast(:busca as String), '%'))
			  and (:minimoLinhas = 0 or (
					select count(distinct le.linhaId) from LinhaEstacaoEntity le
					where le.estacaoId = e.id and le.ativa = true) >= :minimoLinhas)
			""";

	@Query(value = "select e from EstacaoEntity e " + FILTRO,
			countQuery = "select count(e) from EstacaoEntity e " + FILTRO)
	Page<EstacaoEntity> buscarPaginado(@Param("busca") String busca,
			@Param("minimoLinhas") int minimoLinhas, Pageable pageable);

}
