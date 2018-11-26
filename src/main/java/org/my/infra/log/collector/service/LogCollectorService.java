package org.my.infra.log.collector.service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.my.infra.log.collector.entity.CanonicalException;
import org.my.infra.log.collector.entity.JiraIssue;
import org.my.infra.log.collector.entity.UniqueException;
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

    @Autowired
    private JiraService jiraService;

    public LogCollectorService() {

    }

    public boolean process(String exception) {
        String original=getStacktraceMsg(exception).get();
        String normalizeExceptionStr=removeLineNumber(original);
        String uniqueHash=md5Of(normalizeExceptionStr);
        String subHash=md5Of(original);
        System.out.println(String.format("Md5 of exception is %s",uniqueHash));
        if(uniqueExceptionRepository.existsByExceptionHash(uniqueHash)) {
            System.out.println(String.format("Exception already exists :::%s",normalizeExceptionStr.substring(0,40)));
            updateExisting(normalizeExceptionStr,original);
        } else {
            System.out.println(String.format("New exception encounter %s",normalizeExceptionStr));
            saveAsNew(normalizeExceptionStr,original);
        }
        updateJiraIssue(uniqueHash,subHash,"source1","app1",original);
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
        String uniqueHash=md5Of(normalize);
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(uniqueHash);
        String subHash=md5Of(original);
        if(savedUniqueException.isCanonicalExceptionAlreadyExists(subHash)) {
            LOGGER.info("Canonical exception {} is already attached to {}"
                ,subHash,savedUniqueException.getExceptionHash());

        } else {
            CanonicalException canonicalException=buildCanonicalException(original,savedUniqueException);
            savedUniqueException.addExceptionVersion(canonicalException);
            uniqueExceptionRepository.save(savedUniqueException);
            LOGGER.info("Attached the new version of exception {} into root exception {}",canonicalException,savedUniqueException);

        }
        return true;
    }

    private void createTicket(String uniqueHash
        ,String subHash
        ,String source
        ,String app
        ,String exception) {

        String jiraId=jiraService.createNewIssue(buildJiraTitle(source,app,exception),exception);
        LOGGER.info("Created new JIRA ticket with id {} ",jiraId);
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(uniqueHash);
        CanonicalException canonicalException=savedUniqueException.getCanonicalException(subHash);
        canonicalException.addJiraIssue(buildJiraIssue(jiraId
            ,"source1"
            ,"app1",exception
            ,Timestamp.valueOf("2038-01-19 03:14:07"),canonicalException));
        uniqueExceptionRepository.save(savedUniqueException);
        LOGGER.info("Attached the jira id {} in repository",jiraId);

    }
    private void updateJiraIssue(String uniqueHash,String subHash,String source,String app,String original) {

        CanonicalException canonicalException;
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(uniqueHash);
        canonicalException=savedUniqueException.getCanonicalException(md5Of(original));
        Optional<JiraIssue> jiraIssue = canonicalException.getOpenJiraIssue(source,app);
        if(!jiraIssue.isPresent()) {
            createTicket(uniqueHash,subHash,source,app,original);
        }

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
        canonicalException.setException(exception.substring(0,substrLength(exception,10000)));
        canonicalException.setUniqueException(uniqueException);
        return canonicalException;
    }

    private JiraIssue buildJiraIssue(String jiraId
                                     ,String source
                                     ,String app
                                     ,String exception
                                     ,Timestamp createdAt
                                     ,CanonicalException canonicalException) {
        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setJiraId(jiraId);
        jiraIssue.setSource(source);
        jiraIssue.setApp(app);
        jiraIssue.setStartAt(createdAt);
        jiraIssue.setCanonicalException(canonicalException);
        jiraIssue.setStatus("OPEN");
        return jiraIssue;
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
        uniqueException.setNormalizeException(normalizeException.substring(0,substrLength(normalizeException,10000)));
        return uniqueException;
    }

    private int substrLength(String str,int maxLength) {
        return str.length()<maxLength?str.length():maxLength;
    }
    private  String md5Of(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }

    private String buildJiraTitle(String source,String app,String exception) {
        return source +
            "-" +
            app +
            "-" +
            exception.substring(0,substrLength(exception,5));
    }
}
