package com.aicodereview.fetch.config;

import com.aicodereview.common.dto.PullRequestEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

        @Value("${spring.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Value("${spring.kafka.consumer.group-id}")
        private String groupId;

        @Bean
        public ConsumerFactory<String, PullRequestEvent> consumerFactory() {
                Map<String, Object> config = new HashMap<>();

                config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                                StringDeserializer.class);
                config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                                JsonDeserializer.class);
                config.put(JsonDeserializer.TRUSTED_PACKAGES,
                                "com.ai.codereview.*");
                config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
                config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                                PullRequestEvent.class.getName());

                return new DefaultKafkaConsumerFactory<>(config);
        }

        // @Bean
        // public ConcurrentKafkaListenerContainerFactory<String, PullRequestEvent>
        // kafkaListenerContainerFactory() {
        // ConcurrentKafkaListenerContainerFactory<String, PullRequestEvent> factory =
        // new ConcurrentKafkaListenerContainerFactory<>();
        // factory.setConsumerFactory(consumerFactory());
        // return factory;
        // }

        @Value("${spring.kafka.listener.concurrency:3}")
        private Integer concurrency;

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, PullRequestEvent> kafkaListenerContainerFactory() {

                ConcurrentKafkaListenerContainerFactory<String, PullRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

                factory.setConsumerFactory(consumerFactory());
                factory.setConcurrency(concurrency);

                return factory;
        }
}