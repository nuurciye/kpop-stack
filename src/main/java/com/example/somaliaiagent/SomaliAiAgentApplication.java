package com.example.somaliaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SomaliAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SomaliAiAgentApplication.class, args);
    }

    // You could add @PostConstruct beans here for initial setup if needed,
    // for example, to initialize the Twilio SDK, though it's often done
    // within the service that uses it or a dedicated config class.
    /*
    @Autowired
    private TwilioService twilioService; // Assuming TwilioService has an init method

    @PostConstruct
    public void initTwilio() {
        twilioService.initialize(); // Or Twilio.init(sid, token) directly if preferred here
    }
    */
}
