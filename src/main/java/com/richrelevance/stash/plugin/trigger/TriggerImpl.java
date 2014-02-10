package com.richrelevance.stash.plugin.trigger;

import java.util.List;
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
    triggerBuild(pullRequestEvent, AutomaticPredicate.instance);
  }

  @Override
  public void onDemandTrigger(PullRequestCommentAddedEvent pullRequestEvent) {
    triggerBuild(pullRequestEvent, new OnDemandPredicate(pullRequestEvent));
  }

  @Override
  public void triggerBuild(PullRequestEvent pullRequestEvent, BranchPredicate predicate) {
    final PullRequest pullRequest = pullRequestEvent.getPullRequest();
    final PullRequestTriggerSettings settings = getSettings(pullRequest);
    final Repository repository = getRepository(pullRequest);
    final String branchName = pullRequest.getToRef().getId();
    final List<BranchSettings> branchSettingsList = service.getBranchSettingsForBranch(repository, branchName);
    final Long prNumber = pullRequest.getId();

    if (prNumber != null) {
      if (settings.isEnabled()) {
        for (BranchSettings branchSettings : branchSettingsList) {
          if (predicate.matches(branchSettings))
            buildTriggerer.invoke(prNumber, settings, branchSettings);
        }
      }
    } else {
      log.error("id of pull request is null: " + pullRequest);
    }
  }

  private PullRequestTriggerSettings getSettings(PullRequest pullRequest) {
    return service.getPullRequestTriggerSettings(getRepository(pullRequest));
  }

  private Repository getRepository(PullRequest pullRequest) {
    return pullRequest.getToRef().getRepository();
  }

  public static class AutomaticPredicate implements BranchPredicate {
    public static final AutomaticPredicate instance = new AutomaticPredicate();

    private AutomaticPredicate() {
    }

    @Override
    public boolean matches(BranchSettings branchSettings) {
      return true;
    }
  }

  private static class OnDemandPredicate implements BranchPredicate {

    private final String comment;

    public OnDemandPredicate(PullRequestCommentAddedEvent event) {
      comment = event.getComment().getText();
    }

    @Override
    public boolean matches(BranchSettings branchSettings) {
      final String retestMsg = branchSettings.getRetestMsg();
      final Pattern pattern;

      if (retestMsg.isEmpty())
        return false;

      try {
        pattern = Pattern.compile(retestMsg);
      } catch (PatternSyntaxException e) {
        log.error(String.format("Invalid retest message regex for branch $s: %s", branchSettings.getName(), retestMsg), e);
        return false;
      }

      return pattern.matcher(comment).find();
    }
  }
}