package org.my.infra.log.collector.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.my.infra.log.collector.service.LogCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogGeneratorController {
    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LogCollectorService logCollectorService;

    public LogGeneratorController() {
        this.logCollectorService=logCollectorService;
    }


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> logRest(HttpServletRequest request) throws IOException {
        System.out.println("Received data");
        Enumeration<String> headers=request.getHeaderNames();
        Map<String,String> headerMap=new HashMap<>();
        while(headers.hasMoreElements()) {
            String key=headers.nextElement();
            String value=request.getHeader(key);
            System.out.println(key+"===>>"+value);
            headerMap.put(key,value);
        }
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        logCollectorService.process(buffer.toString());
        return ResponseEntity.accepted().build();
    }

}
