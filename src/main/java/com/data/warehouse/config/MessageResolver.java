package com.data.warehouse.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessageSource messageSource;



    public void setMessageSource(MessageSource messageSource) {
        messageSource = messageSource;
    }
    public String get(String code) {
        return messageSource.getMessage(code, null, null);
    }

    public String get(String code, Object[] args) {
        return messageSource.getMessage(code, args, null);
    }
}