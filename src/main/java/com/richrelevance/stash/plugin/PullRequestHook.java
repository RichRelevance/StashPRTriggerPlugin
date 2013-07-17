package com.richrelevance.stash.plugin;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.*;
import com.atlassian.stash.pull.PullRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class PullRequestHook {

  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);
  private String BASEURL = System.getProperty("prhook.baseUrl", "http://localhost/bamboo");
  private String URL = System.getProperty("prhook.url", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable.prnumber=$PRNUMBER&os_authType=basic");
  private String USER = System.getProperty("prhook.user", "user");
  private String PASSWORD = System.getProperty("prhook.password", "password");

  @EventListener
  public void onPullRequestOpen(PullRequestOpenedEvent event) {
    triggerPullRequest(event.getPullRequest());
  }

  @EventListener
  public void onPullRequestReopen(PullRequestReopenedEvent event) {
    triggerPullRequest(event.getPullRequest());
  }

  @EventListener
  public void onPullRequestRescope(PullRequestRescopedEvent event) { }

  @EventListener
  public void onPullRequestComment(PullRequestCommentAddedEvent event) {}

  private void triggerPullRequest(PullRequest pullRequest) {
    String url = getUrl(pullRequest);
    String authStringEnc = getAuthenticationString();
    try {
      HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
      connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestMethod("POST");
      InputStream buildRequisition = connection.getInputStream();
      String response = IOUtils.toString(buildRequisition, "UTF-8");
      log.info("Build Trigger Response: " + response);
      buildRequisition.close();
    } catch (Exception e) {
      log.error("Error triggering: " + url, e);
    }
  }

  private String getUrl(PullRequest pullRequest) {
    Long prNumber = pullRequest.getId();
    return URL.replace("$BASEURL", BASEURL).replace("$PLAN", urlEncode("RRCORE-PRTEST")).replace("$PRNUMBER", prNumber.toString());
  }

  private String getAuthenticationString() {
    String authString = USER + ":" + PASSWORD;
    byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
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
