package ut.com.richrelevance.stash.plugin;

import org.junit.Test;
import static org.mockito.Mockito.*;

import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.event.pull.PullRequestRescopedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.richrelevance.stash.plugin.PullRequestHook;
import com.richrelevance.stash.plugin.trigger.Trigger;

/**
 * Created by dsobral on 1/30/14.
 */
public class PullRequestHookTest {
  @Test
  public void automaticTriggerIsCalledOnPullRequestOpenTest() {
    Trigger trigger = mock(Trigger.class);
    PullRequestOpenedEvent event = mock(PullRequestOpenedEvent.class);
    PullRequestHook pullRequestHook = new PullRequestHook(trigger);

    pullRequestHook.onPullRequestOpen(event);

    verify(trigger).automaticTrigger(event);
  }

  @Test
  public void automaticTriggerIsCalledOnPullRequestReOpenTest() {
    Trigger trigger = mock(Trigger.class);
    PullRequestReopenedEvent event = mock(PullRequestReopenedEvent.class);
    PullRequestHook pullRequestHook = new PullRequestHook(trigger);

    pullRequestHook.onPullRequestReopen(event);

    verify(trigger).automaticTrigger(event);
  }

  @Test
  public void automaticTriggerIsCalledOnRescopeChangingHashTest() {
    Trigger trigger = mock(Trigger.class);
    PullRequestRescopedEvent event = mock(PullRequestRescopedEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef fromRef = mock(PullRequestRef.class);

    when(event.getPreviousFromHash()).thenReturn("X");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getFromRef()).thenReturn(fromRef);
    when(fromRef.getLatestChangeset()).thenReturn("Y");

    PullRequestHook pullRequestHook = new PullRequestHook(trigger);

    pullRequestHook.onPullRequestRescope(event);

    verify(trigger).automaticTrigger(event);
  }

  @Test
  public void triggerIsNotCalledOnRescopesWithSameHashTest() {
    Trigger trigger = mock(Trigger.class);
    PullRequestRescopedEvent event = mock(PullRequestRescopedEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef fromRef = mock(PullRequestRef.class);

    when(event.getPreviousFromHash()).thenReturn("X");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getFromRef()).thenReturn(fromRef);
    when(fromRef.getLatestChangeset()).thenReturn("X");

    PullRequestHook pullRequestHook = new PullRequestHook(trigger);

    pullRequestHook.onPullRequestRescope(event);

    verify(trigger, never()).automaticTrigger(event);
  }

  @Test
  public void onDemandTriggerIsCalledWhenCommentsAreAddedTest() {
    Trigger trigger = mock(Trigger.class);
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);

    PullRequestHook pullRequestHook = new PullRequestHook(trigger);

    pullRequestHook.onPullRequestComment(event);

    verify(trigger).onDemandTrigger(event);
  }
}
