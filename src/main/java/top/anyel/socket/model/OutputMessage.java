package top.anyel.socket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data // generates getters and setters for all properties in the class
@AllArgsConstructor // Lombok will generate a constructor with all properties in the class
public class OutputMessage {

    private String from;
    private String text;
    private String time;


}