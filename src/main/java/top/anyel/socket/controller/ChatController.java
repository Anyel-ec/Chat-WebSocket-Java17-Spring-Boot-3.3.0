package top.anyel.socket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import top.anyel.socket.model.Message;
import top.anyel.socket.model.OutputMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
@Controller
public class ChatController {
    @MessageMapping("/chat")
    @SendTo("/topic/messages") // envia el mensaje a todos los clientes suscritos a /topic/messages
    public OutputMessage send(final Message message) throws Exception {
        // Obtiene la hora actual en formato HH:mm
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        // Crea un nuevo mensaje con el nombre del remitente, el texto del mensaje y la hora
        return new OutputMessage(message.getFrom(), message.getText(), time);
    }
}
