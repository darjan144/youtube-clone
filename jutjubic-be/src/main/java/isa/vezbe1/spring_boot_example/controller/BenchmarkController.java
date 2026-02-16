package isa.vezbe1.spring_boot_example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import isa.vezbe1.spring_boot_example.dto.UploadEvent;
import isa.vezbe1.spring_boot_example.service.UploadEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "Benchmark", description = "JSON vs Protobuf serialization benchmarking via RabbitMQ")
public class BenchmarkController {

    @Autowired
    private UploadEventProducer uploadEventProducer;

    @Operation(summary = "Run serialization benchmark", description = "Sends sample upload events via both JSON and Protobuf, returning timing and size comparison statistics")
    @ApiResponse(responseCode = "200", description = "Benchmark results returned")
    @PostMapping("/upload-events")
    public ResponseEntity<Map<String, Object>> benchmarkUploadEvents(
            @Parameter(description = "Number of messages to send (default 50)") @RequestParam(defaultValue = "50") int count) {

        List<Long> jsonSerTimes = new ArrayList<>();
        List<Long> jsonSizes = new ArrayList<>();
        List<Long> protoSerTimes = new ArrayList<>();
        List<Long> protoSizes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            UploadEvent event = createSampleEvent(i);

            long[] jsonResult = uploadEventProducer.sendJsonEvent(event);
            jsonSerTimes.add(jsonResult[0]);
            jsonSizes.add(jsonResult[1]);

            long[] protoResult = uploadEventProducer.sendProtobufEvent(event);
            protoSerTimes.add(protoResult[0]);
            protoSizes.add(protoResult[1]);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("messageCount", count);

        Map<String, Object> jsonStats = new LinkedHashMap<>();
        jsonStats.put("totalSerializationTimeNs", jsonSerTimes.stream().mapToLong(Long::longValue).sum());
        jsonStats.put("avgSerializationTimeNs", jsonSerTimes.stream().mapToLong(Long::longValue).average().orElse(0));
        jsonStats.put("avgMessageSizeBytes", jsonSizes.stream().mapToLong(Long::longValue).average().orElse(0));
        jsonStats.put("totalMessageSizeBytes", jsonSizes.stream().mapToLong(Long::longValue).sum());
        report.put("json", jsonStats);

        Map<String, Object> protoStats = new LinkedHashMap<>();
        protoStats.put("totalSerializationTimeNs", protoSerTimes.stream().mapToLong(Long::longValue).sum());
        protoStats.put("avgSerializationTimeNs", protoSerTimes.stream().mapToLong(Long::longValue).average().orElse(0));
        protoStats.put("avgMessageSizeBytes", protoSizes.stream().mapToLong(Long::longValue).average().orElse(0));
        protoStats.put("totalMessageSizeBytes", protoSizes.stream().mapToLong(Long::longValue).sum());
        report.put("protobuf", protoStats);

        // Comparison
        double jsonAvgTime = jsonSerTimes.stream().mapToLong(Long::longValue).average().orElse(1);
        double protoAvgTime = protoSerTimes.stream().mapToLong(Long::longValue).average().orElse(1);
        double jsonAvgSize = jsonSizes.stream().mapToLong(Long::longValue).average().orElse(1);
        double protoAvgSize = protoSizes.stream().mapToLong(Long::longValue).average().orElse(1);

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("serializationSpeedRatio", String.format("%.2fx", jsonAvgTime / protoAvgTime));
        comparison.put("messageSizeRatio", String.format("%.2fx", jsonAvgSize / protoAvgSize));
        comparison.put("protobufSmallerByPercent", String.format("%.1f%%", (1 - protoAvgSize / jsonAvgSize) * 100));
        report.put("comparison", comparison);

        return ResponseEntity.ok(report);
    }

    private UploadEvent createSampleEvent(int index) {
        return new UploadEvent(
                (long) (index + 1),
                "Sample Video " + index,
                "This is a sample video description for benchmarking purposes. Video number " + index,
                150.5 + index,
                "benchmarkUser",
                System.currentTimeMillis(),
                List.of("benchmark", "test", "sample", "video" + index)
        );
    }
}
