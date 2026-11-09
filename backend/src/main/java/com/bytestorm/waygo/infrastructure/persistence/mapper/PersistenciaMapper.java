package com.bytestorm.waygo.infrastructure.persistence.mapper;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.valueobject.Coordenada;
import com.bytestorm.waygo.infrastructure.persistence.entity.BaldeacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.ConexaoEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaStatusEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaTransporteEntity;

public final class PersistenciaMapper {

	private PersistenciaMapper() {
	}

	public static LinhaTransporte paraDominio(LinhaTransporteEntity entity) {
		return new LinhaTransporte(entity.getId(), entity.getNome(), entity.getCodigo(), entity.getCor(),
				entity.getOperadora(), entity.getTipoTransporte(), entity.isAtiva());
	}

	public static Estacao paraDominio(EstacaoEntity entity) {
		Coordenada coordenada = entity.getLatitude() != null && entity.getLongitude() != null
				? new Coordenada(entity.getLatitude(), entity.getLongitude())
				: null;
		return new Estacao(entity.getId(), entity.getNome(), coordenada, entity.isAtiva());
	}

	public static LinhaEstacao paraDominio(LinhaEstacaoEntity entity) {
		return new LinhaEstacao(entity.getId(), entity.getLinhaId(), entity.getEstacaoId(), entity.getSequencia(),
				entity.isAtiva());
	}

	public static ConexaoEstacao paraDominio(ConexaoEstacaoEntity entity) {
		return new ConexaoEstacao(entity.getId(), entity.getLinhaEstacaoOrigemId(), entity.getLinhaEstacaoDestinoId(),
				entity.getTempoViagemSegundos(), entity.getDistanciaMetros(), entity.isAtiva());
	}

	public static Baldeacao paraDominio(BaldeacaoEntity entity) {
		return new Baldeacao(entity.getId(), entity.getLinhaEstacaoOrigemId(), entity.getLinhaEstacaoDestinoId(),
				entity.getTempoBaldeacaoSegundos(), entity.isAtiva());
	}

	public static LinhaStatus paraDominio(LinhaStatusEntity entity) {
		return new LinhaStatus(entity.getId(), entity.getLinhaId(), entity.getStatus(), entity.getDescricao(),
				entity.getOrigem(), entity.getAtualizadoEm());
	}

	public static LinhaStatusEntity paraEntity(LinhaStatus linhaStatus) {
		return new LinhaStatusEntity(linhaStatus.getLinhaId(), linhaStatus.getStatus(), linhaStatus.getDescricao(),
				linhaStatus.getOrigem(), linhaStatus.getAtualizadoEm());
	}

}
