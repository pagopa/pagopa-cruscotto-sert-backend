package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

class MailServiceTest {

    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final MessageSource messageSource = mock(MessageSource.class);
    private final SpringTemplateEngine templateEngine = mock(SpringTemplateEngine.class);
    private final MailService service = new MailService(sender, messageSource, templateEngine);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "from", "noreply@example.org");
        ReflectionTestUtils.setField(service, "baseUrl", "https://app.example.org");
        when(sender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    void sendsPlainAndHtmlMessagesWithConfiguredFromAddress() throws Exception {
        service.sendEmail("user@example.org", "Plain", "text body", false, false);
        service.sendEmail("user@example.org", "HTML", "<b>body</b>", true, true);

        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender, org.mockito.Mockito.times(2)).send(captor.capture());
        MimeMessage plain = captor.getAllValues().get(0);
        MimeMessage html = captor.getAllValues().get(1);
        assertThat(plain.getSubject()).isEqualTo("Plain");
        assertThat(plain.getFrom()[0].toString()).isEqualTo("noreply@example.org");
        assertThat(plain.getAllRecipients()[0].toString()).isEqualTo("user@example.org");
        assertThat(html.getSubject()).isEqualTo("HTML");
        assertThat(html.getContent()).isNotNull();
    }

    @Test
    void rendersAndSendsTemplateForUserWithEmail() throws Exception {
        AuthUser user = new AuthUser();
        user.setEmail("user@example.org");
        user.setLangKey("it");
        when(templateEngine.process(eq("mail/welcome"), any(IContext.class))).thenReturn("rendered body");
        when(messageSource.getMessage("email.welcome.title", null, Locale.ITALIAN)).thenReturn("Welcome");

        service.sendEmailFromTemplate(user, "mail/welcome", "email.welcome.title");

        verify(templateEngine).process(eq("mail/welcome"), any(IContext.class));
        verify(messageSource).getMessage("email.welcome.title", null, Locale.ITALIAN);
        var captor = org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Welcome");
        assertThat(captor.getValue().getContent()).isEqualTo("rendered body");
    }

    @Test
    void skipsTemplateWhenUserHasNoEmailAndSuppressesMailTransportFailure() {
        AuthUser user = new AuthUser();
        user.setLogin("user");
        service.sendEmailFromTemplate(user, "mail/welcome", "email.welcome.title");
        verifyNoTemplateOrMailInteractions();

        org.mockito.Mockito.doThrow(new MailSendException("transport unavailable")).when(sender).send(any(MimeMessage.class));
        service.sendEmail("user@example.org", "subject", "body", false, false);
    }

    private void verifyNoTemplateOrMailInteractions() {
        verify(templateEngine, never()).process(any(String.class), any(IContext.class));
        verify(messageSource, never()).getMessage(any(String.class), any(), any(Locale.class));
        verify(sender, never()).send(any(MimeMessage.class));
    }
}