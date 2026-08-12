package by.shakhau.ps.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtService {

    private final PublicKey publicKey;

    public JwtService(SecurityProps securityProps) {
        this.publicKey = parsePublicKey(securityProps.getPublicKeyContent());
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private PublicKey parsePublicKey(String rsaPublicKey) {
        try {
            String cleanKey = rsaPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(cleanKey);
            var keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse public RSA key", e);
        }
    }
}
