package com.gempukku.lotro.log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingProxy {
    private static final Logger logger = Logger.getLogger(LoggingProxy.class.getName());
    private static final long ERROR_LEVEL = 3000;
    private static final long WARN_LEVEL = 1000;
    private static final long INFO_LEVEL = 500;
    private static final long DEBUG_LEVEL = 100;

    public static <T> T createLoggingProxy(Class<T> clazz, T delegate) {
        final String simpleName = clazz.getSimpleName();
        return (T) Proxy.newProxyInstance(LoggingProxy.class.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> {
                    long start = System.currentTimeMillis();
                    try {
                        return method.invoke(delegate, args);
                    } catch (InvocationTargetException exp) {
                        throw exp.getTargetException();
                    } finally {
                        long time = System.currentTimeMillis() - start;
                        String name = method.getName();
                        if (time >= ERROR_LEVEL)
                            logger.log(Level.SEVERE, simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= WARN_LEVEL)
                            logger.log(Level.WARNING, simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= INFO_LEVEL)
                            logger.info(simpleName + "::" + name + "(...) " + time + "ms");
                        else if (time >= DEBUG_LEVEL)
                            logger.log(Level.FINE, simpleName + "::" + name + "(...) " + time + "ms");
                        else
                            logger.log(Level.FINEST, simpleName + "::" + name + "(...) " + time + "ms");
                    }
                });
    }
}
