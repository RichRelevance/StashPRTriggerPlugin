package com.richrelevance.stash.plugin;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class TriggerImpl implements Trigger {
  private static final Logger log = LoggerFactory.getLogger(TriggerImpl.class);

  public final PullRequestTriggerSettingsService service;

  public TriggerImpl(PullRequestTriggerSettingsService service) {
    this.service = service;
  }

  @Override
  public boolean askedForRetest(PullRequestCommentAddedEvent event) {
    final String comment = event.getComment().getText();
    final Repository repository = event.getPullRequest().getToRef().getRepository();
    final PullRequestTriggerSettings settings = service.getPullRequestTriggerSettings(repository);

    if (!settings.isEnabled())
      return false;

    final String retestMsg = settings.getRetestMsg();

    final Pattern pattern;
    try {
      pattern = Pattern.compile(retestMsg);
    } catch (PatternSyntaxException e) {
      final String branchName = event.getPullRequest().getToRef().getId();
      log.error(String.format("Invalid retest regex for repository %s, branch %s: %s", repository, branchName, retestMsg), e);
      return false;
    }

    return pattern.matcher(comment).find();
  }

  @Override
  public void triggerPullRequest(PullRequestEvent pullRequestEvent) {
    new BuildTrigger(pullRequestEvent.getPullRequest(), service).invoke();
  }
}