package org.my.infra.log.collector.controller;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.my.infra.log.collector.model.UniqueException;
import org.my.infra.log.collector.repository.UniqueExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogGeneratorController {
    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());
    private static final String EXCEPTION_REGEX_EXP="(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).\\d{3}" +
            "[\\s]+(INFO|TRACE|ERROR|DEBUG|WARNING|FATAL)" +
            "[\\s]+(.*)---[\\s]+\\[(.*)\\][\\s]+(.*)";

    private static final String STACK_TRACE_LINENUMBER_REGEX="(:[\\d]+)";

    private final int EXCEPTION_GROUP=5;

    private Pattern regex = Pattern.compile(EXCEPTION_REGEX_EXP, Pattern.MULTILINE);

    private Set<String> uniqueExceptions= new HashSet<>();

    @Autowired
    private UniqueExceptionRepository uniqueExceptionRepository;

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
            String normalizeStackTrace=removeLineNumber(getStacktraceMsg(buffer.toString()).get());
            if(uniqueExceptions.contains(normalizeStackTrace)) {
                System.out.println(String.format("Exception already exists :::%s",normalizeStackTrace.substring(0,40)));
            } else {
                UniqueException uniqueException=buildUniqueException(normalizeStackTrace);
                uniqueException=uniqueExceptionRepository.save(uniqueException);
                LOGGER.info("Unique exception saved in db with id %d",uniqueException.getId());
            }
            uniqueExceptions.add(normalizeStackTrace);
        } catch (Exception e) {
            LOGGER.error("Failed to read the request body from the request.");
            e.printStackTrace();
        }
        return ResponseEntity.accepted().build();
    }

    private String removeLineNumber(String stackTrace) {
        if(StringUtils.isEmpty(stackTrace)) {
            return "";
        }
        return stackTrace.replaceAll(STACK_TRACE_LINENUMBER_REGEX,"") ;
    }
    private Optional<String> getStacktraceMsg(String logEventMsg) {
        Matcher m = regex.matcher(logEventMsg);
        if(m.matches()) {
            return Optional.of(m.group(EXCEPTION_GROUP));
        }
        return Optional.empty();
    }

    private UniqueException buildUniqueException(String normalizeException) {
        UniqueException uniqueException = new UniqueException();
        uniqueException.setException(normalizeException);
        return uniqueException;
    }
}
