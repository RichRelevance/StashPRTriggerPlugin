package com.richrelevance.stash.plugin.trigger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richrelevance.stash.plugin.connection.BambooConnector;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;

public class BuildTriggererImpl implements BuildTriggerer {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BuildTriggererImpl.class);

  private final BambooConnector bambooConnector;
  private final String urlTemplate;

  public BuildTriggererImpl(BambooConnector bambooConnector) {
    this.bambooConnector = bambooConnector;
    this.urlTemplate = System.getProperty("prhook.getUrl", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable" +
      ".prnumber=$PRNUMBER&os_authType=basic");
  }

  @Override
  public void invoke(long prNumber, PullRequestTriggerSettings settings, BranchSettings branchSettings) {
    log.info(this.toString()+" triggering pull request "+ prNumber);
    String url = getUrl(prNumber, settings, branchSettings);
    bambooConnector.get(url, settings.getUser(), settings.getPassword());
  }

  private static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private String getUrl(long prNumber, PullRequestTriggerSettings settings, BranchSettings branchSettings) {
    final String plan = branchSettings.getPlan();
    final String baseUrl = urlTemplate.replace("$BASEURL", settings.getUrl());
    final String urlWithPlan = baseUrl.replace("$PLAN", urlEncode(plan));
    final String fullUrl = urlWithPlan.replace("$PRNUMBER", Long.toString(prNumber));

    return fullUrl;
  }
}
