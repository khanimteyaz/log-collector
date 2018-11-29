package org.my.infra.log.collector.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCollectorServerHandler extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogCollectorServerHandler.class);
    private Socket clientSocket;
    private BufferedReader in;

    public LogCollectorServerHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            System.out.println("Waiting for client to send data");
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            Writer bufferedWriter= new StringWriter();
            //String exception=in.readLine();
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                System.out.println("bufferedWriter = " + bufferedWriter);
                bufferedWriter.append(inputLine);
            }

            in.close();
            out.close();
            clientSocket.close();
            System.out.println(((StringWriter) bufferedWriter).getBuffer().toString());
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
        }
    }
}


