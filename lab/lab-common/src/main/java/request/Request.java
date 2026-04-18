package request;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private final String commandName;

    protected Request(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
