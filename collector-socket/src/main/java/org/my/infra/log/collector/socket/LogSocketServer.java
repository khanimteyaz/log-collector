package org.my.infra.log.collector.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import org.my.infra.log.collector.service.LogCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSocketServer.class);

    private ServerSocket serverSocket;
    private final LogCollectorService logCollectorService;

    private List<LogCollectorServerHandler> handlers= new ArrayList<>();

    public LogSocketServer(@Value("${app.socket.server.port}") final String logServerPort,
                           final @NotNull @Autowired LogCollectorService logCollectorService) {
        this.logCollectorService=logCollectorService;
        try {
            LOGGER.info("Performing setup for log collector socket on port {}",logServerPort);
            serverSocket = new ServerSocket(Integer.parseInt(logServerPort));
            LOGGER.info("Setup done successfully for log collector socket on port {}",logServerPort);
            startLogCollectorServer();
        } catch (IOException e) {
            LOGGER.error("Unable to start log collector socket at port {}",logServerPort,e);
        }
    }



    public void startLogCollectorServer() {
        while (true) {
            try {
                LogCollectorServerHandler handler=new LogCollectorServerHandler(serverSocket.accept()
                    ,logCollectorService);
                handlers.add(handler);
                handler.start();
                handler.join();
            } catch (IOException e) {
                LOGGER.error("Got error while receiving connection from client ");
            } catch (InterruptedException e) {
                LOGGER.error("Got error while receiving connection from client ");
            }
        }

    }

    @PreDestroy
    public void stopServer() {
        try {
            LOGGER.info("Stopping log collector socket");
            handlers.forEach(h-> {

            });
            serverSocket.close();
            LOGGER.info("Server stopped successfully");
        } catch (IOException e) {
            LOGGER.error("Error encountered while stopping log collector socket",e);
        }
    }
}
