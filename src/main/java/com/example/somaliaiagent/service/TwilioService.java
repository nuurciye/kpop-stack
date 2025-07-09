package com.example.somaliaiagent.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
// For TwiML for voice responses
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Play; // If playing audio files


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    // Call this method once, perhaps in a @PostConstruct or when the service is first used.
    // Or ensure Twilio.init() is called in your main application class or a config class.
    private boolean initialized = false;

    private void initializeTwilio() {
        if (!initialized) {
            Twilio.init(accountSid, authToken);
            initialized = true;
            System.out.println("Twilio SDK Initialized.");
        }
    }

    public void sendSms(String toPhoneNumber, String messageBody) {
        initializeTwilio();
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber), // To
                    new PhoneNumber(twilioPhoneNumber), // From
                    messageBody)
                    .create();
            System.out.println("SMS sent! SID: " + message.getSid() + " To: " + toPhoneNumber);
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error sending SMS to " + toPhoneNumber + ": " + e.getMessage());
            // Consider re-throwing as a custom application exception or handling appropriately
        }
    }

    /**
     * Creates a TwiML response to say a message to the caller.
     * @param messageText The text to be spoken.
     * @param language The language for speech synthesis ("en-US" for English, "so-SO" for Somali if supported by Twilio, otherwise use English and translate content).
     *                 Twilio's standard Say verb might have limited Somali support. Enhanced voices might be better.
     *                 Refer to Twilio documentation for available Say voices and languages.
     *                 If Somali is not directly supported in Say, the messageText should already be in a language Twilio supports (e.g. English after translation).
     * @return TwiML string.
     */
    public String createVoiceResponse(String messageText, String language) {
        Say.Language twilioLanguage = Say.Language.EN_US; // Default
        if ("so".equalsIgnoreCase(language)) {
            // Check Twilio documentation for the correct enum or string for Somali.
            // Example: Say.Language.SO_SO (this is hypothetical, verify actual value)
            // If not directly supported, the messageText should be pre-translated to English
            // or a supported language, and language parameter here would reflect that.
            // For now, let's assume we use English voice if Somali is not available or message is pre-translated.
            // twilioLanguage = Say.Language.SO_SO; // Replace with actual if available
            System.out.println("Somali language requested for Twilio Say. Ensure messageText is in a supported voice/language or pre-translated. Using English voice as placeholder if Somali not directly available.");
        }

        VoiceResponse twiml = new VoiceResponse.Builder()
                .say(new Say.Builder(messageText).language(twilioLanguage).build())
                .build();
        return twiml.toXml();
    }

    /**
     * Creates a TwiML response to gather speech input from the caller.
     * @param promptMessage The message to prompt the user for input.
     * @param language The language for speech synthesis of the prompt.
     * @param actionUrl The URL to send the gathered speech result to.
     * @param inputType "speech" or "dtmf". We'll focus on "speech".
     * @param speechTimeout Timeout for speech input.
     * @return TwiML string.
     */
    public String createGatherSpeechResponse(String promptMessage, String language, String actionUrl, String inputType, int speechTimeout) {
        Say.Language twilioLanguage = Say.Language.EN_US;
        String speechLanguageHint = "en-US"; // For speech recognition

        if ("so".equalsIgnoreCase(language)) {
            // speechLanguageHint = "so-SO"; // If Twilio supports Somali speech recognition
            System.out.println("Somali language requested for Twilio Gather. Speech recognition hint set to Somali (if supported). Prompt will use English voice as placeholder if Somali Say not available.");
        }

        Gather gather = new Gather.Builder()
                .inputType(Gather.Input.SPEECH) // or Gather.Input.DTMF or BOTH
                .action(actionUrl)
                .method(com.twilio.http.HttpMethod.POST)
                .speechTimeout(Integer.toString(speechTimeout)) // e.g., "auto" or "3"
                .language(speechLanguageHint) // Hint for speech recognition
                .say(new Say.Builder(promptMessage).language(twilioLanguage).build())
                .build();

        VoiceResponse twiml = new VoiceResponse.Builder().gather(gather).build();
        return twiml.toXml();
    }

    /**
     * Creates a TwiML response to play an audio file.
     * @param audioUrl The publicly accessible URL of the audio file.
     * @return TwiML string.
     */
    public String createPlayAudioResponse(String audioUrl) {
        VoiceResponse twiml = new VoiceResponse.Builder()
            .play(new Play.Builder(audioUrl).build())
            .build();
        return twiml.toXml();
    }

    /**
     * Creates a TwiML response to hang up the call.
     * @return TwiML string.
     */
    public String createHangupResponse() {
        VoiceResponse twiml = new VoiceResponse.Builder()
                .hangup()
                .build();
        return twiml.toXml();
    }

    // Placeholder for more complex Twilio interactions
    // e.g., handling call transfers, conference calls, validating Twilio requests (using RequestValidator)

    public String getAccountSid() {
        return accountSid;
    }

    public String getTwilioPhoneNumber() {
        return twilioPhoneNumber;
    }
}
