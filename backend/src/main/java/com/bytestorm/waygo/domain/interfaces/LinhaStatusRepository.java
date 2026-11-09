package com.bytestorm.waygo.domain.interfaces;

import java.util.Optional;

import com.bytestorm.waygo.domain.entity.LinhaStatus;

public interface LinhaStatusRepository {

	Optional<LinhaStatus> buscarAtualPorLinha(Long linhaId);

}
