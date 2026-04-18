package commands;

import manager.CollectionManager;
import request.UUIDAndHumanBeingRequest;
import response.Response;

public class UpdateCommand extends Command<UUIDAndHumanBeingRequest> {
    public UpdateCommand(CollectionManager collectionManager) {
        super(collectionManager, UUIDAndHumanBeingRequest.class);
    }
    @Override
    public Response execute(UUIDAndHumanBeingRequest request) {
        return success(collectionManager.update(request.getId(), request.getHumanBeing()));
    }
}
