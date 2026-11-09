package com.bytestorm.waygo.application.dto.response;

import java.time.LocalDateTime;

public record ErroResponse(
		int status,
		String message,
		LocalDateTime data) {
}
