package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.TokenRecuperacaoSenha;
import com.bytestorm.waygo.domain.interfaces.TokenRecuperacaoSenhaRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.TokenRecuperacaoSenhaJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRecuperacaoSenhaRepositoryAdapter implements TokenRecuperacaoSenhaRepository {

	private final TokenRecuperacaoSenhaJpaRepository jpaRepository;

	@Override
	@Transactional
	public TokenRecuperacaoSenha salvar(TokenRecuperacaoSenha token) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(token)));
	}

	@Override
	public Optional<TokenRecuperacaoSenha> buscarMaisRecenteDoUsuario(UUID idUsuario) {
		return jpaRepository.findFirstByIdUsuarioOrderByCriadoEmDescIdDesc(idUsuario)
				.map(PersistenciaMapper::paraDominio);
	}

	@Override
	@Transactional
	public void invalidarAtivosDoUsuario(UUID idUsuario, LocalDateTime agora) {
		jpaRepository.invalidarAtivos(idUsuario, agora);
	}

}
