package com.richrelevance.stash.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class PullRequestHook {
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

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
    String comment = event.getComment().getText();

    if (comment.contains("Retest this please"))
      triggerPullRequest(event);
  }

  private void triggerPullRequest(PullRequestEvent pullRequestEvent) {
    new BuildTrigger(pullRequestEvent.getPullRequest(), service).invoke();
  }
}
