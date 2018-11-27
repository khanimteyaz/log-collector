package org.my.infra.log.collector.constant;

public final class EventRegexParseConstant {
    public static final String EXCEPTION_REGEX_EXP="(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}).\\d{3}" +
        "[\\s]+(INFO|TRACE|ERROR|DEBUG|WARNING|FATAL)" +
        "[\\s]+(.*)---[\\s]+\\[(.*)\\][\\s]+(.*)";

    public static final String STACK_TRACE_LINENUMBER_REGEX="(:[\\d]+)";

    public static final int EVENT_TIME_GROUP=1;
    public static final int HOST_GROUP=3;
    public static final int EXCEPTION_GROUP=5;

    private EventRegexParseConstant() {

    }
}
