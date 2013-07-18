package com.richrelevance.stash.plugin;

import com.atlassian.stash.repository.Repository;

public interface PullRequestTriggerSettingsService {

  PullRequestTriggerSettings getPullRequestTriggerSettings(Repository repository);

  PullRequestTriggerSettings setPullRequestTriggerSettings(Repository repository, PullRequestTriggerSettings settings);
}
