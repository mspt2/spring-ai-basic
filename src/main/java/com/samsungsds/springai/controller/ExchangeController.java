package com.samsungsds.springai.controller;

import com.samsungsds.springai.function.ExchangeRateFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExchangeController {

    private final ChatClient chatClient;

    public ExchangeController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/ask-exchange")
    public String askExchangeRate(@RequestParam String question) {

        return  chatClient.prompt()
                .user(question)
                .function("exchangeRate", "현재 원화/달러 환율을 조회 해 온다. (USD/KRW만 지원)", new ExchangeRateFunction())
                .call()
                .content();

    }
}