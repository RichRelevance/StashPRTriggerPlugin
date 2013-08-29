package com.richrelevance.stash.plugin.connection;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class OpenConnectionImpl implements OpenConnection {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(OpenConnectionImpl.class);

  private final HttpURLConnection connection;

  public OpenConnectionImpl(HttpURLConnection connection) {
    this.connection = connection;
  }

  @Override
  public SuccessfulConnection checkResult() {
    try {
      final int responseCode = connection.getResponseCode();

      if (responseCode == -1) {
        log.error("response from " + connection.getURL() + "is not a valid http response");
        return FailedConnection.getInstance();
      } else if (responseCode != HttpURLConnection.HTTP_OK) {
        log.error("failed to trigger " + connection.getURL() + ": " + responseCode + "(" + connection.getResponseMessage() + ")");
        return FailedConnection.getInstance();
      }
    } catch (IOException e) {
      log.error("unable to get response code from connection to " + connection.getURL());
      return FailedConnection.getInstance();
    }

    return new SuccessfulConnectionImpl(connection);
  }
}
