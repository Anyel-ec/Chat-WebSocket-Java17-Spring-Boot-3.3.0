package top.anyel.socket.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Message{
    private String from;
    private String text;


}
