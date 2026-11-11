package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import com.bytestorm.waygo.domain.util.Textos;
import com.bytestorm.waygo.domain.valueobject.Coordenada;

import lombok.Getter;

@Getter
public class Estacao {

	private static final int TAMANHO_NOME = 150;

	private final Long id;
	private String nome;
	private Coordenada coordenada;
	private boolean ativa;

	private Estacao(Long id, String nome, Coordenada coordenada, boolean ativa) {
		this.id = id;
		this.nome = validarNome(nome);
		this.coordenada = coordenada;
		this.ativa = ativa;
	}

	public static Estacao criar(String nome, Coordenada coordenada) {
		return reconstruir(null, nome, coordenada, true);
	}

	public static Estacao reconstruir(Long id, String nome, Coordenada coordenada, boolean ativa) {
		return new Estacao(id, nome, coordenada, ativa);
	}

	public void ativar() {
		this.ativa = true;
	}

	public void desativar() {
		this.ativa = false;
	}

	public void renomear(String novoNome) {
		this.nome = validarNome(novoNome);
	}

	public void relocalizar(Coordenada novaCoordenada) {
		this.coordenada = exigir(novaCoordenada, "coordenada");
	}

	public boolean possuiCoordenada() {
		return coordenada != null;
	}

	public boolean possuiNome(String outroNome) {
		return Textos.normalizar(nome).equals(Textos.normalizar(outroNome));
	}

	private static String validarNome(String nome) {
		return exigirTexto(nome, "nome", TAMANHO_NOME);
	}

}
