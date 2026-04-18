package aleksei.lab.server;

import manager.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import request.Request;
import response.Response;
import response.ResponseStatus;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientRequestHandler {
    private final Reader commandReader;
    private final ServerRequestReader requestReader;
    private final ServerResponseSender responseSender;
    private static final Logger logger = LogManager.getLogger(ClientRequestHandler.class);

    public ClientRequestHandler(Reader commandReader) {
        this.commandReader = commandReader;
        this.requestReader = new ServerRequestReader();
        this.responseSender = new ServerResponseSender();
    }

    public void handle(SocketChannel socketChannel) {
        try (SocketChannel clientChannel = socketChannel) {
            clientChannel.configureBlocking(false);
            Request request = requestReader.read(clientChannel);
            if ("save".equals(request.getCommandName())) {
                responseSender.send(clientChannel,
                        new Response("Command 'save' is server-only", ResponseStatus.ERROR, null));
                return;
            }
            responseSender.send(clientChannel, commandReader.toCommand(request));
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client error: " + e.getMessage());
            logger.error("Client error: " + e.getMessage());
        }
    }
}
