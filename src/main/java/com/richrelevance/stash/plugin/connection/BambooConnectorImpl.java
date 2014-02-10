package com.richrelevance.stash.plugin.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class is *not* thread-safe, as it depends on mutable state on the instance to
 * execute the "get" method.
*/
public class BambooConnectorImpl implements BambooConnector {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BambooConnectorImpl.class);

  private HttpURLConnection connection;
  private String user;
  private String password;

  public BambooConnectorImpl() {
  }

  @Override
  public String get(String url, String user, String password) {
    this.user = user;
    this.password = password;
    try {
      checkURL(url);
      openConnection(url);
      setHeaders();
      setMethod();
      connect();
      checkResult();
      return getResponse();
    } catch (URLConnectionBuildError urlConnectionBuildError) {
      log.error(urlConnectionBuildError.getMessage());
      return "";
    }
  }

  private void checkURL(String url) throws URLConnectionBuildError {
    if (url == null || url.isEmpty()) {
      throw new URLConnectionBuildError("Empty URL for Trigger");
    }
  }

  private void openConnection(String url) throws URLConnectionBuildError {
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
    final String authString = user + ":" + password;
    final byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
    return new String(authEncBytes);
  }

  private void connect() throws URLConnectionBuildError {
    try {
      connection.connect();
    } catch (SocketTimeoutException e) {
      throw new URLConnectionBuildError("timeout connecting to " + connection.getURL(), e);
    } catch (IOException e) {
      throw new URLConnectionBuildError("unable to connect to " + connection.getURL(), e);
    }
  }

  private void checkResult() throws URLConnectionBuildError {
    try {
      final int responseCode = connection.getResponseCode();

      if (responseCode == -1) {
        throw new URLConnectionBuildError("response from " + connection.getURL() + "is not a valid http response");
      } else if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new URLConnectionBuildError("failed to trigger " + connection.getURL() + ": " + responseCode +
          "(" + connection.getResponseMessage() + ")");
      }
    } catch (IOException e) {
      throw new URLConnectionBuildError("unable to get response code from connection to " + connection.getURL(), e);
    }
  }

  private String getResponse() throws URLConnectionBuildError {
    String response;

    try {
      final String connectionEncoding = connection.getContentEncoding();
      final String encoding = connectionEncoding != null ? connectionEncoding : "UTF-8";
      final InputStream buildRequisitionResponse = connection.getInputStream();

      response = IOUtils.toString(buildRequisitionResponse, encoding);

      log.info("response from " + connection.getURL() + ": " + response);

      buildRequisitionResponse.close();
    } catch (IOException e) {
      throw new URLConnectionBuildError("unable to get POST response", e);
    }

    return response;
  }
}
