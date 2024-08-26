package com.samsungsds.springaibasic.controller;

import com.samsungsds.springaibasic.model.*;
import com.samsungsds.springaibasic.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ChatController {

    private final Logger logger = Logger.getLogger(ChatController.class.getName());

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/capitalWithInfo")
    @Operation(summary = "포멧이 있는 응답 출력", description = "아래의 프롬프트로 응답 형식을 지정 해 준 수도 관련 질문에 대한 응답을 출력 한다. : <br> " +
            "{country} 나라의 수도는 어디인지와 아래 형태로 대답해줘: <br>" +
            "           \\```country\\``` 나라의 수도는 \\```capital\\```.<br>" +
            "           인구는 \\```population\\```.<br>" +
            "           언어는 \\```language\\```.<br>" +
            "           통화는 \\```currency\\```.")
    public CapitalResponse getCapitalWithInfo(@Parameter(description = "수도를 알고싶은 국가 이름 -> {country}" , example = "대한민국") @RequestBody CapitalRequest capitalRequest) {
        return this.chatService.getCapitalWithInfo(capitalRequest);
    }

    @PostMapping("/capital")
    @Operation(summary = "", description = "아래의 프롬프트를 실행한다. : <br>" +
            " {country} 나라의 수도는 어디?<br> ")
    public CapitalResponse getCapital(@Parameter(description = "수도를 알고싶은 국가나 주 이름 -> {country}", example = "대한민국") @RequestBody CapitalRequest capitalRequest) {
        return this.chatService.getCapital(capitalRequest);
    }

    @PostMapping("/ask")
    @Operation(summary = "자유 프롬프트 실행", description = "별도 포멧 없이 사용자의 입력을 그대로 실행")
    public Answer askQuestion(@Parameter(description = "실행 할 프롬프트") @RequestBody Question question) {
        return chatService.getAnswer(question);
    }


    @MessageMapping("/chat")
    public void handleChat(@Payload ChatMessage message) {

        this.chatService.streamChat(message)
                .doOnNext(content -> {
                    ChatMessage chatMessage = new ChatMessage(message.getSessionId(), "AI", content);
                    messagingTemplate.convertAndSend("/topic/chat/" + message.getSessionId(), chatMessage);
                })
                .doOnError(error -> logger.log(Level.SEVERE, "Error in chat stream", error))
                .doOnComplete(() -> logger.info("Chat stream completed for session: " + message.getSessionId()))
                .subscribe();

    }

}