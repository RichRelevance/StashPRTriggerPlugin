package com.richrelevance.stash.plugin.trigger;

import com.atlassian.stash.pull.PullRequest;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;

/**
 * Created by dsobral on 2/3/14.
 *
 * This class decides what URL to call to trigger build, and with
 * what parameters based on the settings, and sending it to the
 * Bamboo connector.
 */
public interface BuildTriggerer {
  void invoke(long prNumber, PullRequestTriggerSettings settings, BranchSettings branchSettings);
}
