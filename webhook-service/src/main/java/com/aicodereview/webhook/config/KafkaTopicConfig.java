package com.aicodereview.webhook.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic prEventsTopic() {
        return TopicBuilder.name("pr-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic codeAnalysisTasksTopic() {
        return TopicBuilder.name("code-analysis-tasks")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reviewTasksTopic() {
        return TopicBuilder.name("review-tasks")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reviewResultsTopic() {
        return TopicBuilder.name("review-results")
                .partitions(3)
                .replicas(1)
                .build();
    }
}