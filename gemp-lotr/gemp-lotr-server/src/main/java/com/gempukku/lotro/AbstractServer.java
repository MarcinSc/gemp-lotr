package com.gempukku.lotro;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractServer {
    private static final Logger _logger = Logger.getLogger(AbstractServer.class.getName());
    private static final ServerCleaner _cleaningTask = new ServerCleaner();

    private boolean _started;

    public void startServer() {
        if (!_started) {
            _cleaningTask.addServer(this);
            _started = true;
            _logger.log(Level.FINE, "Started: "+getClass().getSimpleName());
            doAfterStartup();
        }
    }

    protected void doAfterStartup() {

    }

    public void stopServer() {
        if (_started) {
            _cleaningTask.removeServer(this);
            _started = false;
            _logger.log(Level.FINE, "Stopped: "+getClass().getSimpleName());
        }
    }

    protected abstract void cleanup() throws SQLException, IOException;
}
