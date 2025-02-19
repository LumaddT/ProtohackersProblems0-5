package proto.hackers.problem01_PrimeTime;

import com.fasterxml.jackson.annotation.JsonProperty;

class ServerMessage {
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
