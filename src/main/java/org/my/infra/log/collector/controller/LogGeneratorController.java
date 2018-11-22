package org.my.infra.log.collector.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/log")
public class LogGeneratorController {
    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());
    private static final String EXCEPTION_REGEX_EXP="(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).\\d{3}" +
            "[\\s]+(INFO|TRACE|ERROR|DEBUG|WARNING|FATAL)" +
            "[\\s]+(.*)---[\\s]+\\[(.*)\\][\\s]+(.*)";

    private final int EXCEPTION_GROUP=5;

    Pattern regex = Pattern.compile(EXCEPTION_REGEX_EXP, Pattern.MULTILINE);


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
            System.out.println(getStacktraceMsg(buffer.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to read the request body from the request.");
            e.printStackTrace();
        }
        return ResponseEntity.accepted().build();
    }

    private String getStacktraceMsg(String logEventMsg) {
        Matcher m = regex.matcher(logEventMsg);
        m.matches();
//        for(int index=1;index<EXCEPTION_GROUP;index++) {
//            m.group(index);
//        }
        return m.group(EXCEPTION_GROUP);
    }
}
