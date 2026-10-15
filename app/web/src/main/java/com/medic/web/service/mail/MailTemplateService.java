package com.medic.web.service.mail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
public class MailTemplateService {

    private static final String APPLICATION_LINK_BLOCK_START = "<!-- application-link:start -->";
    private static final String APPLICATION_LINK_BLOCK_END = "<!-- application-link:end -->";

    public Mono<String> forgotPassword(String nome, String codigo) {

        return readTemplate("templates/forgot-password.html")
                .map(template -> template
                        .replace("{nome}", nome)
                        .replace("{codigo}", codigo));
    }

    public Mono<String> newUserAccess(String nome,
                                      String senha,
                                      String linkAplicacao) {

        return readTemplate("templates/new-user-access.html")
                .map(template -> {
                    String renderedTemplate = template
                            .replace("{nome}", nome)
                            .replace("{senha}", senha);

                    return renderApplicationLinkBlock(renderedTemplate, linkAplicacao);
                });
    }

    private String renderApplicationLinkBlock(String template, String linkAplicacao) {

        if (linkAplicacao == null || linkAplicacao.isBlank() || "N/A".equalsIgnoreCase(linkAplicacao.trim())) {
            int blockStart = template.indexOf(APPLICATION_LINK_BLOCK_START);
            int blockEnd = template.indexOf(APPLICATION_LINK_BLOCK_END, blockStart);

            if (blockStart < 0 || blockEnd < 0) {
                return template;
            }

            return template.substring(0, blockStart)
                    + template.substring(blockEnd + APPLICATION_LINK_BLOCK_END.length());
        }

        return template
                .replace("data-application-href=\"{linkAplicacao}\"", "href=\"" + linkAplicacao + "\"")
                .replace(APPLICATION_LINK_BLOCK_START, "")
                .replace(APPLICATION_LINK_BLOCK_END, "");
    }

    private Mono<String> readTemplate(String path) {

        Resource resource = new ClassPathResource(path);

        return DataBufferUtils
                .read(resource, new DefaultDataBufferFactory(), 4096)
                .reduce(new StringBuilder(), (builder, dataBuffer) -> {

                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);

                    DataBufferUtils.release(dataBuffer);

                    return builder.append(new String(bytes, StandardCharsets.UTF_8));
                })
                .map(StringBuilder::toString);
    }
}
