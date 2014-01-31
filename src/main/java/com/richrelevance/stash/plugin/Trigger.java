package com.richrelevance.stash.plugin;

import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;

/**
 * Created by dsobral on 1/30/14.
 */
public interface Trigger {
  boolean askedForRetest(PullRequestCommentAddedEvent event);

  void triggerPullRequest(PullRequestEvent pullRequestEvent);
}
