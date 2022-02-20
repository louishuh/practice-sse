package com.example.practicesse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
public class MessageEvent {
    private OffsetDateTime at;
    private String message;
}
