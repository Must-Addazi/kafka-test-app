package com.example.kafka_test.controllers;

import com.example.kafka_test.events.PageEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
public class PageEventController {

    private static final Logger logger = LoggerFactory.getLogger(PageEventController.class);
    private final StreamBridge streamBridge;

    @GetMapping("/publish")
    public PageEvent publish(@RequestParam String name, @RequestParam String topic) {
        PageEvent pageEvent = new PageEvent(
                name,
                Math.random() > 0.5 ? "U1" : "U2",
                new Date(),
                new Random().nextInt(10000) + 10
        );

        logger.info("Publishing event: {} to topic: {}", pageEvent, topic);
        streamBridge.send(topic, pageEvent);

        return pageEvent;
    }
}
