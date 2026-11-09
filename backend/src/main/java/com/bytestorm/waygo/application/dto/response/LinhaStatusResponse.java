package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;

import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;

public record LinhaStatusResponse(
		Long linhaId,
		StatusOperacional status,
		String descricao,
		OrigemStatus origem,
		LocalDateTime atualizadoEm) {

	public static LinhaStatusResponse de(LinhaStatus status) {
		return new LinhaStatusResponse(status.getLinhaId(), status.getStatus(), status.getDescricao(),
				status.getOrigem(), status.getAtualizadoEm());
	}

}
