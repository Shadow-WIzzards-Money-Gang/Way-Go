package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.domain.interfaces.NotificacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.NotificacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificacaoRepositoryAdapter implements NotificacaoRepository {

	private final NotificacaoJpaRepository jpaRepository;

	/** A UK_NOT_USUARIO_CHAVE garante a unicidade mesmo se duas threads gravarem ao mesmo tempo. */
	@Override
	public Optional<Notificacao> salvarSeNova(Notificacao notificacao) {
		if (existe(notificacao.getIdUsuario(), notificacao.getChave())) {
			return Optional.empty();
		}
		try {
			return Optional.of(PersistenciaMapper.paraDominio(
					jpaRepository.saveAndFlush(PersistenciaMapper.paraEntity(notificacao))));
		} catch (DataIntegrityViolationException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean existe(UUID idUsuario, String chave) {
		return jpaRepository.existsByIdUsuarioAndChave(idUsuario, chave);
	}

	@Override
	public List<Notificacao> listarProntasParaEnvio(LocalDateTime agora, int limite) {
		return jpaRepository.findProntas(StatusNotificacao.PENDENTE, agora, PageRequest.of(0, limite)).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	@Transactional
	public Notificacao salvar(Notificacao notificacao) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(notificacao)));
	}

}
