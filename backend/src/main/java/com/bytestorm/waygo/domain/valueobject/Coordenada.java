package com.bytestorm.waygo.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public record Coordenada(BigDecimal latitude, BigDecimal longitude) {

	private static final BigDecimal LATITUDE_MAXIMA = BigDecimal.valueOf(90);
	private static final BigDecimal LONGITUDE_MAXIMA = BigDecimal.valueOf(180);

	public Coordenada {
		Objects.requireNonNull(latitude, "latitude é obrigatória");
		Objects.requireNonNull(longitude, "longitude é obrigatória");
		if (latitude.abs().compareTo(LATITUDE_MAXIMA) > 0) {
			throw new IllegalArgumentException("latitude deve estar entre -90 e 90");
		}
		if (longitude.abs().compareTo(LONGITUDE_MAXIMA) > 0) {
			throw new IllegalArgumentException("longitude deve estar entre -180 e 180");
		}
	}

}
