package com.example.dapr.backend;

import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class BackEndController {

    private String STATE_STORE = "statestore";
    private String KEY = "count";

    @PostConstruct()
    public void postconstruct() {
        log.info("Initializing state sotre");
        try (DaprClient client = new DaprClientBuilder().build()) {
            client.waitForSidecar(100000).block();
            Counter counter = client.getState(STATE_STORE, KEY, Counter.class).block().getValue();
            if (counter == null) {
                client.saveState(STATE_STORE, KEY, new Counter()).block();
            }
        } catch (Exception e) {
            log.error("build errror", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("say")
    public String say(@RequestBody(required = false) byte[] body) {
        log.info("invoked");
        String message = body == null ? "" : new String(body, StandardCharsets.UTF_8);
        try (DaprClient client = new DaprClientBuilder().build()) {
            client.waitForSidecar(10000).block();
            Counter counter = client.getState(STATE_STORE, KEY, Counter.class).block().getValue();
            log.info("prev counter = " + counter.getValue());
            counter.increment();
            client.saveState(STATE_STORE, KEY, counter).block();
            log.info("after counter = " + counter.getValue());
            return String.format("Hello %s from backend service. count = %d\n", message, counter.getValue());
        } catch (Exception e) {
            log.error("build errror", e);
            throw new RuntimeException(e);
        }
    }
}
