package com.example.practicesse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageChannels {

    private final ConcurrentHashMap<String, MessageChannel> channelsMap = new ConcurrentHashMap<>();

    public Flux<MessageEvent> getStream(String channelCode) {

        return channelsMap
                .computeIfAbsent(channelCode, key -> {
                    MessageChannel messageChannel = new MessageChannel();
                    log.debug("open new channel [{}]. channels count: {}", key, channelsMap.size() + 1);
                    return messageChannel;
                })
                .toFlux()
                .doOnCancel(() -> {
                    MessageChannel messageChannel = channelsMap.get(channelCode);
                    if (messageChannel.tryClosing()) {
                        channelsMap.remove(channelCode);
                        log.debug("the channel [{}] closed. channels count: {}", channelCode, channelsMap.size());
                    }
                });
    }

    public void post(String channelCode, String message) {
        Optional.ofNullable(channelsMap.get(channelCode))
                .ifPresentOrElse(ch -> {
                    log.debug("send the message to the channel [{}]", channelCode);
                    ch.send(message);
                }, () -> log.debug("ignored send message because the channel [{}] is nobody use.", channelCode));
    }

    public List<String> getChannelsAsText() {
        return new ArrayList<>(channelsMap.keySet());
    }

}
