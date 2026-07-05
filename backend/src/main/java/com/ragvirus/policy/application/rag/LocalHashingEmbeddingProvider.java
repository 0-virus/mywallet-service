package com.ragvirus.policy.application.rag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "policy.rag.embedding-provider", havingValue = "local", matchIfMissing = true)
public class LocalHashingEmbeddingProvider implements TextEmbeddingProvider {

    private static final int DIMENSIONS = 128;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[가-힣A-Za-z0-9]+");

    @Override
    public String modelName() {
        return "local-hashing-v1";
    }

    @Override
    public double[] embed(String text) {
        double[] vector = new double[DIMENSIONS];
        for (String token : tokenize(text)) {
            int index = Math.floorMod(hash(token), DIMENSIONS);
            vector[index] += 1.0d;
        }
        normalize(vector);
        return vector;
    }

    private List<String> tokenize(String text) {
        String normalized = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        Matcher matcher = TOKEN_PATTERN.matcher(normalized);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            tokens.add(token);
            if (token.length() >= 4 && token.chars().anyMatch(this::isKorean)) {
                addKoreanNgrams(token, tokens);
            }
        }
        return tokens;
    }

    private void addKoreanNgrams(String token, List<String> tokens) {
        for (int size = 2; size <= 3; size++) {
            for (int i = 0; i <= token.length() - size; i++) {
                tokens.add(token.substring(i, i + size));
            }
        }
    }

    private boolean isKorean(int codePoint) {
        return codePoint >= '가' && codePoint <= '힣';
    }

    private int hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return ((bytes[0] & 0xff) << 24)
                    | ((bytes[1] & 0xff) << 16)
                    | ((bytes[2] & 0xff) << 8)
                    | (bytes[3] & 0xff);
        } catch (Exception ex) {
            return token.hashCode();
        }
    }

    private void normalize(double[] vector) {
        double sum = 0.0d;
        for (double value : vector) {
            sum += value * value;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0.0d) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
