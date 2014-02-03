package com.richrelevance.stash.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.connection.BambooConnector;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class BuildTriggererImpl implements BuildTriggerer {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(BuildTriggererImpl.class);

  private PullRequestTriggerSettings settings;
  private BranchSettings branchSettings;
  private final PullRequestTriggerSettingsService service;
  private final BambooConnector bambooConnector;
  private final String urlTemplate;

  public BuildTriggererImpl(PullRequestTriggerSettingsService service, BambooConnector bambooConnector) {
    this.service = service;
    this.bambooConnector = bambooConnector;
    this.urlTemplate = System.getProperty("prhook.getUrl", "$BASEURL/rest/api/latest/queue/$PLAN?bamboo.variable" +
      ".prnumber=$PRNUMBER&os_authType=basic");

  }

  @Override
  public void invoke(PullRequest pullRequest) {
    String url = getUrl(pullRequest);

    if (!settings.isEnabled() || branchSettings == null) {
      return;
    }

    log.info(this.toString()+" triggering pull request "+pullRequest);

    bambooConnector.get(url, settings.getUser(), settings.getPassword());
  }

  private static String makeUrl(String urlTemplate, String baseURL, String plan, Long prNumber) {
    if (prNumber != null) {
      return urlTemplate.replace("$BASEURL", baseURL).replace("$PLAN", urlEncode(plan)).replace("$PRNUMBER",
        prNumber.toString());
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

  private String getUrl(PullRequest pullRequest) {
    final Repository repository = pullRequest.getToRef().getRepository();
    final String branchName = pullRequest.getToRef().getId();

    this.settings = service.getPullRequestTriggerSettings(repository);
    this.branchSettings = service.getBranchSettingsForBranch(repository, branchName);

    final String plan = branchSettings == null ? "INVALID" : branchSettings.getPlan();

    Long prNumber = pullRequest.getId();
    String url = makeUrl(urlTemplate, settings.getUrl(), plan, prNumber);

    if (prNumber == null) {
      log.error("id of pull request is null: " + pullRequest);
    }

    return url;
  }
}
