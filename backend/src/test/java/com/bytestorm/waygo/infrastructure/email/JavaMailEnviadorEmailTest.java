package com.bytestorm.waygo.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import com.bytestorm.waygo.infrastructure.config.EmailProperties;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

class JavaMailEnviadorEmailTest {

	private final JavaMailSender mailSender = mock(JavaMailSender.class);

	private JavaMailEnviadorEmail criar(boolean habilitado) {
		given(mailSender.createMimeMessage())
				.willAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
		return new JavaMailEnviadorEmail(mailSender,
				new EmailProperties(habilitado, "waygo@gmail.com", "WayGo"));
	}

	@Test
	void enviaHtmlDeBoasVindasParaODestinatario() throws Exception {
		criar(true).enviarBoasVindas("ana@email.com");

		ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(captor.capture());
		MimeMessage mensagem = captor.getValue();
		mensagem.saveChanges();

		assertThat(mensagem.getSubject()).isEqualTo(JavaMailEnviadorEmail.ASSUNTO_BOAS_VINDAS);
		assertThat(mensagem.getRecipients(Message.RecipientType.TO))
				.containsExactly(new InternetAddress("ana@email.com"));
		InternetAddress remetente = (InternetAddress) mensagem.getFrom()[0];
		assertThat(remetente.getAddress()).isEqualTo("waygo@gmail.com");
		assertThat(remetente.getPersonal()).isEqualTo("WayGo");
		assertThat(textoDe(mensagem)).contains("ana@email.com").doesNotContain("{{email}}");
	}

	@Test
	void enviaConfirmacaoDeContaExcluida() throws Exception {
		criar(true).enviarContaExcluida("ana@email.com");

		ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(captor.capture());
		MimeMessage mensagem = captor.getValue();
		mensagem.saveChanges();

		assertThat(mensagem.getSubject()).isEqualTo(JavaMailEnviadorEmail.ASSUNTO_CONTA_EXCLUIDA);
		assertThat(mensagem.getRecipients(Message.RecipientType.TO))
				.containsExactly(new InternetAddress("ana@email.com"));
		assertThat(textoDe(mensagem)).contains("ana@email.com", "excluída").doesNotContain("{{email}}");
	}

	@Test
	void desabilitadoNaoEnvia() {
		new JavaMailEnviadorEmail(mailSender, new EmailProperties(false, "waygo@gmail.com", "WayGo"))
				.enviarBoasVindas("ana@email.com");

		verifyNoInteractions(mailSender);
	}

	@Test
	void falhaNoSmtpNaoPropaga() {
		JavaMailEnviadorEmail enviador = criar(true);
		willThrow(new MailSendException("SMTP fora do ar")).given(mailSender).send(any(MimeMessage.class));

		assertThatCode(() -> enviador.enviarBoasVindas("ana@email.com")).doesNotThrowAnyException();
	}

	/** Procura o corpo HTML dentro das partes da mensagem. */
	private static String textoDe(Object conteudo) throws Exception {
		if (conteudo instanceof MimeMessage mensagem) {
			return textoDe(mensagem.getContent());
		}
		if (conteudo instanceof MimeMultipart multipart) {
			StringBuilder texto = new StringBuilder();
			for (int i = 0; i < multipart.getCount(); i++) {
				texto.append(textoDe(multipart.getBodyPart(i).getContent()));
			}
			return texto.toString();
		}
		return String.valueOf(conteudo);
	}

}
