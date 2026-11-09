package com.bytestorm.waygo.infrastructure.persistence.converter;

import com.bytestorm.waygo.domain.enums.TipoTransporte;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoTransporteConverter implements AttributeConverter<TipoTransporte, String> {

	@Override
	public String convertToDatabaseColumn(TipoTransporte tipo) {
		return tipo == null ? null : tipo.getValor();
	}

	@Override
	public TipoTransporte convertToEntityAttribute(String valor) {
		return valor == null ? null : TipoTransporte.deValor(valor);
	}

}
