package com.bytestorm.waygo.infrastructure.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.bytestorm.waygo.domain.interfaces.EnviadorEmail;
import com.bytestorm.waygo.infrastructure.config.EmailProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JavaMailEnviadorEmail implements EnviadorEmail {

	static final String ASSUNTO_BOAS_VINDAS = "Bem-vindo(a) ao WayGo!";
	private static final String TEMPLATE_BOAS_VINDAS = "templates/email/boas-vindas.html";

	private final JavaMailSender mailSender;
	private final EmailProperties propriedades;
	private final String templateBoasVindas;

	public JavaMailEnviadorEmail(JavaMailSender mailSender, EmailProperties propriedades) {
		this.mailSender = mailSender;
		this.propriedades = propriedades;
		this.templateBoasVindas = carregarTemplate(TEMPLATE_BOAS_VINDAS);
	}

	@Async
	@Override
	public void enviarBoasVindas(String destinatario) {
		if (!propriedades.habilitado()) {
			log.info("Envio de e-mail desabilitado; boas-vindas para {} não enviado", destinatario);
			return;
		}

		try {
			MimeMessage mensagem = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, StandardCharsets.UTF_8.name());
			helper.setFrom(propriedades.remetente(), propriedades.nomeRemetente());
			helper.setTo(destinatario);
			helper.setSubject(ASSUNTO_BOAS_VINDAS);
			helper.setText(templateBoasVindas.replace("{{email}}", HtmlUtils.htmlEscape(destinatario)), true);
			mailSender.send(mensagem);
			log.info("E-mail de boas-vindas enviado para {}", destinatario);
		} catch (MailException | MessagingException | IOException e) {
			log.warn("Falha ao enviar e-mail de boas-vindas para {}: {}", destinatario, e.getMessage());
		}
	}

	private static String carregarTemplate(String caminho) {
		try (InputStream entrada = new ClassPathResource(caminho).getInputStream()) {
			return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Template de e-mail não encontrado: " + caminho, e);
		}
	}

}
