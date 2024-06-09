package top.anyel.socket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class OutputMessage {
    private String from;
    private String text;
    private String time;
}