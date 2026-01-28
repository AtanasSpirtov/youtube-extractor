package com.youtube;

import com.youtube.config.EmbeddingProperties;
import com.youtube.config.ModelProperties;
import com.youtube.config.RagSystemClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {ModelProperties.class, RagSystemClientProperties.class, EmbeddingProperties.class})
public class YoutubeExtractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeExtractorApplication.class, args);
    }

}
