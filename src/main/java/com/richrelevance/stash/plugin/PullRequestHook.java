package com.richrelevance.stash.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.event.api.EventListener;
import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.richrelevance.stash.plugin.trigger.Trigger;

/**
 * This class is responsible for hooking into pull request events from Stash, deciding whether
 * the event calls for an automatic or on demand (by comment) trigger, and sending it to the
 * proper trigger if so.
 */
public class PullRequestHook {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);

  private final Trigger trigger;

  public PullRequestHook(Trigger trigger) {
    this.trigger = trigger;
  }

  @EventListener
  public void onPullRequestOpen(PullRequestOpenedEvent event) {
    trigger.automaticTrigger(event);
  }

  @EventListener
  public void onPullRequestReopen(PullRequestReopenedEvent event) {
    trigger.automaticTrigger(event);
  }

  @EventListener
  public void onPullRequestRescope(PullRequestRescopedEvent event) {
    final String previousHash = event.getPreviousFromHash();
    final String currentHash = event.getPullRequest().getFromRef().getLatestChangeset();

    if (!previousHash.equals(currentHash))
      trigger.automaticTrigger(event);
  }

  @EventListener
  public void onPullRequestComment(PullRequestCommentAddedEvent event) {
    trigger.onDemandTrigger(event);
  }
}
