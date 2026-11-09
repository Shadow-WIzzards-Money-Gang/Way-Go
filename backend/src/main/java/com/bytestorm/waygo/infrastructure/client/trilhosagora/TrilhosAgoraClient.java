package com.bytestorm.waygo.infrastructure.client.trilhosagora;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.bytestorm.waygo.infrastructure.client.trilhosagora.dto.TrilhosAgoraStatusResponse;

@FeignClient(name = "trilhos-agora", url = "${trilhos-agora.url}", configuration = TrilhosAgoraFeignConfig.class)
public interface TrilhosAgoraClient {

	@GetMapping("/status")
	TrilhosAgoraStatusResponse buscarStatus();

}
