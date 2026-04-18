package request;

import java.util.UUID;

public class UUIDRequest extends Request {
    private final UUID value;

    public UUIDRequest(String commandName, UUID value) {
        super(commandName);
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }
}
