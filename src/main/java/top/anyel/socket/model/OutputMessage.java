package top.anyel.socket.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class OutputMessage {

    private String from;
    private String text;
    private String time;

}