package top.anyel.socket.service;

import com.github.javafaker.Faker;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.anyel.socket.model.OutputMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ScheduledPushMessages {
    // Clase que permite enviar mensajes a los clientes conectados a un broker de mensajería
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final Faker faker; // Clase que permite generar datos falsos de manera aleatoria

    public ScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        faker = new Faker();
    }

    @Scheduled(fixedRate = 5000) // Anotación que permite ejecutar el método cada 5 segundos
    public void sendMessage() { // Método que envía un mensaje a todos los clientes suscritos a /topic/pushmessages
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        // Envia un mensaje con el nombre del remitente "Chuck Norris", un hecho aleatorio de Chuck Norris y la hora actual
        simpMessagingTemplate.convertAndSend("/topic/pushmessages",
                new OutputMessage("Chuck Norris", faker.chuckNorris().fact(), time));
    }
}
