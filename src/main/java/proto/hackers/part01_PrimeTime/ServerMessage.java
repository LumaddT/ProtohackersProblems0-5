package proto.hackers.part01_PrimeTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerMessage {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @JsonProperty("method")
    private final String Method;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @JsonProperty("prime")
    private final boolean Prime;

    public ServerMessage(String method, boolean prime) {
        Method = method;
        Prime = prime;
    }
}
