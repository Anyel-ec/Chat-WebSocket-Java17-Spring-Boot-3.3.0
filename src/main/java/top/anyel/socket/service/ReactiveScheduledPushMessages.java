package top.anyel.socket.service;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.anyel.socket.model.OutputMessage;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@Service
public class ReactiveScheduledPushMessages implements InitializingBean {

    // SimpMessagingTemplate es una clase que permite enviar mensajes a los clientes conectados a un broker de mensajería
    private final SimpMessagingTemplate simpMessagingTemplate;

    // Faker es una clase que permite generar datos falsos de manera aleatoria
    private final Faker faker;

    // Constructor de la clase
    public ReactiveScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate; // Inicializa el atributo simpMessagingTemplate
        this.faker = new Faker(); // Inicializa el atributo faker
    }

    // Metodo que se ejecuta despues de que la clase es inicializada
    @Override
    public void afterPropertiesSet() throws Exception {
        Flux.interval(Duration.ofSeconds(4L)) // Crea un flujo de datos que emite un valor cada 4 segundos
                .map((n) -> new OutputMessage(faker.backToTheFuture().character(), faker.backToTheFuture().quote(),
                        new SimpleDateFormat("HH:mm").format(new Date())))
                .subscribe(message -> simpMessagingTemplate.convertAndSend("/topic/pushmessages", message));
    }
}
