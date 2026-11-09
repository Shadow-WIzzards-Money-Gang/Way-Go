package com.bytestorm.waygo.infrastructure.client.trilhosagora.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrilhosAgoraStatusResponse(
		List<LinhaTrilhosAgora> lines,
		@JsonProperty("fetched_at") OffsetDateTime fetchedAt,
		boolean stale) {

}
