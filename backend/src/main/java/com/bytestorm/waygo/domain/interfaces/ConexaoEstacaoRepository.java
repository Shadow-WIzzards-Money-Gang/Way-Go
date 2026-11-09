package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;

import com.bytestorm.waygo.domain.entity.ConexaoEstacao;

public interface ConexaoEstacaoRepository {

	List<ConexaoEstacao> listarAtivas();

	List<ConexaoEstacao> listarPorOrigens(Collection<Long> linhaEstacaoIds);

}
