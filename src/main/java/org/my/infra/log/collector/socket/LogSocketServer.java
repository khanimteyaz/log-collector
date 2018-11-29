package org.my.infra.log.collector.socket;

import java.io.IOException;
import java.net.ServerSocket;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class LogSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSocketServer.class);

    private ServerSocket serverSocket;
    private int logServerPort=9050;

    public LogSocketServer() {
        try {
            LOGGER.info("Performing setup for log collector server on port {}",logServerPort);
            serverSocket = new ServerSocket(logServerPort);
            LOGGER.info("Setup done successfully for log collector server on port {}",logServerPort);
            startLogCollectorServer();
        } catch (IOException e) {
            LOGGER.error("Unable to start log collector server at port {}",logServerPort,e);
        }
    }

    public void startLogCollectorServer() {
        while (true) {
            try {
                new LogCollectorServerHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                LOGGER.error("Got error while receiving connection from client ");
            }
        }

    }

    @PreDestroy
    public void stopServer() {
        try {
            LOGGER.info("Stopping log collector server");
            serverSocket.close();
            LOGGER.info("Server stopped successfully");
        } catch (IOException e) {
            LOGGER.error("Error encountered while stopping log collector server",e);
        }
    }
}
