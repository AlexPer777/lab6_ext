package request;

public abstract class Request {
    private final String commandName;

    protected Request(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
