package com.bytestorm.waygo.domain.valueobject;

import java.util.Objects;

import com.bytestorm.waygo.domain.enums.StatusOperacional;

public record StatusLinhaExterno(String codigoLinha, StatusOperacional status, String descricao) {

	public StatusLinhaExterno {
		Objects.requireNonNull(codigoLinha, "codigoLinha é obrigatório");
		Objects.requireNonNull(status, "status é obrigatório");
	}

}
