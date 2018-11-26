package org.my.infra.log.collector.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.my.infra.log.collector.model.CanonicalException;
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
        String originalExceptionStr=getStacktraceMsg(exception.toString()).get();
        String normalizeExceptionStr=removeLineNumber(originalExceptionStr);
        String md5OfStacktrace=md5Of(normalizeExceptionStr);
        System.out.println(String.format("Md5 of exception is %s",md5OfStacktrace));
        if(uniqueExceptionRepository.existsByExceptionHash(md5OfStacktrace)) {
            System.out.println(String.format("Exception already exists :::%s",normalizeExceptionStr.substring(0,40)));
            updateExisting(normalizeExceptionStr,originalExceptionStr);
        } else {
            System.out.println(String.format("New exception encounter %s",normalizeExceptionStr));
            saveAsNew(normalizeExceptionStr,originalExceptionStr);
        }
        return true;
    }

    private UniqueException saveAsNew(String normalize,String original) {
        UniqueException uniqueException = buildUniqueException(md5Of(normalize), normalize);
        uniqueException.addExceptionVersion(buildCanonicalException(original,uniqueException));
        uniqueException=uniqueExceptionRepository.save(uniqueException);
        LOGGER.info(String.format("Unique exception saved in db with id %d",uniqueException.getId()));
        return uniqueException;
    }

    private boolean updateExisting(String normalize,String original) {
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(md5Of(normalize));
        CanonicalException canonicalException=buildCanonicalException(original,savedUniqueException);
        if(savedUniqueException.isCanonicalExceptionAlreadyExists(canonicalException)) {
            LOGGER.info("Canonical exception {} is already attached to {}"
                ,canonicalException.getExceptionSubVersionHash(),savedUniqueException.getExceptionHash());
        } else {
            savedUniqueException.addExceptionVersion(canonicalException);
            uniqueExceptionRepository.save(savedUniqueException);
            LOGGER.info("Attached the new version of exception {} into root exception {}",canonicalException,savedUniqueException);
        }
        return true;
    }
    private UniqueException buildUniqueException(String md5, String normalize) {
        UniqueException uniqueException=buildUniqueException(normalize);
        uniqueException.setExceptionHash(md5);
        return uniqueException;
    }

    private CanonicalException buildCanonicalException(String exception,UniqueException uniqueException){
        String md5=md5Of(exception);
        CanonicalException canonicalException = new CanonicalException();
        canonicalException.setExceptionSubVersionHash(md5);
        canonicalException.setException(exception.substring(0,substrLength(exception)));
        canonicalException.setUniqueException(uniqueException);
        return canonicalException;
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
        uniqueException.setNormalizeException(normalizeException.substring(0,substrLength(normalizeException)));
        return uniqueException;
    }

    private int substrLength(String str) {
        return str.length()<10000?str.length():10000;
    }
    private  String md5Of(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}
