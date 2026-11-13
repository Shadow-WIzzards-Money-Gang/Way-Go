package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.valueobject.Pagina;

public interface EstacaoRepository {

	List<Estacao> listarTodas();

	Pagina<Estacao> listarPaginado(String busca, boolean somenteIntegracoes, int pagina, int tamanho);

	Optional<Estacao> buscarPorId(Long id);

	List<Estacao> buscarPorIds(Collection<Long> ids);

	boolean existePorId(Long id);

}
