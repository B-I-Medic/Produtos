package com.medic.Produtos.service.mail;

import com.medic.Produtos.config.mail.MailProperties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.test.StepVerifier;

import java.util.Properties;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private MailService service;

    @Test
    void shouldSendEmail() {

        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailProperties.username()).thenReturn("from@medic.com");

        StepVerifier.create(service.sendSimpleEmail("to@medic.com", "subject", "body"))
                .verifyComplete();

        verify(mailSender).send(message);
    }
}
