package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.application.dto.response.UsuarioResponse;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;
import com.bytestorm.waygo.domain.valueobject.Pagina;

class UsuarioServiceTest {

	private final Usuarios usuarios = new Usuarios();
	private final UsuarioService service = new UsuarioService(usuarios);

	private static class Usuarios implements UsuarioRepository {

		final List<Usuario> todos = new ArrayList<>();

		Usuario criar(String email, int dia, Role... roles) {
			return salvar(Usuario.reconstruir(UUID.randomUUID(), email, "hash", Set.of(roles),
					LocalDateTime.of(2026, 1, dia, 10, 0)));
		}

		@Override
		public Usuario salvar(Usuario usuario) {
			todos.add(usuario);
			return usuario;
		}

		@Override
		public Optional<Usuario> buscarPorId(UUID id) {
			return todos.stream().filter(u -> u.getId().equals(id)).findFirst();
		}

		@Override
		public Optional<Usuario> buscarPorEmail(String email) {
			return todos.stream().filter(u -> u.possuiEmail(email)).findFirst();
		}

		@Override
		public boolean existePorEmail(String email) {
			return buscarPorEmail(email).isPresent();
		}

		@Override
		public Pagina<Usuario> listarPaginado(int pagina, int tamanho) {
			List<Usuario> ordenados = todos.stream()
					.sorted(Comparator.comparing(Usuario::getCriadoEm).reversed())
					.toList();
			int inicio = Math.min(pagina * tamanho, ordenados.size());
			int fim = Math.min(inicio + tamanho, ordenados.size());
			return new Pagina<>(ordenados.subList(inicio, fim), pagina, tamanho, ordenados.size());
		}

	}

	@Test
	void listaPaginadoSemExporASenha() {
		usuarios.criar("ana@waygo.test", 1, Role.USUARIO);
		usuarios.criar("bruno@waygo.test", 2, Role.USUARIO, Role.ADMINISTRADOR);
		usuarios.criar("caio@waygo.test", 3, Role.USUARIO);

		PaginaResponse<UsuarioResponse> pagina = service.listar(0, 2);

		assertThat(pagina.totalItens()).isEqualTo(3);
		assertThat(pagina.totalPaginas()).isEqualTo(2);
		assertThat(pagina.conteudo()).extracting(UsuarioResponse::email)
				.containsExactly("caio@waygo.test", "bruno@waygo.test");
		assertThat(pagina.conteudo().get(1).roles()).containsExactly("ADMINISTRADOR", "USUARIO");
	}

	@Test
	void validaPaginaETamanho() {
		assertThatThrownBy(() -> service.listar(-1, 20)).isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.listar(0, 0)).isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.listar(0, 101)).isInstanceOf(RequisicaoInvalidaException.class);
	}

	@Test
	void buscaAContaDoUsuario() {
		Usuario ana = usuarios.criar("ana@waygo.test", 1, Role.USUARIO);

		UsuarioResponse eu = service.buscar(ana.getId());

		assertThat(eu.email()).isEqualTo("ana@waygo.test");
		assertThat(eu.roles()).containsExactly("USUARIO");
		assertThat(eu.criadoEm()).isEqualTo(ana.getCriadoEm());
		assertThatThrownBy(() -> service.buscar(UUID.randomUUID()))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

}
