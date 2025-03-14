package com.example.kafka_test.controllers;

import com.example.kafka_test.events.PageEvent;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class PageEventController {

    private final StreamBridge streamBridge;
    private final InteractiveQueryService interactiveQueryService;
    @GetMapping("/publish")
    public PageEvent publish(@RequestParam String name, @RequestParam String topic) {
        PageEvent pageEvent = new PageEvent(
                name,
                Math.random() > 0.5 ? "U1" : "U2",
                new Date(),
                new Random().nextInt(10000) + 10
        );

        streamBridge.send(topic, pageEvent);

        return pageEvent;
    }

    @GetMapping(path = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Long>> analytics() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {

                    Map<String, Long> stringLongMap = new HashMap<>();
                    ReadOnlyWindowStore<String, Long> windowStore = interactiveQueryService.getQueryableStore("count-store", QueryableStoreTypes.windowStore());

                    // Vérifier si le store est bien récupéré
                    if (windowStore == null) {
                        System.out.println("❌ Erreur : count-store est NULL !");
                        return stringLongMap;
                    }

                    Instant now = Instant.now();
                    Instant from = now.minusMillis(5000);

                    // Vérifier si des données existent dans la fenêtre
                    KeyValueIterator<Windowed<String>, Long> fetchAll = windowStore.fetchAll(from, now);
                    if (!fetchAll.hasNext()) {
                        System.out.println("⚠️ Aucune donnée trouvée dans le count-store !");
                    }

                    while (fetchAll.hasNext()) {
                        KeyValue<Windowed<String>, Long> next = fetchAll.next();
                        stringLongMap.put(next.key.key(), next.value);
                    }

                    return stringLongMap;
                });
    }

}
