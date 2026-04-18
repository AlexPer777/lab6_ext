package commands;

import manager.CollectionManager;
import request.UUIDRequest;
import response.Response;

public class RemoveByIdCommand extends Command<UUIDRequest> {
    public RemoveByIdCommand(CollectionManager collectionManager){
        super(collectionManager, UUIDRequest.class);
    }
    @Override
    public Response execute(UUIDRequest request) {
        return success(collectionManager.removeById(request.getValue()));
    }
}
