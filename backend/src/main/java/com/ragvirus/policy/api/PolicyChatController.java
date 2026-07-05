package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.PolicyChatRequest;
import com.ragvirus.policy.api.dto.PolicyChatResponse;
import com.ragvirus.policy.application.rag.PolicyChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policy-chat")
public class PolicyChatController {

    private final PolicyChatService chatService;

    public PolicyChatController(PolicyChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public PolicyChatResponse chat(@RequestBody PolicyChatRequest request) {
        return chatService.answer(request);
    }
}
