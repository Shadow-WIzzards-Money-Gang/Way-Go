package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.interfaces.FonteStatusLinhas;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.domain.valueobject.StatusLinhaExterno;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtualizacaoStatusLinhaService {

	private final FonteStatusLinhas fonteStatusLinhas;
	private final LinhaTransporteRepository linhaRepository;
	private final LinhaStatusRepository linhaStatusRepository;
	private final Clock clock;

	@Transactional
	public int atualizar() {
		List<StatusLinhaExterno> statusExternos = fonteStatusLinhas.buscarStatusAtuais();

		Map<String, Long> linhaIdPorCodigo = linhaRepository.listarTodas().stream()
				.collect(Collectors.toMap(LinhaTransporte::getCodigo, LinhaTransporte::getId));
		Map<Long, LinhaStatus> statusAtualPorLinha = linhaStatusRepository.buscarAtuaisDeTodasAsLinhas().stream()
				.collect(Collectors.toMap(LinhaStatus::getLinhaId, Function.identity()));

		LocalDateTime agora = LocalDateTime.now(clock);
		int registrados = 0;
		for (StatusLinhaExterno externo : statusExternos) {
			Long linhaId = linhaIdPorCodigo.get(externo.codigoLinha());
			if (linhaId == null) {
				log.debug("Linha {} da fonte externa não está cadastrada; ignorando", externo.codigoLinha());
				continue;
			}

			LinhaStatus atual = statusAtualPorLinha.get(linhaId);
			if (atual != null && !atual.mudouPara(externo.status())) {
				continue;
			}

			linhaStatusRepository.salvar(new LinhaStatus(null, linhaId, externo.status(), externo.descricao(),
					OrigemStatus.API, agora));
			log.info("Linha {} mudou de status: {} -> {}", externo.codigoLinha(),
					atual == null ? "(sem status)" : atual.getStatus(), externo.status());
			registrados++;
		}

		log.info("Atualização de status concluída: {} de {} linhas mudaram de status", registrados,
				statusExternos.size());
		return registrados;
	}

}
