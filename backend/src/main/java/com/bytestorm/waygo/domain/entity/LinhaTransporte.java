package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;
import static com.bytestorm.waygo.domain.util.Validacoes.textoOpcional;

import java.util.regex.Pattern;

import com.bytestorm.waygo.domain.enums.TipoTransporte;

import lombok.Getter;

@Getter
public class LinhaTransporte {

	private static final int TAMANHO_NOME = 100;
	private static final int TAMANHO_CODIGO = 20;
	private static final int TAMANHO_OPERADORA = 100;
	private static final Pattern COR_HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");

	private final Long id;
	private final String codigo;
	private final TipoTransporte tipoTransporte;
	private String nome;
	private String cor;
	private String operadora;
	private boolean ativa;

	private LinhaTransporte(Long id, String nome, String codigo, String cor, String operadora,
			TipoTransporte tipoTransporte, boolean ativa) {
		this.id = id;
		this.codigo = exigirTexto(codigo, "codigo", TAMANHO_CODIGO);
		this.tipoTransporte = exigir(tipoTransporte, "tipoTransporte");
		this.nome = validarNome(nome);
		this.cor = validarCor(cor);
		this.operadora = validarOperadora(operadora);
		this.ativa = ativa;
	}

	public static LinhaTransporte criar(String nome, String codigo, String cor, String operadora,
			TipoTransporte tipoTransporte) {
		return reconstruir(null, nome, codigo, cor, operadora, tipoTransporte, true);
	}

	public static LinhaTransporte reconstruir(Long id, String nome, String codigo, String cor, String operadora,
			TipoTransporte tipoTransporte, boolean ativa) {
		return new LinhaTransporte(id, nome, codigo, cor, operadora, tipoTransporte, ativa);
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

	public void alterarCor(String novaCor) {
		this.cor = validarCor(novaCor);
	}

	public void alterarOperadora(String novaOperadora) {
		this.operadora = validarOperadora(novaOperadora);
	}

	public boolean possuiCodigo(String outroCodigo) {
		return outroCodigo != null && codigo.equalsIgnoreCase(outroCodigo.trim());
	}

	private static String validarNome(String nome) {
		return exigirTexto(nome, "nome", TAMANHO_NOME);
	}

	private static String validarOperadora(String operadora) {
		return exigirTexto(operadora, "operadora", TAMANHO_OPERADORA);
	}

	private static String validarCor(String cor) {
		String valor = textoOpcional(cor, "cor", 7);
		if (valor != null && !COR_HEX.matcher(valor).matches()) {
			throw new IllegalArgumentException("cor deve estar no formato #RRGGBB");
		}
		return valor;
	}

}
