package rabbitmq.consumer.example.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import rabbitmq.consumer.example.proto.UploadEventProtos;

@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    @RabbitListener(queues = "${myqueue2}")
    public void handler(Message message) {
        try {
            byte[] body = message.getBody();

            long startTime = System.nanoTime();
            UploadEventProtos.UploadEventProto proto = UploadEventProtos.UploadEventProto.parseFrom(body);
            long deserializationTime = System.nanoTime() - startTime;

            log.info("Protobuf Consumer | deserialization={}ns | size={} bytes | videoId={} | title='{}' | author='{}' | sizeMb={} | tags={}",
                    deserializationTime, body.length,
                    proto.getVideoId(), proto.getTitle(), proto.getAuthorUsername(),
                    proto.getVideoSizeMb(), proto.getTagsList());
        } catch (Exception e) {
            log.error("Protobuf Consumer | Failed to deserialize: {}", e.getMessage());
        }
    }
}
