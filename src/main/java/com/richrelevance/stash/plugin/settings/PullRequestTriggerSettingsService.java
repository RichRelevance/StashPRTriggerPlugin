package com.richrelevance.stash.plugin.settings;

import com.atlassian.stash.repository.Repository;

public interface PullRequestTriggerSettingsService {

  PullRequestTriggerSettings getPullRequestTriggerSettings(Repository repository);

  PullRequestTriggerSettings setPullRequestTriggerSettings(Repository repository, PullRequestTriggerSettings settings);
}
