package com.medic.Web.service.mail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
public class MailTemplateService {

    public Mono<String> forgotPassword(String nome, String codigo) {

        Resource resource = new ClassPathResource("templates/forgot-password.html");

        return DataBufferUtils
                .read(resource, new DefaultDataBufferFactory(), 4096)
                .reduce(new StringBuilder(), (builder, dataBuffer) -> {

                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);

                    DataBufferUtils.release(dataBuffer);

                    return builder.append(new String(bytes, StandardCharsets.UTF_8));
                })
                .map(StringBuilder::toString)
                .map(s -> s.replace("{nome}", nome).replace("{codigo}", codigo));
    }
}
