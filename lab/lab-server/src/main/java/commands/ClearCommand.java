package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;

public class ClearCommand extends Command<NoArgumentRequest> {
    public ClearCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return success(collectionManager.clear());
    }
}