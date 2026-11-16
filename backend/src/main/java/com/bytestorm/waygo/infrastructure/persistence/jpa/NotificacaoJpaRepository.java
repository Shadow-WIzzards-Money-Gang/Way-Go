package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.infrastructure.persistence.entity.NotificacaoEntity;

public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoEntity, Long> {

	boolean existsByIdUsuarioAndChave(UUID idUsuario, String chave);

	@Query("select n from NotificacaoEntity n where n.status = :status and n.proximaTentativaEm <= :agora "
			+ "order by n.proximaTentativaEm, n.id")
	List<NotificacaoEntity> findProntas(@Param("status") StatusNotificacao status,
			@Param("agora") LocalDateTime agora, Pageable pagina);

}
