# Chat WebSocket Application

Este repositorio se inspiró en [https://github.com/LuisChica18/demoWebSocket](https://github.com/LuisChica18/demoWebSocket).

Autor: Ing. Luis Chica Moncayo

## Table of Contents

1. [Project Structure](#project-structure)
2. [Prerequisites](#prerequisites)
3. [Running the Application](#running-the-application)
4. [WebSocket Endpoints](#websocket-endpoints)
5. [Frontend Integration](#frontend-integration)
6. [Detailed Explanation](#detailed-explanation)

## Project Structure

The project consists of the following main components:

- `Message`: A model representing the chat message.
- `OutputMessage`: A model representing the output message with a timestamp.
- `ReactiveScheduledPushMessages`: A service that sends random messages at intervals using reactive programming.
- `ScheduledPushMessages`: A service that sends random messages at fixed intervals.
- `BotsController`: A controller that handles messages to a bot.
- `ChatController`: A controller that handles chat messages.
- `WebSocketConfig`: A configuration class for setting up WebSocket endpoints and message brokers.
- `index.html`: A frontend file for connecting and interacting with the WebSocket.

## Prerequisites

Ensure you have the following installed:

- Java 11 or higher
- Maven
- A web browser

## Running the Application

1. Clone the repository:
   ```sh
   git clone https://github.com/Anyel-ec/Chat-WebSocket-Java17-Spring-Boot-3.3.0
   cd Chat-WebSocket-Java17-Spring-Boot-3.3.0
   ```

2. Build the application using Maven:
   ```sh
   mvn clean install
   ```

3. Run the application:
   ```sh
   mvn spring-boot:run
   ```

4. Open a web browser and navigate to:
   ```
   http://localhost:8080
   ```

## WebSocket Endpoints

- `/chat`: WebSocket endpoint for chat messages.
- `/chatwithbots`: WebSocket endpoint for bot interactions.

### Configured Subscriptions

- `/topic/messages`: Subscription for chat messages.
- `/topic/pushmessages`: Subscription for messages sent by the bot or scheduled services.

## Frontend Integration

### HTML and JavaScript

Include the following in your `index.html` to integrate with the WebSocket:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Chat WebSocket</title>
    <script src="resources/js/sockjs-0.3.4.js"></script>
    <script src="resources/js/stomp.js"></script>
    <script type="text/javascript">

        var stompClient = null;

        function setConnected(connected) {
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
            document.getElementById('response').innerHTML = '';
        }

        function connect() {
            var socket = new SockJS('/chat');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function (frame) {
                setConnected(true);
                console.log('Connected: ' + frame);
                stompClient.subscribe('/topic/messages', function (messageOutput) {
                    showMessageOutput(JSON.parse(messageOutput.body));
                });
            });
        }

        function disconnect() {
            if (stompClient != null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }

        function sendMessage() {
            var from = document.getElementById('from').value;
            var text = document.getElementById('text').value;
            stompClient.send("/app/chat", {}, JSON.stringify({'from': from, 'text': text}));
        }

        function showMessageOutput(messageOutput) {
            var response = document.getElementById('response');
            var p = document.createElement('p');
            p.style.wordWrap = 'break-word';
            p.appendChild(document.createTextNode(messageOutput.from + ": " + messageOutput.text + " (" + messageOutput.time + ")"));
            response.appendChild(p);
        }

    </script>
</head>
<body onload="disconnect()">
<div>
    <div>
        <input type="text" id="from" placeholder="Choose a nickname"/>
    </div>
    <br/>
    <div>
        <button id="connect" onclick="connect();">Connect</button>
        <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button>
    </div>
    <br/>
    <div id="conversationDiv">
        <input type="text" id="text" placeholder="Write a message..."/>
        <button id="sendMessage" onclick="sendMessage();">Send</button>
        <p id="response"></p>
    </div>
</div>
</body>
</html>
```

## Detailed Explanation

### Models

- `Message`: Represents an incoming message from a user.
    ```java
    @Data
    @RequiredArgsConstructor
    public class Message {
        private String from;
        private String text;
    }
    ```

- `OutputMessage`: Represents the message sent to the client, including a timestamp.
    ```java
    @Data
    @AllArgsConstructor
    public class OutputMessage {
        private String from;
        private String text;
        private String time;
    }
    ```

### Services

- `ReactiveScheduledPushMessages`: Sends random messages using reactive programming.
    ```java
    @Service
    public class ReactiveScheduledPushMessages implements InitializingBean {
        private final SimpMessagingTemplate simpMessagingTemplate;
        private final Faker faker;

        public ReactiveScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
            this.simpMessagingTemplate = simpMessagingTemplate;
            this.faker = new Faker();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            Flux.interval(Duration.ofSeconds(4L))
                .map((n) -> new OutputMessage(faker.backToTheFuture().character(), faker.backToTheFuture().quote(),
                        new SimpleDateFormat("HH:mm").format(new Date())))
                .subscribe(message -> simpMessagingTemplate.convertAndSend("/topic/pushmessages", message));
        }
    }
    ```

- `ScheduledPushMessages`: Sends random messages at fixed intervals.
    ```java
    @Service
    public class ScheduledPushMessages {
        private final SimpMessagingTemplate simpMessagingTemplate;
        private final Faker faker;

        public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
            this.simpMessagingTemplate = simpMessagingTemplate;
            faker = new Faker();
        }

        @Scheduled(fixedRate = 5000)
        public void sendMessage() {
            final String time = new SimpleDateFormat("HH:mm").format(new Date());
            simpMessagingTemplate.convertAndSend("/topic/pushmessages",
                new OutputMessage("Chuck Norris", faker.chuckNorris().fact(), time));
        }
    }
    ```

### Controllers

- `BotsController`: Handles messages directed to the bot.
    ```java
    @Controller
    public class BotsController {
        @MessageMapping("/chatwithbots")
        @SendTo("/topic/pushmessages")
        public OutputMessage send(final Message message) throws Exception {
            final String time = new SimpleDateFormat("HH:mm").format(new Date());
            return new OutputMessage(message.getFrom(), message.getText(), time);
        }
    }
    ```

- `ChatController`: Handles regular chat messages.
    ```java
    @Controller
    public class ChatController {
        @MessageMapping("/chat")
        @SendTo("/topic/messages")
        public OutputMessage send(final Message message) throws Exception {
            final String time = new SimpleDateFormat("HH:mm").format(new Date());
            return new OutputMessage(message.getFrom(), message.getText(), time);
        }
    }
    ```

### Configuration

- `WebSocketConfig`: Sets up WebSocket endpoints and message brokers.
    ```java
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        @Override
        public void configureMessageBroker(final MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic");
            config.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(final StompEndpointRegistry registry) {
            registry.addEndpoint("/chat").withSockJS();
            registry.addEndpoint("/chatwithbots").withSockJS();
        }
    }
    ```

This setup provides a fully functional chat application using WebSockets with Spring Boot, enabling real-time communication between clients and the server.