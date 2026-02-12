package isa.vezbe1.spring_boot_example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String JSON_QUEUE = "upload-events-json";
    public static final String PROTOBUF_QUEUE = "upload-events-protobuf";

    @Bean
    public Queue jsonQueue() {
        return new Queue(JSON_QUEUE, true);
    }

    @Bean
    public Queue protobufQueue() {
        return new Queue(PROTOBUF_QUEUE, true);
    }
}
