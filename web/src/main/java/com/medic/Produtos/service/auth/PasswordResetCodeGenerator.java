package com.medic.Produtos.service.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordResetCodeGenerator {

    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    public PasswordResetCodeGenerator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generate() {

        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public String hash(String code) {
        return passwordEncoder.encode(code);
    }

    public boolean valide(String code, String hash) {
        return passwordEncoder.matches(code, hash);
    }
}
