package com.richrelevance.stash.plugin.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richrelevance.stash.plugin.BuildTrigger;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;

/**
*/
public class TriggerConnectionBuilder implements URLConnectionBuilder {
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BuildTrigger.class);

  private final String url;
  private final PullRequestTriggerSettings settings;

  private HttpURLConnection connection;

  public TriggerConnectionBuilder(String url, PullRequestTriggerSettings settings) {
    this.url = url;
    this.settings = settings;
  }

  @Override
  public Connector createConnection() {
    try {
      checkURL();
      openConnection();
      setHeaders();
      setMethod();
    } catch (URLConnectionBuildError urlConnectionBuildError) {
      log.error(urlConnectionBuildError.getMessage());
      return FailedConnection.getInstance();
    }
    return new ConnectorImpl(connection);
  }

  private void checkURL() throws URLConnectionBuildError {
    if (url == null || url.isEmpty()) {
      throw new URLConnectionBuildError("Empty URL for Trigger");
    }
  }

  private void openConnection() throws URLConnectionBuildError {
    try {
      URLConnection conn = new URL(url).openConnection();
      if (conn instanceof HttpURLConnection) {
        connection = (HttpURLConnection) conn;
      } else {
        throw new URLConnectionBuildError("not an Http connection: " + url);
      }
    } catch (IOException e) {
      throw new URLConnectionBuildError("unable to open a connection to " + url, e);
    }
  }

  private void setMethod() throws URLConnectionBuildError {
    try {
      connection.setRequestMethod("POST");
    } catch (ProtocolException e) {
      throw new URLConnectionBuildError("unable to set method to POST", e);
    }
  }

  private void setHeaders() {
    setAuthentication();
    setEncoding();
  }

  private void setAuthentication() {
    connection.setRequestProperty("Authorization", "Basic " + getAuthenticationString());
  }

  private void setEncoding() {
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("Accept-Charset", "UTF-8");
  }

  private String getAuthenticationString() {
    final String user = settings.getUser();
    final String password = settings.getPassword();
    final String authString = user + ":" + password;
    final byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
    return new String(authEncBytes);
  }
}
