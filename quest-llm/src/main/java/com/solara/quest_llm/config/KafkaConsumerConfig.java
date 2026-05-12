package com.solara.quest_llm.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.solara.quest_llm.models.kafkaDto.requests.EmbeddingRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, EmbeddingRequest> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "quest-embedding-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Use this constructor to avoid the "abstract" instantiation error
        JsonDeserializer<EmbeddingRequest> payloadDeserializer = new JsonDeserializer<>(EmbeddingRequest.class, false);
        payloadDeserializer.addTrustedPackages("*");

        // Manual Type Mapping to handle different packages
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("embedding_req", EmbeddingRequest.class);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setIdClassMapping(mappings);
        payloadDeserializer.setTypeMapper(typeMapper);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                payloadDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmbeddingRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmbeddingRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}