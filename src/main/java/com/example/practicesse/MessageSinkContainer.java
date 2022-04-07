package com.example.practicesse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageSinkContainer<M> {

    private final ConcurrentHashMap<String, MessageSink<M>> sinksMap = new ConcurrentHashMap<>();

    public Flux<M> getStream(String code) {

        return sinksMap
                .computeIfAbsent(code, key -> {
                    MessageSink<M> messageSink = new MessageSink<>();
                    log.debug("open new channel [{}]. channels count: {}", key, sinksMap.size() + 1);
                    return messageSink;
                })
                .toFlux()
                .doOnCancel(() -> {
                    log.debug("doOnCancel");
                    MessageSink<M> messageSink = sinksMap.get(code);
                    if (messageSink.tryClosing()) {
                        sinksMap.remove(code);
                        log.debug("the channel [{}] closed. channels count: {}", code, sinksMap.size());
                    }
                })
                .doOnTerminate(() -> {
                    log.debug("doOnTerminate");
                })
                .doAfterTerminate(() -> {
                    log.debug("doAfterTerminate.");
                })
                .doFinally(signalType -> {
                    log.debug("doFinally. signalType: {}", signalType);
                })
                .doOnEach(mSignal -> log.debug("doOnEach, signal: {}", mSignal))
                .doOnDiscard(Object.class, obj -> log.debug("doOnDiscard. obj: {}", obj))
                .doOnError(t -> log.debug("on error.", t));
    }

    public void send(String code, M message) {
        Optional.ofNullable(sinksMap.get(code))
                .ifPresentOrElse(ch -> {
                    log.debug("send the message to the channel [{}]", code);
                    ch.send(message);
                }, () -> log.debug("ignored send message because the channel [{}] is nobody use.", code));
    }

    public List<String> getAliveCodes() {
        return new ArrayList<>(sinksMap.keySet());
    }

}
