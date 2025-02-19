package proto.hackers.problem01_PrimeTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
class ClientMessage {
    private final String Method;
    private final double Number;

    public ClientMessage(@JsonProperty(value = "method", required = true) String method,
                         @JsonProperty(value = "number", required = true) double number) {
        Method = method;
        Number = number;
    }
}
