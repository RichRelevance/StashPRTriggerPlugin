package com.richrelevance.stash.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.*;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
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

  private final String URL = System.getProperty("prhook.getUrl", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable.prnumber=$PRNUMBER&os_authType=basic");

  private final PullRequestTriggerSettingsService service;

  public PullRequestHook(PullRequestTriggerSettingsService pullRequestTriggerSettingsService) {
    this.service = pullRequestTriggerSettingsService;
  }

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
    final Repository repository = pullRequest.getToRef().getRepository();
    final PullRequestTriggerSettings settings = service.getPullRequestTriggerSettings(repository);

    if (!settings.isEnabled())
      return;

    final String url = getUrl(pullRequest, settings);

    if (url != null && !url.isEmpty()) {
      String authStringEnc = getAuthenticationString(settings);

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

  private String getUrl(PullRequest pullRequest, PullRequestTriggerSettings settings) {
    final Long prNumber = pullRequest.getId();
    final String baseURL = settings.getUrl();

    if (prNumber != null) {
      return URL.replace("$BASEURL", baseURL).replace("$PLAN", urlEncode("RRCORE-PRTEST")).replace("$PRNUMBER", prNumber.toString());
    } else {
      log.error("id of pull request is null:" + pullRequest);
      return "";
    }
  }

  private String getAuthenticationString(PullRequestTriggerSettings settings) {
    final String user = settings.getUser();
    final String password = settings.getPassword();
    final String authString = user + ":" + password;
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
