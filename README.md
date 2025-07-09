Somali AI Host Agent for Restaurants

This project implements a multilingual AI host agent tailored for Somali restaurants using Spring AI. It handles voice and SMS interactions in Somali and English, supporting menu inquiries, order management, reservation handling, personalization, and analytics.

Features
• Multilingual Interaction: Processes calls and SMS in Somali and English, responding in the customer’s preferred language.
• Menu and Order Management: Responds to menu inquiries, processes takeout orders, and syncs with POS systems.
• Reservation Handling: Books reservations and manages availability.
• Personalization and Analytics: Stores customer preferences and provides analytics via a REST endpoint.

Technical Stack
• Spring Boot: For building REST APIs and managing application context.
• Spring AI: For integrating with Large Language Models (LLMs) like OpenAI or Ollama.
• Spring Data JPA & PostgreSQL: For persistent storage of customer, order, and reservation data.
• Twilio: For handling voice and SMS interactions.
• Maven: For dependency management and project build.

Setup Instructions

Prerequisites
• Java 17 or higher
• Maven
• PostgreSQL database
• Twilio Account (Account SID, Auth Token, and a Twilio Phone Number)
• OpenAI API Key or a running Ollama instance with a Somali-supported model (e.g., Llama 3.1)
• Square API credentials (if integrating with Square POS)

1. Clone the Repository
   git clone <repository-url>
   cd somali-ai-agent

2. Database Setup
   1. Create a PostgreSQL database named somali_ai_agent.
   2. Update the database connection properties in src/main/resources/application.properties:
      spring.datasource.url=jdbc:postgresql://localhost:5432/somali_ai_agent
      spring.datasource.username=your_postgres_username
      spring.datasource.password=your_postgres_password

3. Configure Twilio
   Add your Twilio credentials to src/main/resources/application.properties:
   twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   twilio.auth.token=your_auth_token
   twilio.phone.number=+1234567890

4. Configure LLM (OpenAI or Ollama)
   Option A: OpenAI
   Add your OpenAI API key to src/main/resources/application.properties:
   spring.ai.openai.api-key=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   spring.ai.openai.chat.options.model=gpt-4o

   Option B: Ollama
   If you are using Ollama, ensure it's running locally and you have a Somali-supported model (e.g., llama3.1) pulled. Then, configure application.properties:
   spring.ai.ollama.chat.options.model=llama3.1
   spring.ai.ollama.base-url=http://localhost:11434

5. Square POS Integration (Optional)
   For Square POS integration, you would typically use the Square API. This project provides placeholder for order processing. You would need to implement the actual Square API calls within the OrderService or a dedicated SquareService.

6. Build and Run the Application
   mvn clean install
   mvn spring-boot:run

7. Ngrok for Local Testing (Twilio Webhooks)
   To expose your local application to Twilio, you can use ngrok:
   ngrok http 8080
   Copy the https forwarding URL provided by ngrok and configure your Twilio phone number's webhook URLs:
   • Voice & Fax URL: YOUR_NGROK_URL/api/twilio/voice (HTTP POST)
   • Messaging URL: YOUR_NGROK_URL/api/twilio/sms (HTTP POST)

Deployment on Spring Cloud
For production deployment, consider using Spring Cloud services on platforms like AWS, Azure, or Google Cloud. The application is designed to be scalable and can be deployed as a standard Spring Boot application.

Example Deployment Steps (General)
1. Containerization: Create a Dockerfile for your Spring Boot application.
2. Container Registry: Push your Docker image to a container registry (e.g., Docker Hub, AWS ECR).
3. Orchestration: Deploy your containerized application using Kubernetes (EKS, AKS, GKE) or a managed service like AWS Elastic Beanstalk, Azure App Service, or Google App Engine.
4. Database: Use a managed PostgreSQL service (e.g., AWS RDS, Azure Database for PostgreSQL, Google Cloud SQL).
5. Environment Variables: Configure your Twilio, LLM, and database credentials as environment variables in your deployment environment.

API Endpoints
• POST /api/ai/message: Process AI messages.
• GET /api/menu: Get all menu items.
• POST /api/menu: Add a new menu item.
• GET /api/orders: Get all orders.
• POST /api/orders: Create a new order.
• GET /api/reservations: Get all reservations.
• POST /api/reservations: Create a new reservation.
• GET /api/analytics: Get analytics data.
• POST /api/twilio/sms: Twilio webhook for incoming SMS.
• POST /api/twilio/voice: Twilio webhook for incoming voice calls.
