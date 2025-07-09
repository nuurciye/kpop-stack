package com.example.somaliaiagent.config;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.client.RestTemplate;

// If you need to conditionally create ChatClient beans or other specific configurations,
// you can do it here. For now, Spring AI's auto-configuration should handle
// ChatClient creation based on application.properties.

@Configuration
public class AppConfig {

    // Example: If you needed a RestTemplate bean for external API calls (not Square SDK related)
    // @Bean
    // public RestTemplate restTemplate() {
    //     return new RestTemplate();
    // }

    // Twilio initialization could be done here via @PostConstruct in TwilioService
    // or by calling an init method from main application class.

    // Spring AI ChatClient bean is usually auto-configured if the respective starter (openai or ollama)
    // is on the classpath and properties are set.
    // If you need to customize it, for example, based on a profile or a dynamic property:
    /*
    @Value("${spring.ai.active.provider:ollama}") // Example custom property
    private String activeAiProvider;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        if ("openai".equalsIgnoreCase(activeAiProvider)) {
            // Potentially customize builder for OpenAI if needed, though auto-config is usually sufficient
            // builder.defaultOptions(OpenAiChatOptions.builder().withModel("gpt-4o").build());
        } else { // ollama or default
            // Potentially customize builder for Ollama if needed
            // builder.defaultOptions(OllamaOptions.builder().withModel("llama3.1").build());
        }
        return builder.build();
    }
    */

    // For now, we'll rely on Spring AI's auto-configuration for ChatClient.
    // This AppConfig class serves as a placeholder for any future explicit bean definitions
    // or component scan configurations if needed beyond standard Spring Boot setup.

    // A common use case here might be to configure ObjectMapper for JSON processing
    // or to set up global CORS configuration if not done via WebMvcConfigurer.
}
