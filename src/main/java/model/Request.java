package model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Request {
    private final String command;
    private final String[] arguments;

    public Request(String inputLine, String separator) {
        String[] parts = inputLine.split(separator);
        this.command = parts[0].toUpperCase();
        this.arguments = new String[parts.length - 1];
        System.arraycopy(parts, 1, this.arguments, 0, parts.length - 1);
    }
}