package com.example.inventoryservice.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class MetricsExporter {

    private static final Logger logger = LoggerFactory.getLogger(MetricsExporter.class);
    private final MeterRegistry meterRegistry;

    public MetricsExporter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void exportMetricsToFile() {
        try (FileWriter writer = new FileWriter("metrics.log", true)) {
            writer.write("=== Metrics at " + LocalDateTime.now() + " ===\n");
            meterRegistry.getMeters().forEach(meter -> {
                try {
                    Map<String, String> tags = meter.getId().getTags().stream()
                            .collect(java.util.stream.Collectors.toMap(t -> t.getKey(), t -> t.getValue()));
                    writer.write(meter.getId().getName() + ": " + meter.measure().iterator().next().getValue() +
                            " | tags: " + tags + "\n");
                } catch (Exception e) {
                    // ignore
                }
            });
            writer.write("==========================\n");
            logger.debug("Metrics exported to metrics.log");
        } catch (IOException e) {
            logger.error("Failed to export metrics", e);
        }
    }
}
