package com.example.somaliaiagent.controller;

import com.example.somaliaiagent.service.AIService;
import com.example.somaliaiagent.service.CustomerService;
import com.example.somaliaiagent.service.TwilioService;
import com.example.somaliaiagent.model.Customer;

// Interaction specific services
// import com.example.somaliaiagent.service.MenuService;
// import com.example.somaliaiagent.service.OrderService;
// import com.example.somaliaiagent.service.ReservationService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/twilio")
public class TwilioWebhookController {

    private final TwilioService twilioService;
    private final AIService aiService;
    private final CustomerService customerService;
    // Inject other services like OrderService, MenuService, ReservationService as needed

    @Autowired
    public TwilioWebhookController(TwilioService twilioService, AIService aiService, CustomerService customerService) {
        this.twilioService = twilioService;
        this.aiService = aiService;
        this.customerService = customerService;
    }

    // POST /api/twilio/sms: Twilio webhook for incoming SMS
    @PostMapping(value = "/sms", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handleIncomingSms(
            @RequestParam("From") String fromPhoneNumber,
            @RequestParam("Body") String messageBody,
            @RequestParam("MessageSid") String messageSid,
            HttpServletRequest request // For advanced validation if needed
    ) {
        System.out.println("Received SMS from: " + fromPhoneNumber + ", Body: '" + messageBody + "', SID: " + messageSid);

        // 1. Get or create customer
        // For simplicity, default name and lang, can be updated later by AI or user preference
        Customer customer = customerService.getOrCreateCustomer(fromPhoneNumber, "Valued Customer", "en");

        // 2. Determine language (simple detection or use customer preference)
        String language = customer.getPreferredLanguage() != null ? customer.getPreferredLanguage() : "en";
        // Potentially use AIService.detectLanguage(messageBody) if more robust detection is needed.

        // 3. Process message with AI
        // This is a very simplified interaction flow. A real app would have a state machine
        // or more complex intent handling.
        String aiResponseText = aiService.processUserMessage(messageBody, language);

        // 4. Send response back via Twilio SMS (sync response - TwiML for SMS)
        // For SMS, Twilio expects an empty response or a TwiML <Message> response if you want to reply in the same HTTP response.
        // Alternatively, use twilioService.sendSms() for an async reply if complex processing is needed.
        // Sync response (TwiML):
        // com.twilio.twiml.messaging.Body twiMLBody = new com.twilio.twiml.messaging.Body.Builder(aiResponseText).build();
        // com.twilio.twiml.messaging.Message twiMLMessage = new com.twilio.twiml.messaging.Message.Builder().body(twiMLBody).build();
        // com.twilio.twiml.MessagingResponse twiMLResponse = new com.twilio.twiml.MessagingResponse.Builder().message(twiMLMessage).build();
        // return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(twiMLResponse.toXml());

        // For now, send async SMS and return empty 200 OK to Twilio to acknowledge receipt.
        twilioService.sendSms(fromPhoneNumber, aiResponseText);

        return ResponseEntity.ok().build(); // Acknowledge receipt to Twilio
    }

    // POST /api/twilio/voice: Twilio webhook for incoming voice calls
    @PostMapping(value = "/voice", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handleIncomingVoiceCall(
            @RequestParam("From") String fromPhoneNumber,
            @RequestParam("CallSid") String callSid,
            // @RequestParam(name = "SpeechResult", required = false) String speechResult, // If using Gather
            // @RequestParam(name = "Digits", required = false) String digits, // If using Gather with DTMF
            HttpServletRequest request
    ) {
        System.out.println("Received Voice Call from: " + fromPhoneNumber + ", Call SID: " + callSid);
        // String speechResult = request.getParameter("SpeechResult"); // More robust way to get optional params
        // String digits = request.getParameter("Digits");

        // 1. Get or create customer
        Customer customer = customerService.getOrCreateCustomer(fromPhoneNumber, "Valued Caller", "en");
        String language = customer.getPreferredLanguage() != null ? customer.getPreferredLanguage() : "en"; // "en-US" or "so-SO" for Twilio Say/Gather

        String twiMLResponse;

        // This is the initial interaction point.
        // A more complex flow would involve multiple turns using <Gather> and subsequent webhook calls.
        // For the first interaction:
        String initialGreeting = "Hello! Welcome to our restaurant's automated service. How can I help you today?";
        if ("so".equals(language)) {
            // TODO: Get actual Somali translation from AI or properties file
            initialGreeting = "Salaam! Kusoo dhawow adeega makhaayadeena. Sideen kuu caawin karaa maanta?";
        }

        // Example: Gather speech input
        // The action URL would be another endpoint (e.g., /api/twilio/voice/gather-handler)
        // that processes the SpeechResult.
        String gatherActionUrl = request.getRequestURL().toString() + "/handle-speech"; // Construct full URL

        // For Twilio Say verb, language codes like "en-US", "en-GB", etc. For Somali, need to check Twilio docs.
        // If "so-SO" is not a valid Say language, the `initialGreeting` must be in a supported language (e.g. English).
        String twilioSayLanguage = "en-US";
        if ("so".equals(language)) {
            // twilioSayLanguage = "so-SO"; // This is hypothetical, check Twilio docs
        }

        // Simple initial response: Say a greeting and then hang up (for stub)
        // twiMLResponse = twilioService.createVoiceResponse(initialGreeting, twilioSayLanguage);
        // To make it interactive, use <Gather>
        twiMLResponse = twilioService.createGatherSpeechResponse(initialGreeting, twilioSayLanguage, gatherActionUrl, "speech", 5);


        System.out.println("Responding with TwiML: " + twiMLResponse);
        return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(twiMLResponse);
    }

    // POST /api/twilio/voice/handle-speech: Handles the result of a <Gather>
    @PostMapping(value = "/voice/handle-speech", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> handleGatheredSpeech(
            @RequestParam("From") String fromPhoneNumber,
            @RequestParam("CallSid") String callSid,
            @RequestParam(name = "SpeechResult", required = false) String speechResult,
            @RequestParam(name = "Confidence", required = false) String confidenceStr
    ) {
        System.out.println("Voice Gather Handler - From: " + fromPhoneNumber + ", SID: " + callSid + ", Speech: '" + speechResult + "', Confidence: " + confidenceStr);

        Customer customer = customerService.getOrCreateCustomer(fromPhoneNumber, "Valued Caller", "en");
        String language = customer.getPreferredLanguage() != null ? customer.getPreferredLanguage() : "en";
        String twilioSayLanguage = "en-US";
         if ("so".equals(language)) {
            // twilioSayLanguage = "so-SO"; // Hypothetical
        }

        String responseMessage;

        if (speechResult != null && !speechResult.isEmpty()) {
            // Process the speechResult with AIService
            String aiResponseMessage = aiService.processUserMessage(speechResult, language);
            responseMessage = aiResponseMessage;
            // Potentially gather more input or end call
            // For now, say response and hang up.
        } else {
            responseMessage = "Sorry, I didn't catch that. Please try again.";
            if ("so".equals(language)) {
                // TODO: Somali translation
                responseMessage = "Waan ka xumahay, ma fahmin. Fadlan ku celi.";
            }
        }

        // For now, just say the AI's response and hang up.
        // A real application would decide the next step (e.g., another Gather, specific action).
        com.twilio.twiml.VoiceResponse.Builder builder = new com.twilio.twiml.VoiceResponse.Builder();
        builder.say(new com.twilio.twiml.voice.Say.Builder(responseMessage).language(
            com.twilio.twiml.voice.Say.Language.valueOf(twilioSayLanguage.replace("-","_")) // Enum needs underscore
        ).build());
        // builder.hangup(); // Or gather more input

        String twiMLResponse = builder.build().toXml();
        System.out.println("Gather Handler Responding with TwiML: " + twiMLResponse);
        return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(twiMLResponse);
    }


    // TODO: Add Twilio request validation for production using 'RequestValidator'
    // This involves getting the X-Twilio-Signature header and request parameters.
}
