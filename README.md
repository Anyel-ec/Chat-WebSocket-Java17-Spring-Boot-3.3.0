# WebSocket Chat Application
Este repositorio se inspiró en [https://github.com/LuisChica18/demoWebSocket](https://github.com/LuisChica18/demoWebSocket).
Autor: Ing. Luis Chica Moncayo

Este proyecto es una aplicación de chat en tiempo real implementada con Spring Boot y WebSocket. Permite a los usuarios enviar y recibir mensajes a través de un socket WebSocket, además de recibir mensajes automáticos generados por bots a intervalos regulares.

## Requisitos Previos

- Java 17 o superior
- Maven 3.6.3 o superior

## Estructura del Proyecto

El proyecto contiene las siguientes clases principales:

### Modelos

#### `Message`
```java
@Data
@RequiredArgsConstructor
public class Message {
    private String from;
    private String text;
}
```

#### `OutputMessage`
```java
@Data
@AllArgsConstructor
public class OutputMessage {
    private String from;
    private String text;
    private String time;
}
```

### Servicios

#### `ReactiveScheduledPushMessages`
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

#### `ScheduledPushMessages`
```java
@Service
public class ScheduledPushMessages {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Faker faker;

    public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.faker = new Faker();
    }

    @Scheduled(fixedRate = 5000)
    public void sendMessage() {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        simpMessagingTemplate.convertAndSend("/topic/pushmessages",
            new OutputMessage("Chuck Norris", faker.chuckNorris().fact(), time));
    }
}
```

### Controladores

#### `BotsController`
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

#### `ChatController`
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

### Configuración

#### `WebSocketConfig`
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
        registry.addEndpoint("/chat");
        registry.addEndpoint("/chat").withSockJS();
        registry.addEndpoint("/chatwithbots");
        registry.addEndpoint("/chatwithbots").withSockJS();
    }
}
```

## Frontend

El frontend está implementado en HTML y JavaScript, utilizando SockJS y Stomp.js para la comunicación WebSocket.

### `chat.html`
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

### `chatwithbots.html`
```html
<!DOCTYPE html>
<html>
<head>
    <title>Chat WebSocket with Bots</title>
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
            var socket = new SockJS('/chatwithbots');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function(frame) {
                setConnected(true);
                console.log('Connected: ' + frame);
                stompClient.subscribe('/topic/pushmessages', function(messageOutput) {
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
            stompClient.send("/app/chatwithbots", {}, JSON.stringify({'from': from, 'text': text}));
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
        <br />
        <div>
            <button id="connect" onclick="connect();">Connect</button>
            <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button>
        </div>
        <br />
        <div id="conversationDiv">
            <input type="text" id="text" placeholder="Write a message..."/>
            <button id="sendMessage" onclick="sendMessage();">Send</button>
            <p id="response"></p>
        </div>
    </div>
</body>
</html>
```

## Configuración de Maven

El archivo `pom.xml` contiene todas las dependencias necesarias para ejecutar la aplicación.

### `pom.xml`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion

>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>
    <groupId>top.anyel</groupId>
    <artifactId>socket</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>WebSocketDemo</name>
    <description>WebSocketDemo</description>
    <packaging>war</packaging>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.javafaker</groupId>
            <artifactId>javafaker</artifactId>
            <version>1.0.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
        </dependency>
    </dependencies>
    <build>
        <finalName>spring-websockets</finalName>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
    </build>
</project>
```

## Instrucciones de Ejecución

1. Clona este repositorio.
2. Navega al directorio del proyecto.
3. Ejecuta `mvn clean install` para construir el proyecto.
4. Ejecuta `mvn spring-boot:run` para iniciar la aplicación.
5. Abre tu navegador y navega a `http://localhost:8080/chat.html` o `http://localhost:8080/chatwithbots.html` para comenzar a utilizar la aplicación de chat.

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un issue o envía un pull request para mejoras y correcciones.
```
