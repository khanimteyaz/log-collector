package org.my.infra.log.collector.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.my.infra.log.collector.entity.CanonicalException;
import org.my.infra.log.collector.entity.ExceptionOccurrence;
import org.my.infra.log.collector.entity.JiraIssue;
import org.my.infra.log.collector.entity.UniqueException;
import org.my.infra.log.collector.model.LogEvent;
import org.my.infra.log.collector.repository.ExceptionOccurrenceRepository;
import org.my.infra.log.collector.repository.UniqueExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import static org.my.infra.log.collector.constant.EventRegexParseConstant.EXCEPTION_REGEX_EXP;
import static org.my.infra.log.collector.constant.EventRegexParseConstant.STACK_TRACE_LINENUMBER_REGEX;
import static org.my.infra.log.collector.constant.EventRegexParseConstant.EVENT_TIME_GROUP;
import static org.my.infra.log.collector.constant.EventRegexParseConstant.EXCEPTION_GROUP;
import static org.my.infra.log.collector.constant.EventRegexParseConstant.HOST_GROUP;

import static org.my.infra.log.collector.constant.LogPropertyConstant.APP_NAME;
import static org.my.infra.log.collector.constant.LogPropertyConstant.SOURCE;

@Service
public class LogCollectorService {

    private Logger LOGGER=LoggerFactory.getLogger(this.getClass());

    private Pattern regex = Pattern.compile(EXCEPTION_REGEX_EXP, Pattern.MULTILINE);

    @Autowired
    private UniqueExceptionRepository uniqueExceptionRepository;

    @Autowired
    private ExceptionOccurrenceRepository exceptionOccurrenceRepository;

    @Autowired
    private JiraService jiraService;

    public LogCollectorService() {

    }

    public boolean process(String exception,Map<String,String> headers) {
        LogEvent logEvent=parseEvent(exception,headers);
        String original= logEvent.getStackTraceWithLineNo();
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
        updateJiraIssue(uniqueHash,subHash,logEvent);
        addExceptionOccurence(uniqueHash,subHash,logEvent);
        return true;
    }

    private LogEvent parseEvent(String exception,Map<String,String> eventProperty) {
        Map<Integer,String> logEntryMap= parseRawEvent(exception);
        LogEvent event= new LogEvent();
        event.setAppName(eventProperty.get(APP_NAME));
        event.setSource(eventProperty.get(SOURCE));
        event.setHost(logEntryMap.get(HOST_GROUP));
        event.setEventTime(Timestamp.valueOf(logEntryMap.get(EVENT_TIME_GROUP)));
        event.setStackTraceWithLineNo(logEntryMap.get(EXCEPTION_GROUP));
        event.setStackTraceWithoutLineNo(removeLineNumber(event.getStackTraceWithLineNo()));
        event.setExceptionAsString(exception);
        event.setOtherInfos(eventProperty);
        return event;
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
        ,LogEvent logEvent) {

        String jiraId=jiraService.createNewIssue(buildJiraTitle(logEvent),logEvent.getExceptionAsString());
        LOGGER.info("Created new JIRA ticket with id {} ",jiraId);
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(uniqueHash);
        CanonicalException canonicalException=savedUniqueException.getCanonicalException(subHash);
        canonicalException.addJiraIssue(buildJiraIssue(jiraId,logEvent,canonicalException));
        uniqueExceptionRepository.save(savedUniqueException);
        LOGGER.info("Attached the jira id {} in repository",jiraId);

    }

    private void updateExistingTicket(String jiraId,String comment) {
        LOGGER.info("Updating existing JIRA id {} with new event details",jiraId);
        jiraService.updateIssue(jiraId,comment);
        LOGGER.info("JIRA id {} updated successfully",jiraId);
    }
    private void updateJiraIssue(String uniqueHash,String subHash,LogEvent logEvent) {

        Optional<JiraIssue> jiraIssue = getJiraIssue(uniqueHash,subHash
            , logEvent.getSource()
            , logEvent.getAppName());

        if(!jiraIssue.isPresent()) {
            createTicket(uniqueHash,subHash,logEvent);
        } else {
            updateExistingTicket(jiraIssue.get().getJiraId(),logEvent.getExceptionAsString());
        }

    }

    private Optional<JiraIssue> getJiraIssue(String uniqueHash,String subHash, String source, String app) {
        UniqueException savedUniqueException= uniqueExceptionRepository.findByExceptionHash(uniqueHash);
        CanonicalException canonicalException=savedUniqueException.getCanonicalException(subHash);
        return canonicalException.getOpenJiraIssue(source,app);
    }

    private void addExceptionOccurence(String uniqueHash,String subHash,LogEvent logEvent) {
        Optional<JiraIssue> jiraIssue = getJiraIssue(uniqueHash,subHash,logEvent.getSource(),logEvent.getAppName());
        ExceptionOccurrence occurrence = new ExceptionOccurrence();
        occurrence.setJiraId(jiraIssue.get().getJiraId());
        occurrence.setOccurredAt(logEvent.getEventTime());
        occurrence.setApp(logEvent.getAppName());
        occurrence.setSource(logEvent.getSource());
        occurrence.setHost(logEvent.getHost());
        occurrence.setOtherInfos(logEvent.getOtherInfos().toString());
        occurrence=exceptionOccurrenceRepository.save(occurrence);
        LOGGER.info("Occurrence {} has been added into db",occurrence);
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
                                     ,LogEvent logEvent
                                     ,CanonicalException canonicalException) {
        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setJiraId(jiraId);
        jiraIssue.setSource(logEvent.getSource());
        jiraIssue.setApp(logEvent.getAppName());
        jiraIssue.setStartAt(logEvent.getEventTime());
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
    private Map<Integer,String> parseRawEvent(String logEventMsg) {
        Map<Integer,String> parseGroup=new HashMap<>();
        Matcher m = regex.matcher(logEventMsg);

        if(m.matches()) {
            parseGroup.put(EVENT_TIME_GROUP,m.group(EVENT_TIME_GROUP));
            parseGroup.put(HOST_GROUP,m.group(HOST_GROUP));
            parseGroup.put(EXCEPTION_GROUP,m.group(EXCEPTION_GROUP));
        }
        return parseGroup;
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

    private String buildJiraTitle(LogEvent logEvent) {
        return logEvent.getSource() +
            "-" +
            logEvent.getAppName() +
            "-" +
            logEvent.getStackTraceWithoutLineNo().substring(0
                ,substrLength(logEvent.getStackTraceWithoutLineNo(),5));
    }
}
