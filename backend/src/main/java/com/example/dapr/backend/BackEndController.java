package com.example.dapr.backend;

import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.State;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class BackEndController {

    private String STATE_STORE = "statestore";
    private String KEY = "count";

    private static int WAIT_FOR_SIDECAR = 10000;

    @PostConstruct()
    public void postconstruct() {
        log.info("Initializing state sotre");
        try (DaprClient client = new DaprClientBuilder().build()) {
            client.waitForSidecar(WAIT_FOR_SIDECAR).block();
            Counter counter = client.getState(STATE_STORE, KEY, Counter.class).block().getValue();
            if (counter == null) {
                saveCounter(client, new Counter());
            }
        } catch (Exception e) {
            log.error("post construction errror", e);
            throw new RuntimeException(e);
        }
    }


    @PostMapping("say")
    public String say(@RequestBody(required = false) byte[] body) {
        log.info("invoked");
        String message = body == null ? "" : new String(body, StandardCharsets.UTF_8);
        try (DaprClient client = new DaprClientBuilder().build()) {
            client.waitForSidecar(WAIT_FOR_SIDECAR).block();
            Counter counter = client.getState(STATE_STORE, KEY, Counter.class).block().getValue();
            counter.increment();
            saveCounter(client, counter);
            return String.format("Hello %s from backend service. count = %d\n", message, counter.getValue());
        } catch (Exception e) {
            log.error("say errror", e);
            throw new RuntimeException(e);
        }
    }

    private void saveCounter(DaprClient client, Counter counter) {
        // Omit contentType metadata to retain Redis Hash storage, including on Redis 8 with JSON support.
        client.saveBulkState(STATE_STORE, List.of(new State<>(KEY, counter, null))).block();
    }
}
