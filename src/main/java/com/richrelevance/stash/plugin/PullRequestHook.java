package com.richrelevance.stash.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.*;
import com.atlassian.stash.pull.PullRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import static java.net.HttpURLConnection.HTTP_OK;

public class PullRequestHook {
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);

  private final String BASEURL = System.getProperty("prhook.baseUrl", "http://localhost/bamboo");
  private final String URL = System.getProperty("prhook.getUrl", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable.prnumber=$PRNUMBER&os_authType=basic");
  private final String USER = System.getProperty("prhook.getUser", "getUser");
  private final String PASSWORD = System.getProperty("prhook.getPassword", "getPassword");

  @EventListener
  public void onPullRequestOpen(PullRequestOpenedEvent event) {
    triggerPullRequest(event.getPullRequest());
  }

  @EventListener
  public void onPullRequestReopen(PullRequestReopenedEvent event) {
    triggerPullRequest(event.getPullRequest());
  }

  @EventListener
  public void onPullRequestRescope(PullRequestRescopedEvent event) {
  }

  @EventListener
  public void onPullRequestComment(PullRequestCommentAddedEvent event) {
  }

  private void triggerPullRequest(PullRequest pullRequest) {
    final String url = getUrl(pullRequest);
    if (url != null && !url.isEmpty()) {
      String authStringEnc = getAuthenticationString();

      final HttpURLConnection connection;
      try {
        URLConnection conn = new java.net.URL(url).openConnection();
        if (conn instanceof HttpURLConnection) {
          connection = (HttpURLConnection) conn;
        } else {
          log.error("not an Http connection: " + url);
          return;
        }
      } catch (IOException e) {
        log.error("unable to open a connection to " + url, e);
        return;
      }

      connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("Accept-Charset", "UTF-8");

      try {
        connection.setRequestMethod("POST");
      } catch (ProtocolException e) {
        log.error("unable to set method to POST", e);
        return;
      }

      try {
        connection.connect();
      } catch (SocketTimeoutException e) {
        log.error("timeout connecting to " + url, e);
      } catch (IOException e) {
        log.error("unable to connect to " + url, e);
      }

      try {
        final int responseCode = connection.getResponseCode();

        if (responseCode == -1) {
          log.error("response from " + url + "is not a valid http response");
          return;
        } else if (responseCode != HTTP_OK) {
          log.error("failed to trigger " + url + ": " + responseCode + "(" + connection.getResponseMessage() + ")");
          return;
        }
      } catch (IOException e) {
        log.error("unable to get response code from connection to " + url);
        return;
      }

      try {
        String encoding = connection.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;

        final InputStream buildRequisitionResponse = connection.getInputStream();
        final String response = IOUtils.toString(buildRequisitionResponse, encoding);
        log.info("response from " + url + ": " + response);
        buildRequisitionResponse.close();
      } catch (IOException e) {
        log.error("unable to get POST response", e);
      }
    } else {
      log.error("empty trigger getUrl");
    }
  }

  private String getUrl(PullRequest pullRequest) {
    final Long prNumber = pullRequest.getId();
    if (prNumber != null) {
      return URL.replace("$BASEURL", BASEURL).replace("$PLAN", urlEncode("RRCORE-PRTEST")).replace("$PRNUMBER", prNumber.toString());
    } else {
      log.error("id of pull request is null:" + pullRequest);
      return "";
    }
  }

  private String getAuthenticationString() {
    final String authString = USER + ":" + PASSWORD;
    final byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
    return new String(authEncBytes);
  }

  private static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
