package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.interfaces.FonteStatusLinhas;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.valueobject.StatusLinhaExterno;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtualizacaoStatusLinhaService {

	private final FonteStatusLinhas fonteStatusLinhas;
	private final LinhaStatusRepository linhaStatusRepository;
	private final StatusLinhasCache cache;
	private final Clock clock;

	public int atualizar() {
		List<StatusLinhaExterno> statusExternos = fonteStatusLinhas.buscarStatusAtuais();
		cache.carregarSeNecessario();

		LocalDateTime agora = LocalDateTime.now(clock);
		int registrados = 0;
		for (StatusLinhaExterno externo : statusExternos) {
			Optional<Long> linhaId = cache.linhaIdPorCodigo(externo.codigoLinha());
			if (linhaId.isEmpty()) {
				log.debug("Linha {} da fonte externa não está cadastrada; ignorando", externo.codigoLinha());
				continue;
			}

			Optional<StatusOperacional> atual = cache.statusAtual(linhaId.get());
			if (atual.isPresent() && atual.get() == externo.status()) {
				continue;
			}

			linhaStatusRepository.salvar(
					LinhaStatus.registrarPelaApi(linhaId.get(), externo.status(), externo.descricao(), agora));
			cache.registrar(linhaId.get(), externo.status());
			log.info("Linha {} mudou de status: {} -> {}", externo.codigoLinha(),
					atual.map(Enum::name).orElse("(sem status)"), externo.status());
			registrados++;
		}

		log.info("Atualização de status concluída: {} de {} linhas mudaram de status", registrados,
				statusExternos.size());
		return registrados;
	}

}
