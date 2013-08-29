package com.richrelevance.stash.plugin;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class PullRequestHook {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);


  private final PullRequestTriggerSettingsService service;

  public PullRequestHook(PullRequestTriggerSettingsService pullRequestTriggerSettingsService) {
    this.service = pullRequestTriggerSettingsService;
  }

  @EventListener
  public void onPullRequestOpen(PullRequestOpenedEvent event) {
    triggerPullRequest(event);
  }

  @EventListener
  public void onPullRequestReopen(PullRequestReopenedEvent event) {
    triggerPullRequest(event);
  }

  @EventListener
  public void onPullRequestRescope(PullRequestRescopedEvent event) {
    final String previousHash = event.getPreviousFromHash();
    final String currentHash = event.getPullRequest().getFromRef().getLatestChangeset();

    if (!previousHash.equals(currentHash))
      triggerPullRequest(event);
  }

  @EventListener
  public void onPullRequestComment(PullRequestCommentAddedEvent event) {
    if (askedForRetest(event))
      triggerPullRequest(event);
  }

  private boolean askedForRetest(PullRequestCommentAddedEvent event) {
    final String comment = event.getComment().getText();
    final Repository repository = event.getPullRequest().getToRef().getRepository();
    final PullRequestTriggerSettings settings = service.getPullRequestTriggerSettings(repository);
    final String retestMsg = settings.getRetestMsg();

    final Pattern pattern;
    try {
      pattern = Pattern.compile(retestMsg);
    } catch (PatternSyntaxException e) {
      final String branchName = event.getPullRequest().getToRef().getId();
      log.error(String.format("Invalid retest regex for repository %s, branch %s: %s", repository, branchName, retestMsg), e);
      return false;
    }

    return pattern.matcher(comment).find();
  }

  private void triggerPullRequest(PullRequestEvent pullRequestEvent) {
    new BuildTrigger(pullRequestEvent.getPullRequest(), service).invoke();
  }
}
