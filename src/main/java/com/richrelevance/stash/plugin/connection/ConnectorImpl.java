package com.richrelevance.stash.plugin.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richrelevance.stash.plugin.BuildTrigger;

/**
 */
public class ConnectorImpl implements Connector {
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BuildTrigger.class);

  private final HttpURLConnection connection;

  public ConnectorImpl(HttpURLConnection connection) {
    this.connection = connection;
  }

  @Override
  public OpenConnection connect() {
    try {
      connection.connect();
    } catch (SocketTimeoutException e) {
      log.error("timeout connecting to " + connection.getURL(), e);
      return FailedConnection.getInstance();
    } catch (IOException e) {
      log.error("unable to connect to " + connection.getURL(), e);
      return FailedConnection.getInstance();
    }
    return new OpenConnectionImpl(connection);
  }
}
