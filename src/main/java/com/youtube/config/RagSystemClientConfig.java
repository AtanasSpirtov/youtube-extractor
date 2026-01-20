package com.youtube.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RagSystemClientConfig {

    @Bean
    RestClient ragRestClient(RagSystemClientProperties props) {
        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }
}
