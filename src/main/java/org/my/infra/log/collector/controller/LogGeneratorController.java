package org.my.infra.log.collector.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

@RestController
@RequestMapping("/log")
public class LogGeneratorController {

    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> logRest(HttpServletRequest request){
        System.out.println("Received data");
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            System.out.println(buffer.toString());
        } catch (Exception e) {
            LOGGER.error("Failed to read the request body from the request.");
        }
        return ResponseEntity.accepted().build();
    }
}
