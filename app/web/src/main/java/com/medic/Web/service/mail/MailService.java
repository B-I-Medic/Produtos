package com.medic.Web.service.mail;

import com.medic.Web.config.mail.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

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
