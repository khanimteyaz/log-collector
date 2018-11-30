package org.my.infra.log.collector.socket;

import static org.my.infra.log.collector.constant.EventRegexParseConstant.RECORD_START_REGEX_PARSER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import javax.validation.constraints.NotNull;
import org.my.infra.log.collector.service.LogCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCollectorServerHandler extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogCollectorServerHandler.class);
    private Socket clientSocket;
    private BufferedReader in;

    private final LogCollectorService logCollectorService;

    public LogCollectorServerHandler(@NotNull Socket socket
        ,@NotNull  final LogCollectorService logCollectorService) {
        this.clientSocket = socket;
        this.logCollectorService=logCollectorService;
    }

    public void run() {
        try {
            System.out.println("Waiting for client to send data");
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            StringBuilder lastRecord=new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                if(isStartOfNewRecord(inputLine)) {
                    if(!inputLine.isEmpty()) {
                        logCollectorService.process(lastRecord.toString());
                        lastRecord=new StringBuilder();
                        lastRecord.append(inputLine);
                    }

                }
                lastRecord.append(inputLine);
            }
            LOGGER.info("Closing handler");
            out.close();
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
        }
    }

    private boolean isStartOfNewRecord(String inputLine) {
        Matcher m = RECORD_START_REGEX_PARSER.matcher(inputLine);
        return m.matches();
    }
}


