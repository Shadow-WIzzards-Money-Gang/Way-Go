package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.bytestorm.waygo.domain.entity.LinhaTransporte;

public interface LinhaTransporteRepository {

	List<LinhaTransporte> listarTodas();

	Optional<LinhaTransporte> buscarPorId(Long id);

	List<LinhaTransporte> buscarPorIds(Collection<Long> ids);

	boolean existePorId(Long id);

	boolean existePorCodigo(String codigo);

	LinhaTransporte salvar(LinhaTransporte linha);

}
