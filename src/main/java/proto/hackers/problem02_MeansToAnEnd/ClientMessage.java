package proto.hackers.problem02_MeansToAnEnd;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientMessage {
    private final MessageTypes MessageType;
    private final int FirstValue;
    private final int SecondValue;

    @Override
    public String toString() {
        return "%s, %d, %d".formatted(MessageType.toString(), FirstValue, SecondValue);
    }
}
