package com.richrelevance.stash.plugin;

import com.atlassian.stash.pull.PullRequest;

/**
 * Created by dsobral on 2/3/14.
 */
public interface BuildTriggerer {
  void invoke(PullRequest pullRequest);
}
