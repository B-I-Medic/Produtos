package com.medic.web.service.mail;

import com.medic.web.config.mail.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public MailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public Mono<Void> sendSimpleEmail(String to, String subject, String text) {

        return Mono.fromRunnable(() -> {

            MimeMessage message = mailSender.createMimeMessage();

            try {

                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(mailProperties.username());
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text, true);

                mailSender.send(message);

            } catch (MessagingException e) {

                throw new RuntimeException(e);
            }

        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
