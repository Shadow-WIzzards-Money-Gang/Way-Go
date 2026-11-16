package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.interfaces.DispositivoUsuarioRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.DispositivoUsuarioJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DispositivoUsuarioRepositoryAdapter implements DispositivoUsuarioRepository {

	private final DispositivoUsuarioJpaRepository jpaRepository;

	@Override
	@Transactional
	public DispositivoUsuario salvar(DispositivoUsuario dispositivo) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(dispositivo)));
	}

	@Override
	public Optional<DispositivoUsuario> buscarPorToken(String tokenFcm) {
		return jpaRepository.findByTokenFcm(tokenFcm).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<DispositivoUsuario> listarAtivosDoUsuario(UUID idUsuario) {
		return jpaRepository.findByIdUsuarioAndAtivoTrue(idUsuario).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public Set<UUID> listarUsuariosComAlertasStatus() {
		return Set.copyOf(jpaRepository.findUsuariosComAlertasStatus());
	}

	@Override
	@Transactional
	public void excluirPorTokenEUsuario(String tokenFcm, UUID idUsuario) {
		jpaRepository.excluirPorTokenEUsuario(tokenFcm, idUsuario);
	}

}
