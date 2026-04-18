package commands;

import manager.CollectionManager;
import request.NoArgumentRequest;
import response.Response;

public class SaveCommand extends Command<NoArgumentRequest> {
    public SaveCommand(CollectionManager collectionManager) {
        super(collectionManager, NoArgumentRequest.class);
    }
    @Override
    public Response execute(NoArgumentRequest request) {
        return success(collectionManager.save());
    }
}
