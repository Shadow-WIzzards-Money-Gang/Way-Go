package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.bytestorm.waygo.domain.entity.Estacao;

public interface EstacaoRepository {

	List<Estacao> listarTodas();

	Optional<Estacao> buscarPorId(Long id);

	List<Estacao> buscarPorIds(Collection<Long> ids);

	boolean existePorId(Long id);

}
