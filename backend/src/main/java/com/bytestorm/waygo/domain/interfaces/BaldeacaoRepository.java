package com.bytestorm.waygo.domain.interfaces;

import java.util.Collection;
import java.util.List;

import com.bytestorm.waygo.domain.entity.Baldeacao;

public interface BaldeacaoRepository {

	List<Baldeacao> listarAtivas();

	List<Baldeacao> listarPorOrigens(Collection<Long> linhaEstacaoIds);

}
