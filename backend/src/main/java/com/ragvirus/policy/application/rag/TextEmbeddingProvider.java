package com.ragvirus.policy.application.rag;

public interface TextEmbeddingProvider {

    String modelName();

    double[] embed(String text);
}
