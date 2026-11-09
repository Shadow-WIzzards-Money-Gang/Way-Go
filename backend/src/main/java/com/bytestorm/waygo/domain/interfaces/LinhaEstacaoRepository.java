package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;

import com.bytestorm.waygo.domain.entity.LinhaEstacao;

public interface LinhaEstacaoRepository {

	List<LinhaEstacao> listarTodas();

	List<LinhaEstacao> listarPorLinhaOrdenadoPorSequencia(Long linhaId);

	List<LinhaEstacao> listarPorEstacao(Long estacaoId);

	List<LinhaEstacao> buscarPorIds(Collection<Long> ids);

}
