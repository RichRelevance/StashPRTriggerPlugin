package ut.com.richrelevance.stash.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.atlassian.stash.comment.Comment;
import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.Trigger;
import com.richrelevance.stash.plugin.TriggerImpl;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

/**
 * Created by dsobral on 1/31/14.
 */
public class TriggerTest {

  private final PullRequestTriggerSettings settingsEnabled = new ImmutablePullRequestTriggerSettings(true, "fake url", "fake user",
    "fake password", "Retest Message");
  private final PullRequestTriggerSettings settingsDisabled = new ImmutablePullRequestTriggerSettings(false, "fake url",
    "fake user",
    "fake password", "Retest Message");

  @Test
  public void askedForRetestReturnsTrueIfMessageMatchesSettingsTest() {
    PullRequestTriggerSettingsService settingsService = mock(PullRequestTriggerSettingsService.class);
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn("Retest Message");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(settingsService.getPullRequestTriggerSettings(repository)).thenReturn(settingsEnabled);

    Trigger trigger = new TriggerImpl(settingsService);

    assertTrue("Trigger returned false on a comment that asked for retest", trigger.askedForRetest(event));
  }

  @Test
  public void askedForRetestReturnsFalseIfMessageDoesNotMatchSettingsTest() {
    PullRequestTriggerSettingsService settingsService = mock(PullRequestTriggerSettingsService.class);
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn("Do Not Retest");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(settingsService.getPullRequestTriggerSettings(repository)).thenReturn(settingsEnabled);

    Trigger trigger = new TriggerImpl(settingsService);

    assertFalse("Trigger returned true on a comment that did not ask for retest", trigger.askedForRetest(event));
  }

  @Test
  public void askedForRetestReturnsFalseIfRepositoryDisabledTest() {
    PullRequestTriggerSettingsService settingsService = mock(PullRequestTriggerSettingsService.class);
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn("Retest Message");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(settingsService.getPullRequestTriggerSettings(repository)).thenReturn(settingsDisabled);

    Trigger trigger = new TriggerImpl(settingsService);

    assertFalse("Trigger returned true on a disabled repository", trigger.askedForRetest(event));
  }
}
