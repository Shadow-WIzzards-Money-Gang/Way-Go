package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;
import com.bytestorm.waygo.domain.valueobject.Pagina;
import com.bytestorm.waygo.infrastructure.persistence.entity.RoleEntity;
import com.bytestorm.waygo.infrastructure.persistence.entity.UsuarioEntity;
import com.bytestorm.waygo.infrastructure.persistence.jpa.RoleJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

	private final UsuarioJpaRepository jpaRepository;
	private final RoleJpaRepository roleJpaRepository;

	@Override
	@Transactional
	public Usuario salvar(Usuario usuario) {
		Set<RoleEntity> roles = buscarRoles(usuario.getRoles());
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(usuario, roles)));
	}

	@Override
	public Optional<Usuario> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public Optional<Usuario> buscarPorEmail(String email) {
		if (email == null || email.isBlank()) {
			return Optional.empty();
		}
		return jpaRepository.findByEmailNormalizado(normalizar(email)).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public boolean existePorEmail(String email) {
		return email != null && !email.isBlank() && jpaRepository.existsByEmailNormalizado(normalizar(email));
	}

	@Override
	public Pagina<Usuario> listarPaginado(int pagina, int tamanho) {
		Page<UsuarioEntity> resultado = jpaRepository.findAll(
				PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm").and(Sort.by("id"))));
		return new Pagina<>(
				resultado.map(PersistenciaMapper::paraDominio).getContent(),
				resultado.getNumber(),
				resultado.getSize(),
				resultado.getTotalElements());
	}

	private Set<RoleEntity> buscarRoles(Set<Role> roles) {
		Set<RoleEntity> encontradas = Set.copyOf(roleJpaRepository.findByNomeIn(roles));
		if (encontradas.size() != roles.size()) {
			Set<Role> faltando = roles.stream()
					.filter(role -> encontradas.stream().noneMatch(e -> e.getNome() == role))
					.collect(Collectors.toSet());
			throw new IllegalStateException("Roles não cadastradas na tabela ROLE: " + faltando);
		}
		return encontradas;
	}

	private static String normalizar(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
