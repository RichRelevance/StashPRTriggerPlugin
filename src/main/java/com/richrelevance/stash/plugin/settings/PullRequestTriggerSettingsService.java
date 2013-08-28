package com.richrelevance.stash.plugin.settings;

import java.util.List;

import com.atlassian.stash.repository.Repository;

public interface PullRequestTriggerSettingsService {

  PullRequestTriggerSettings getPullRequestTriggerSettings(Repository repository);

  PullRequestTriggerSettings setPullRequestTriggerSettings(Repository repository, PullRequestTriggerSettings settings);

  List<BranchSettings> getBranchSettings(Repository repository);

  void setBranch(Repository repository, String branchName, BranchSettings settings);

  void deleteBranch(Repository repository, String branchName);
}
