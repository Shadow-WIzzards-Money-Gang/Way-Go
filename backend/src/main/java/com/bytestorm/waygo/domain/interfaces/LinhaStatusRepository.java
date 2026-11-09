package com.bytestorm.waygo.domain.interfaces;

import java.util.List;
import java.util.Optional;

import com.bytestorm.waygo.domain.entity.LinhaStatus;

public interface LinhaStatusRepository {

	Optional<LinhaStatus> buscarAtualPorLinha(Long linhaId);

	List<LinhaStatus> buscarAtuaisDeTodasAsLinhas();

	LinhaStatus salvar(LinhaStatus linhaStatus);

}
