package com.medic.Web.service.mail;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class MailTemplateServiceTest {

    private final MailTemplateService service = new MailTemplateService();

    @Test
    void shouldRenderForgotPasswordTemplate() {

        StepVerifier.create(service.forgotPassword("Joao", "123456"))
                .expectNextMatches(body -> body.contains("Joao") && body.contains("123456"))
                .verifyComplete();
    }
}
