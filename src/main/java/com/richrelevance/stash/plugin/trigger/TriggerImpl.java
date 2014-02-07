package com.richrelevance.stash.plugin.trigger;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class TriggerImpl implements Trigger {
  private static final Logger log = LoggerFactory.getLogger(TriggerImpl.class);

  private final PullRequestTriggerSettingsService service;
  private BuildTriggerer buildTriggerer;

  public TriggerImpl(PullRequestTriggerSettingsService service, BuildTriggerer buildTriggerer) {
    this.service = service;
    this.buildTriggerer = buildTriggerer;
  }

  @Override
  public void automaticTrigger(PullRequestEvent pullRequestEvent) {
    triggerBuild(pullRequestEvent);
  }

  @Override
  public void onDemandTrigger(PullRequestCommentAddedEvent pullRequestEvent) {
    if (askedForRetest(pullRequestEvent))
      triggerBuild(pullRequestEvent);
  }

  @Override
  public void triggerBuild(PullRequestEvent pullRequestEvent) {
    final PullRequest pullRequest = pullRequestEvent.getPullRequest();
    final PullRequestTriggerSettings settings = getSettings(pullRequest);
    final Repository repository = getRepository(pullRequest);
    final String branchName = pullRequest.getToRef().getId();
    final BranchSettings branchSettings = service.getBranchSettingsForBranch(repository, branchName);
    final Long prNumber = pullRequest.getId();

    if (prNumber != null) {
      if (settings.isEnabled() && branchSettings != null)
        buildTriggerer.invoke(prNumber, settings, branchSettings);
    } else {
      log.error("id of pull request is null: " + pullRequest);
    }
  }

  private boolean askedForRetest(PullRequestCommentAddedEvent event) {
    final String comment = event.getComment().getText();
    final PullRequest pullRequest = event.getPullRequest();
    final PullRequestTriggerSettings settings = getSettings(pullRequest);

    final String retestMsg = settings.getRetestMsg();

    final Pattern pattern;
    try {
      pattern = Pattern.compile(retestMsg);
    } catch (PatternSyntaxException e) {
      final String branchName = pullRequest.getToRef().getId();
      log.error(String.format("Invalid retest regex for repository %s, branch %s: %s", getRepository(pullRequest), branchName,
        retestMsg), e);
      return false;
    }

    return pattern.matcher(comment).find();
  }

  private PullRequestTriggerSettings getSettings(PullRequest pullRequest) {
    return service.getPullRequestTriggerSettings(getRepository(pullRequest));
  }

  private Repository getRepository(PullRequest pullRequest) {
    return pullRequest.getToRef().getRepository();
  }
}