package com.bytestorm.waygo.application.event;

import com.bytestorm.waygo.domain.enums.StatusOperacional;

public record StatusLinhaAlteradoEvent(Long linhaId, Long idLinhaStatus, StatusOperacional anterior,
		StatusOperacional novo, String descricao) {
}
