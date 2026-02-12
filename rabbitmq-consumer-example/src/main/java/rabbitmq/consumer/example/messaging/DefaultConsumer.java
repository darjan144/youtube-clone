package rabbitmq.consumer.example.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DefaultConsumer {

    private static final Logger log = LoggerFactory.getLogger(DefaultConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${myqueue}")
    public void handler(Message message) {
        try {
            byte[] body = message.getBody();

            long startTime = System.nanoTime();
            UploadEvent event = objectMapper.readValue(body, UploadEvent.class);
            long deserializationTime = System.nanoTime() - startTime;

            log.info("JSON Consumer | deserialization={}ns | size={} bytes | event={}",
                    deserializationTime, body.length, event);
        } catch (Exception e) {
            log.error("JSON Consumer | Failed to deserialize: {}", e.getMessage());
        }
    }
}
