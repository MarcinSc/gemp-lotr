package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.ResponseWriter;
import io.netty.handler.codec.http.HttpRequest;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface UriRequestHandler {
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception;

    default void logHttpError(Logger log, int code, String uri, Exception exp) {
        //401, 403, 404, and other 400 errors should just do minimal logging,
        // but 400 itself should error out
        if(code % 400 < 100 && code != 400) {
            log.log(Level.FINE, "HTTP " + code + " response for " + uri);
        }
        // record an HTTP 400
        else if(code == 400 || code % 500 < 100) {
            log.log(Level.SEVERE, "HTTP code " + code + " response for " + uri, exp);
        }
    }
}
