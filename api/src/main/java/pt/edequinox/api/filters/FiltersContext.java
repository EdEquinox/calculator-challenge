package pt.edequinox.api.filters;

import org.slf4j.MDC;

public class FiltersContext {

    public static final String REQUEST_ID_MDC_KEY = "request_id";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    public static void put(String requestId) {
        if (requestId != null) {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
        }
    }

    public static void remove() {
        MDC.remove(REQUEST_ID_MDC_KEY);
    }

    public static String get() {
        return MDC.get(REQUEST_ID_MDC_KEY);
    }

}
