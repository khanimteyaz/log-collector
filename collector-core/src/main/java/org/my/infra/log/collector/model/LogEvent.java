package org.my.infra.log.collector.model;

import java.sql.Timestamp;
import java.util.Map;

public class LogEvent {

    private String source;
    private String appName;
    private String host;
    private Timestamp eventTime;
    private String stackTraceWithLineNo;
    private String stackTraceWithoutLineNo;
    private String exceptionAsString;
    private Map<String,String> otherInfos;

    public LogEvent() {
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public String getStackTraceWithLineNo() {
        return stackTraceWithLineNo;
    }

    public void setStackTraceWithLineNo(String stackTraceWithLineNo) {
        this.stackTraceWithLineNo = stackTraceWithLineNo;
    }

    public String getStackTraceWithoutLineNo() {
        return stackTraceWithoutLineNo;
    }

    public void setStackTraceWithoutLineNo(String stackTraceWithoutLineNo) {
        this.stackTraceWithoutLineNo = stackTraceWithoutLineNo;
    }

    public String getExceptionAsString() {
        return exceptionAsString;
    }

    public void setExceptionAsString(String exceptionAsString) {
        this.exceptionAsString = exceptionAsString;
    }

    public Map<String, String> getOtherInfos() {
        return otherInfos;
    }

    public void setOtherInfos(Map<String, String> otherInfos) {
        this.otherInfos = otherInfos;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogEvent{");
        sb.append("source='").append(source).append('\'');
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", eventTime='").append(eventTime).append('\'');
        sb.append(", stackTraceWithLineNo='").append(stackTraceWithLineNo).append('\'');
        sb.append(", stackTraceWithoutLineNo='").append(stackTraceWithoutLineNo).append('\'');
        sb.append(", exceptionAsString='").append(exceptionAsString).append('\'');
        sb.append(", otherInfos=").append(otherInfos);
        sb.append('}');
        return sb.toString();
    }
}
