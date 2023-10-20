package com.example.chatgptbasedcookingingredients;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final WebClient webClient;

    @PostMapping
    String categorizeIngredient(@RequestBody String ingredient) {
        return webClient.post()
                .bodyValue(new ChatGPTRequest("Tell me whether " + ingredient + " is vegan, vegetarian or regular. Answer with 'unknown' if you do not have an answer. Only respond with one word."))
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .map(response -> {
                    String responseText = response.text();
                    System.out.println(responseText);
                    if ("vegan".equalsIgnoreCase(responseText)) {
                        return "vegan";
                    } else if ("vegetarian".equalsIgnoreCase(responseText)) {
                        return "vegetarian";
                    } else if ("regular".equalsIgnoreCase(responseText)) {
                        return "regular";
                    } else {
                        return "unknown";
                    }
                }).block();

    }

    record ChatGPTMessage(
            String role,
            String content
    ) {
    }

    record ChatGPTRequest(
            String model,
            List<ChatGPTMessage> messages
    ) {
        ChatGPTRequest(String message) {
            this("gpt-3.5-turbo", Collections.singletonList(new ChatGPTMessage("user", message)));
        }
    }

    record ChatGPTChoice(
            ChatGPTMessage message
    ) {
    }

    record ChatGPTResponse(
            List<ChatGPTChoice> choices
    ) {
        public String text() {
            return choices.get(0).message().content();
        }
    }

}
