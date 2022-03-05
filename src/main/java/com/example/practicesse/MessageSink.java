package com.example.practicesse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
public class MessageSink<M> {
    private final Sinks.Many<M> sink;

    public MessageSink() {
        sink = Sinks.many().multicast().directAllOrNothing();
    }

    void send(M message) {
        sink.tryEmitNext(message);
    }

    Flux<M> toFlux() {
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
