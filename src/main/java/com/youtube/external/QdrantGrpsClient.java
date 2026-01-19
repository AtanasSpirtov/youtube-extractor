package com.youtube.external;

import com.youtube.config.QdrantGrpcConfig;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Getter
public class QdrantGrpsClient {
    private final QdrantClient client;
    private final QdrantGrpcConfig config;

    public QdrantGrpsClient(QdrantGrpcConfig config) {
        this.config = config;
        this.client = new QdrantClient(QdrantGrpcClient
                .newBuilder(config.getHost(), config.getPort(), false)
                .withApiKey(config.getApiKey())
                .build()
        );
    }

    private void insertData() {

    }

    public Boolean healthCheck() throws ExecutionException, InterruptedException {
        return client.healthCheckAsync().get().isInitialized();
    }
}
