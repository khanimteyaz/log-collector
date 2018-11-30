package org.my.infra.log.collector.constant;

import java.util.regex.Pattern;

public final class EventRegexParseConstant {
    public static final String EXCEPTION_REGEX_EXP="[{](.*)[}][\\s]+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).\\d{3}" +
        "[\\s]+(INFO|TRACE|ERROR|DEBUG|WARNING|FATAL)" +
        "[\\s]+(.*)---[\\s]+\\[(.*)\\][\\s]+(.*)";

    public static final String BEGINNING_OF_LOG_MESSAGE_REGEX="[{](.*)[}](.*)";
    public static final String STACK_TRACE_LINENUMBER_REGEX="(:[\\d]+)";

    public static final int EVENT_PROPERTIES_GROUP=1;
    public static final int EVENT_TIME_GROUP=2;
    public static final int HOST_GROUP=4;
    public static final int EXCEPTION_GROUP=6;

    public static final Pattern RAW_RECORD_PARSER = Pattern.compile(EXCEPTION_REGEX_EXP, Pattern.MULTILINE);
    public static final Pattern RECORD_START_REGEX_PARSER = Pattern.compile(BEGINNING_OF_LOG_MESSAGE_REGEX,Pattern.MULTILINE);

    private EventRegexParseConstant() {

    }
}
