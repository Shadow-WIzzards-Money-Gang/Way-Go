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
	static final String ASSUNTO_CONTA_EXCLUIDA = "Sua conta WayGo foi excluída";
	private static final String TEMPLATE_BOAS_VINDAS = "templates/email/boas-vindas.html";
	private static final String TEMPLATE_CONTA_EXCLUIDA = "templates/email/conta-excluida.html";

	private final JavaMailSender mailSender;
	private final EmailProperties propriedades;
	private final String templateBoasVindas;
	private final String templateContaExcluida;

	public JavaMailEnviadorEmail(JavaMailSender mailSender, EmailProperties propriedades) {
		this.mailSender = mailSender;
		this.propriedades = propriedades;
		this.templateBoasVindas = carregarTemplate(TEMPLATE_BOAS_VINDAS);
		this.templateContaExcluida = carregarTemplate(TEMPLATE_CONTA_EXCLUIDA);
	}

	@Async
	@Override
	public void enviarBoasVindas(String destinatario) {
		enviar(destinatario, ASSUNTO_BOAS_VINDAS, templateBoasVindas, "boas-vindas");
	}

	@Async
	@Override
	public void enviarContaExcluida(String destinatario) {
		enviar(destinatario, ASSUNTO_CONTA_EXCLUIDA, templateContaExcluida, "conta excluída");
	}

	private void enviar(String destinatario, String assunto, String template, String tipo) {
		if (!propriedades.habilitado()) {
			log.info("Envio de e-mail desabilitado; e-mail de {} para {} não enviado", tipo, destinatario);
			return;
		}

		try {
			MimeMessage mensagem = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, StandardCharsets.UTF_8.name());
			helper.setFrom(propriedades.remetente(), propriedades.nomeRemetente());
			helper.setTo(destinatario);
			helper.setSubject(assunto);
			helper.setText(template.replace("{{email}}", HtmlUtils.htmlEscape(destinatario)), true);
			mailSender.send(mensagem);
			log.info("E-mail de {} enviado para {}", tipo, destinatario);
		} catch (MailException | MessagingException | IOException e) {
			log.warn("Falha ao enviar e-mail de {} para {}: {}", tipo, destinatario, e.getMessage());
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
