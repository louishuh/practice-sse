package com.example.practicesse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.OffsetDateTime;

@Slf4j
public class MessageChannel {
    private final Sinks.Many<MessageEvent> sink;

    public MessageChannel() {
        sink = Sinks.many().multicast().directAllOrNothing();
    }

    void send(String message) {
        sink.tryEmitNext(new MessageEvent(OffsetDateTime.now(), message));
    }

    Flux<MessageEvent> toFlux() {
        return sink.asFlux();
    }

    synchronized boolean tryClosing() {
        log.debug("current subscriber count: {}", sink.currentSubscriberCount());
        if (sink.currentSubscriberCount() <= 1) {
            Sinks.EmitResult emitResult = sink.tryEmitComplete();
            log.debug("emit complete for closing result: {}", emitResult);
            return emitResult.isSuccess();
        }

        return false;
    }

}
