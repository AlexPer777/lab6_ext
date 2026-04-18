package request;

import java.util.UUID;
import model.HumanBeing;

public class UUIDAndHumanBeingRequest extends Request {
    private final UUID id;
    private final HumanBeing humanBeing;

    public UUIDAndHumanBeingRequest(String commandName, UUID id, HumanBeing humanBeing) {
        super(commandName);
        this.id = id;
        this.humanBeing = humanBeing;
    }

    public UUID getId() {
        return id;
    }

    public HumanBeing getHumanBeing() {
        return humanBeing;
    }
}
