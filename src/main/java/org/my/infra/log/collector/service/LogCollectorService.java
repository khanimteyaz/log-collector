package org.my.infra.log.collector.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.my.infra.log.collector.model.UniqueException;
import org.my.infra.log.collector.repository.UniqueExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class LogCollectorService {

    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());
    private static final String EXCEPTION_REGEX_EXP="(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).\\d{3}" +
        "[\\s]+(INFO|TRACE|ERROR|DEBUG|WARNING|FATAL)" +
        "[\\s]+(.*)---[\\s]+\\[(.*)\\][\\s]+(.*)";

    private static final String STACK_TRACE_LINENUMBER_REGEX="(:[\\d]+)";

    private final int EXCEPTION_GROUP=5;

    private Pattern regex = Pattern.compile(EXCEPTION_REGEX_EXP, Pattern.MULTILINE);
    @Autowired
    private UniqueExceptionRepository uniqueExceptionRepository;

    public LogCollectorService() {

    }

    public boolean process(String exception) {
        String stackTraceMessage=getStacktraceMsg(exception.toString()).get();
        String md5OfStacktrace=md5Of(removeLineNumber(stackTraceMessage));
        System.out.println(String.format("Md5 of exception is %s",md5OfStacktrace));
        UniqueException uniqueException=uniqueExceptionRepository.findByExceptionHash(md5OfStacktrace);
        if(uniqueException!=null) {
            System.out.println(String.format("Exception already exists :::%s",stackTraceMessage.substring(0,40)));
        } else {
            System.out.println(String.format("New exception encounter %s",stackTraceMessage));
            uniqueException=buildUniqueException(stackTraceMessage);
            uniqueException.setExceptionHash(md5OfStacktrace);
            uniqueException=uniqueExceptionRepository.save(uniqueException);
            LOGGER.info(String.format("Unique exception saved in db with id %d",uniqueException.getId()));
        }
        return true;
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

    private  String md5Of(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}
