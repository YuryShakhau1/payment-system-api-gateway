package by.shakhau.ps.gateway.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Getter
@Setter
public class SecurityProps {

    private Resource publicKey;

    public String getPublicKeyContent() {
        try {
            return publicKey.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read public key", e);
        }
    }
}

