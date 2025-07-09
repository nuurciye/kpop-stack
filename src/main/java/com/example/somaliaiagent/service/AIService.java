package com.example.somaliaiagent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {

    private final ChatClient chatClient;

    // You can inject specific model names from properties if you want to switch easily
    @Value("${spring.ai.openai.chat.options.model:gpt-4o}") // Default if not set
    private String openAiModel;

    @Value("${spring.ai.ollama.chat.options.model:llama3.1}") // Default if not set
    private String ollamaModel;

    // TODO: Determine how to select between OpenAI and Ollama at runtime if both are configured.
    // This might involve different ChatClient beans or a factory.
    // For now, this service assumes a single ChatClient is configured and injected.

    @Autowired
    public AIService(ChatClient.Builder chatClientBuilder) {
        // The ChatClient.Builder will use the primary configured AI provider (OpenAI or Ollama)
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Processes a general user message using the configured LLM.
     *
     * @param userMessage The message from the user.
     * @param language    The language of the user message ("en" or "so").
     * @return The AI's response.
     */
    public String processUserMessage(String userMessage, String language) {
        // A very basic prompt, enhance this significantly for real interactions
        String systemPrompt = "You are a helpful AI assistant for a Somali restaurant. " +
                              "The user is communicating in " + (language.equals("so") ? "Somali." : "English.") +
                              " Respond in " + (language.equals("so") ? "Somali." : "English.");

        PromptTemplate promptTemplate = new PromptTemplate("{systemPrompt}\n\nUser: {userMessage}\nAssistant:");
        Prompt prompt = promptTemplate.create(Map.of(
                "systemPrompt", systemPrompt,
                "userMessage", userMessage
        ));

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        return response.getResult().getOutput().getContent();
    }

    /**
     * Extracts structured information from a user's message for a specific task.
     * Example: Extracting order details or reservation parameters.
     *
     * @param userMessage The message from the user.
     * @param taskDescription A description of what information to extract.
     * @param language The language of the user message.
     * @return Structured information (e.g., a Map or a custom DTO).
     */
    public Map<String, Object> extractInformation(String userMessage, String taskDescription, String language) {
        // This would require a more sophisticated prompt, possibly with examples (few-shot prompting)
        // or by using Spring AI's ability to map responses to Java objects.
        String systemPrompt = "You are an AI assistant that extracts structured information from user messages. " +
                              "The user is communicating in " + (language.equals("so") ? "Somali." : "English.") +
                              " Based on the user's message, extract the following: " + taskDescription +
                              " Provide the output in a JSON format.";

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\nUser Message: {userMessage}\n\nJSON Output:");
        Prompt prompt = promptTemplate.create(Map.of("userMessage", userMessage));

        // For structured output, you might use chatClient.prompt().call().entity(YourDto.class)
        // For now, let's assume it returns a string that we hope is JSON.
        String jsonResponse = chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getContent();

        // In a real application, parse this JSON string into a Map or DTO
        // For this stub, we'll return a placeholder.
        System.out.println("AI raw extraction response: " + jsonResponse);
        // ObjectMapper objectMapper = new ObjectMapper(); try { return objectMapper.readValue(jsonResponse, Map.class); } catch (Exception e) { ... }
        return Map.of("extracted_info_placeholder", jsonResponse);
    }


    /**
     * Placeholder for intent classification.
     * @param userMessage The user's message.
     * @param language The language of the message.
     * @return A string representing the classified intent (e.g., "MENU_INQUIRY", "PLACE_ORDER", "MAKE_RESERVATION").
     */
    public String classifyIntent(String userMessage, String language) {
        String systemPrompt = "You are an AI assistant that classifies user intent for a restaurant. " +
                              "Possible intents are: MENU_INQUIRY, PLACE_ORDER, ORDER_STATUS, MAKE_RESERVATION, CANCEL_RESERVATION, GENERAL_GREETING, OTHER. " +
                              "The user is communicating in " + (language.equals("so") ? "Somali." : "English.") +
                              " Respond with only the intent label.";

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\nUser Message: {userMessage}\n\nIntent:");
        Prompt prompt = promptTemplate.create(Map.of("userMessage", userMessage));

        String intent = chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getContent().trim();
        System.out.println("Classified intent for '" + userMessage + "': " + intent);
        return intent;
    }

    /**
     * Placeholder for translating text.
     * @param text The text to translate.
     * @param targetLanguage The language to translate to ("en" or "so").
     * @param sourceLanguage The language of the input text ("en" or "so").
     * @return Translated text.
     */
    public String translate(String text, String targetLanguage, String sourceLanguage) {
        if (sourceLanguage.equals(targetLanguage)) {
            return text;
        }
        String systemPrompt = "You are a translation assistant. Translate the following text from " +
                              (sourceLanguage.equals("so") ? "Somali" : "English") + " to " +
                              (targetLanguage.equals("so") ? "Somali." : "English.");

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt + "\n\nText: {textToTranslate}\n\nTranslation:");
        Prompt prompt = promptTemplate.create(Map.of("textToTranslate", text));

        String translatedText = chatClient.prompt(prompt).call().chatResponse().getResult().getOutput().getContent();
        System.out.println("Translated '" + text + "' from " + sourceLanguage + " to " + targetLanguage + ": " + translatedText);
        return translatedText;
    }

    // More specific AI methods as needed, e.g., for generating summaries, handling FAQs, etc.
}
