package com.richrelevance.stash.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.pull.PullRequest;
import com.richrelevance.stash.plugin.connection.TriggerConnectionBuilder;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class BuildTrigger {
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BuildTrigger.class);

  private final PullRequestTriggerSettings settings;
  private final String url;
  private final Long prNumber;

  public BuildTrigger(PullRequest pullRequest, PullRequestTriggerSettingsService service) {
    final String urlTemplate = System.getProperty("prhook.getUrl", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable" +
      ".prnumber=$PRNUMBER&os_authType=basic");

    this.settings = service.getPullRequestTriggerSettings(pullRequest.getToRef().getRepository());
    this.prNumber = pullRequest.getId();
    this.url = makeUrl(urlTemplate, settings.getUrl(), prNumber);

    if (prNumber == null) {
      log.error("id of pull request is null:" + pullRequest);
    }
  }

  void invoke() {
    if (!settings.isEnabled())
      return;

    log.info(this.toString()+" triggering pull request");

    new TriggerConnectionBuilder(url, settings).createConnection().connect().checkResult().getResponse();
  }

  private static String makeUrl(String urlTemplate, String baseURL, Long prNumber) {
    if (prNumber != null) {
      return urlTemplate.replace("$BASEURL", baseURL).replace("$PLAN", urlEncode("RRCORE-PRTEST")).replace("$PRNUMBER", prNumber.toString());
    } else {
      return "";
    }
  }

  private static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return "BuildTrigger{prNumber=" + (prNumber == null ? "Unknown" : prNumber) + '}';
  }
}
