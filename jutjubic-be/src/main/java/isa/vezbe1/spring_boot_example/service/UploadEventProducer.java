package isa.vezbe1.spring_boot_example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import isa.vezbe1.spring_boot_example.config.RabbitMQConfig;
import isa.vezbe1.spring_boot_example.dto.UploadEvent;
import isa.vezbe1.spring_boot_example.proto.UploadEventProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UploadEventProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serialize event as JSON and send to json queue.
     * Returns array: [serialization time in ns, message size in bytes]
     */
    public long[] sendJsonEvent(UploadEvent event) {
        try {
            long startTime = System.nanoTime();
            byte[] jsonBytes = objectMapper.writeValueAsBytes(event);
            long serializationTime = System.nanoTime() - startTime;

            MessageProperties props = new MessageProperties();
            props.setContentType("application/json");
            rabbitTemplate.send(RabbitMQConfig.JSON_QUEUE, new Message(jsonBytes, props));

            log.info("JSON event sent | videoId={} | size={} bytes | serialization={}ns",
                    event.getVideoId(), jsonBytes.length, serializationTime);

            return new long[]{serializationTime, jsonBytes.length};
        } catch (Exception e) {
            log.error("Failed to send JSON event: {}", e.getMessage());
            return new long[]{0, 0};
        }
    }

    /**
     * Serialize event as Protobuf and send to protobuf queue.
     * Returns array: [serialization time in ns, message size in bytes]
     */
    public long[] sendProtobufEvent(UploadEvent event) {
        try {
            long startTime = System.nanoTime();

            UploadEventProtos.UploadEventProto proto = UploadEventProtos.UploadEventProto.newBuilder()
                    .setVideoId(event.getVideoId())
                    .setTitle(event.getTitle())
                    .setDescription(event.getDescription())
                    .setVideoSizeMb(event.getVideoSizeMb())
                    .setAuthorUsername(event.getAuthorUsername())
                    .setCreatedAt(event.getCreatedAt())
                    .addAllTags(event.getTags())
                    .build();

            byte[] protoBytes = proto.toByteArray();
            long serializationTime = System.nanoTime() - startTime;

            MessageProperties props = new MessageProperties();
            props.setContentType("application/x-protobuf");
            rabbitTemplate.send(RabbitMQConfig.PROTOBUF_QUEUE, new Message(protoBytes, props));

            log.info("Protobuf event sent | videoId={} | size={} bytes | serialization={}ns",
                    event.getVideoId(), protoBytes.length, serializationTime);

            return new long[]{serializationTime, protoBytes.length};
        } catch (Exception e) {
            log.error("Failed to send Protobuf event: {}", e.getMessage());
            return new long[]{0, 0};
        }
    }
}
