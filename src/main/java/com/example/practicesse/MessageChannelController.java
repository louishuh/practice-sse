package com.example.practicesse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
public class MessageChannelController {

    private final MessageSinkContainer<MessageEvent> sinkContainer = new MessageSinkContainer<>();

    @GetMapping("/channels/{channelCode}")
    public Flux<ServerSentEvent<MessageEvent>> sse(@PathVariable String channelCode) {

        log.debug("requested sse: {}", Thread.currentThread().getName());

        Flux<MessageEvent> messageEventFlux = sinkContainer.getStream(channelCode);
        Flux<MessageEvent> tickFlux = Flux.interval(Duration.ofSeconds(5))
                .map(tick -> new MessageEvent(OffsetDateTime.now(), "HEARTBEAT"));

        return Flux.merge(messageEventFlux, tickFlux)
                .map(event -> ServerSentEvent.builder(event).build());
    }

    @PostMapping(value = "/channels/{channelCode}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void send(@PathVariable String channelCode, @RequestBody String message) {

        log.debug("requested send message: {}", Thread.currentThread().getName());
        sinkContainer.send(channelCode, new MessageEvent(OffsetDateTime.now(), message));
    }

    @GetMapping("/channels")
    public List<String> getChannels() {
        return sinkContainer.getAliveCodes();
    }

    @GetMapping(value = "/channels", params = "size")
    public int getChannelCount() {
        return sinkContainer.getAliveCodes().size();
    }

}
