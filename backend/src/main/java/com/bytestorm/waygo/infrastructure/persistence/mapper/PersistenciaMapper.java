package com.bytestorm.waygo.infrastructure.persistence.mapper;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.valueobject.Coordenada;
import com.bytestorm.waygo.infrastructure.persistence.entity.BaldeacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.ConexaoEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaStatusEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaTransporteEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.RoleEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.RotaAgendadaEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.RotaSalvaEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.UsuarioEntity;

public final class PersistenciaMapper {

	private PersistenciaMapper() {
	}

	public static LinhaTransporte paraDominio(LinhaTransporteEntity entity) {
		return LinhaTransporte.reconstruir(entity.getId(), entity.getNome(), entity.getCodigo(), entity.getCor(),
				entity.getOperadora(), entity.getTipoTransporte(), entity.isAtiva());
	}

	public static LinhaTransporteEntity paraEntity(LinhaTransporte linha) {
		return new LinhaTransporteEntity(linha.getId(), linha.getNome(), linha.getCodigo(), linha.getCor(),
				linha.getOperadora(), linha.getTipoTransporte(), linha.isAtiva());
	}

	public static EstacaoEntity paraEntity(Estacao estacao) {
		Coordenada coordenada = estacao.getCoordenada();
		return new EstacaoEntity(estacao.getId(), estacao.getNome(),
				coordenada == null ? null : coordenada.latitude(),
				coordenada == null ? null : coordenada.longitude(),
				estacao.isAtiva());
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

	public static RotaSalva paraDominio(RotaSalvaEntity entity) {
		return RotaSalva.reconstruir(entity.getId(), entity.getIdEstacaoOrigem(), entity.getIdEstacaoDestino(),
				entity.getNome(), entity.getCriterio(), entity.getIdUsuario(), entity.getCriadoEm());
	}

	public static RotaSalvaEntity paraEntity(RotaSalva rotaSalva) {
		return new RotaSalvaEntity(rotaSalva.getId(), rotaSalva.getNome(), rotaSalva.getIdEstacaoOrigem(),
				rotaSalva.getIdEstacaoDestino(), rotaSalva.getCriterioRota(), rotaSalva.getIdUsuario(),
				rotaSalva.getCriadoEm());
	}

	public static RotaAgendada paraDominio(RotaAgendadaEntity entity) {
		return RotaAgendada.reconstruir(entity.getId(), entity.getIdRotaSalva(), entity.getHorarioPartidaMinutos(),
				entity.getAntecedenciaMinutos(), entity.isAtiva(), entity.getDiasSemana(), entity.getCriadoEm());
	}

	public static RotaAgendadaEntity paraEntity(RotaAgendada rotaAgendada) {
		return new RotaAgendadaEntity(rotaAgendada.getId(), rotaAgendada.getIdRotaSalva(),
				rotaAgendada.getHorarioPartidaMinutos(), rotaAgendada.getAntecedenciaMinutos(), rotaAgendada.isAtiva(),
				rotaAgendada.getCriadoEm(), new HashSet<>(rotaAgendada.getDiasSemana()));
	}

}
