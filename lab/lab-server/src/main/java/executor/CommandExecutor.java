package executor;

import commands.*;
import manager.CollectionManager;
import manager.Reader;
import request.Request;
import response.Response;
import response.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private final Map<String, Command<? extends Request>> commands = new HashMap<>();
    private final CollectionManager collectionManager;
    private Reader reader;

    public CommandExecutor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }
    public void initCommands() {
        commands.put("help", new HelpCommand(collectionManager));
        commands.put("exit", new ExitCommand(collectionManager));
        commands.put("show", new ShowCommand(collectionManager));
        commands.put("clear", new ClearCommand(collectionManager));
        commands.put("info", new InfoCommand(collectionManager));
        commands.put("add", new AddCommand(collectionManager));
        commands.put("update", new UpdateCommand(collectionManager));
        commands.put("remove_by_id", new RemoveByIdCommand(collectionManager));
        commands.put("remove_at", new RemoveAtCommand(collectionManager));
        commands.put("save", new SaveCommand(collectionManager));
        commands.put("remove_greater", new RemoveGreaterCommand(collectionManager));
        commands.put("sort", new SortCommand(collectionManager));
        commands.put("count_greater_than_car", new CountGreaterThanCarCommand(collectionManager));
        commands.put("filter_contains_name", new FilterContainsNameCommand(collectionManager));
        commands.put("filter_starts_with_name", new FilterStartsWithNameCommand(collectionManager));
        commands.put("execute_script", new ExecuteScriptCommand(collectionManager, reader));
    }
    public Response execute(Request request) {
        if (request == null) {
            return new Response("Пустой запрос", ResponseStatus.ERROR, null);
        }
        Command<? extends Request> command = commands.get(request.getCommandName());
        if (command == null) {
            return new Response("Unknown command: " + request.getCommandName(), ResponseStatus.ERROR, null);
        }
        return executeTyped(command, request);
    }
    private <T extends Request> Response executeTyped(Command<T> command, Request request) {
        if (!command.getRequestType().isInstance(request)) {
            return new Response("Неверный тип запроса для команды " + request.getCommandName(), ResponseStatus.ERROR, null);
        }
        return command.execute(command.getRequestType().cast(request));
    }
}
