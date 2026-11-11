package com.bytestorm.waygo.infrastructure.persistence.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.valueobject.Coordenada;
import com.bytestorm.waygo.infrastructure.persistence.entity.BaldeacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.ConexaoEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaStatusEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaTransporteEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.RoleEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.UsuarioEntity;

public final class PersistenciaMapper {

	private PersistenciaMapper() {
	}

	public static LinhaTransporte paraDominio(LinhaTransporteEntity entity) {
		return LinhaTransporte.reconstruir(entity.getId(), entity.getNome(), entity.getCodigo(), entity.getCor(),
				entity.getOperadora(), entity.getTipoTransporte(), entity.isAtiva());
	}

	public static Estacao paraDominio(EstacaoEntity entity) {
		Coordenada coordenada = entity.getLatitude() != null && entity.getLongitude() != null
				? new Coordenada(entity.getLatitude(), entity.getLongitude())
				: null;
		return Estacao.reconstruir(entity.getId(), entity.getNome(), coordenada, entity.isAtiva());
	}

	public static LinhaEstacao paraDominio(LinhaEstacaoEntity entity) {
		return LinhaEstacao.reconstruir(entity.getId(), entity.getLinhaId(), entity.getEstacaoId(), entity.getSequencia(),
				entity.isAtiva());
	}

	public static ConexaoEstacao paraDominio(ConexaoEstacaoEntity entity) {
		return ConexaoEstacao.reconstruir(entity.getId(), entity.getLinhaEstacaoOrigemId(), entity.getLinhaEstacaoDestinoId(),
				entity.getTempoViagemSegundos(), entity.getDistanciaMetros(), entity.isAtiva());
	}

	public static Baldeacao paraDominio(BaldeacaoEntity entity) {
		return Baldeacao.reconstruir(entity.getId(), entity.getLinhaEstacaoOrigemId(), entity.getLinhaEstacaoDestinoId(),
				entity.getTempoBaldeacaoSegundos(), entity.isAtiva());
	}

	public static LinhaStatus paraDominio(LinhaStatusEntity entity) {
		return LinhaStatus.reconstruir(entity.getId(), entity.getLinhaId(), entity.getStatus(), entity.getDescricao(),
				entity.getOrigem(), entity.getAtualizadoEm());
	}

	public static LinhaStatusEntity paraEntity(LinhaStatus linhaStatus) {
		return new LinhaStatusEntity(linhaStatus.getLinhaId(), linhaStatus.getStatus(), linhaStatus.getDescricao(),
				linhaStatus.getOrigem(), linhaStatus.getAtualizadoEm());
	}

	public static Usuario paraDominio(UsuarioEntity entity) {
		return Usuario.reconstruir(entity.getId(), entity.getEmail(), entity.getSenhaHash(),
				entity.getRoles().stream().map(RoleEntity::getNome).collect(Collectors.toSet()), entity.getCriadoEm());
	}

	public static UsuarioEntity paraEntity(Usuario usuario, Set<RoleEntity> roles) {
		return new UsuarioEntity(usuario.getId(), usuario.getEmail(), usuario.getSenhaHash(), usuario.getCriadoEm(),
				roles);
	}

}
