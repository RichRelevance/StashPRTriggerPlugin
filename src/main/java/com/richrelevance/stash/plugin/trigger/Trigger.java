package com.richrelevance.stash.plugin.trigger;

import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;

/**
 * Created by dsobral on 1/30/14.
 *
 * This class decides, based on user settings, whether the pull request event
 * should trigger a build or not, and calling the build triggerer if so.
 */
public interface Trigger {
  /**
   * Treats a pull request event that leads to an automatic trigger.
   *
   * This method calls triggerBuild, which does further validation.
   */
  void automaticTrigger(PullRequestEvent pullRequestEvent);

  /**
   * Treats a pull request event that leads to an on demand trigger.
   *
   * This method calls triggerBuild, which does further validation.
   */
  void onDemandTrigger(PullRequestCommentAddedEvent pullRequestEvent);

  /**
   * Trigger a build, irrespective of the type of event that
   * led to it, if build trigger is enabled on the settings
   * for that repository, and there's at least one enabled branch
   * configuration for it.
   *
   * This method is used by automaticTrigger and onDemandTrigger.
   */
  void triggerBuild(PullRequestEvent pullRequestEvent);
}
