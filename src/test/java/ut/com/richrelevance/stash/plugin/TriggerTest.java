package ut.com.richrelevance.stash.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.atlassian.stash.comment.Comment;
import com.atlassian.stash.event.pull.PullRequestCommentAddedEvent;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.pull.PullRequestRef;
import com.atlassian.stash.repository.Repository;
import com.richrelevance.stash.plugin.BuildTriggerer;
import com.richrelevance.stash.plugin.BuildTriggererImpl;
import com.richrelevance.stash.plugin.Trigger;
import com.richrelevance.stash.plugin.TriggerImpl;
import com.richrelevance.stash.plugin.admin.PullRequestSettingServlet;
import com.richrelevance.stash.plugin.connection.BambooConnector;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutableBranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

/**
 * Created by dsobral on 1/31/14.
 */
public class TriggerTest {

  private static final String user = "fake user";
  private static final String password = "fake password";
  private static final String url = "fakeUrl";
  private static final String retestMsg = "Retest Message";
  private static final PullRequestTriggerSettings settingsEnabled = new ImmutablePullRequestTriggerSettings(true, url,
    user, password, retestMsg);

  private static final PullRequestTriggerSettings settingsDisabled = new ImmutablePullRequestTriggerSettings(false, url,
    user, password, retestMsg);

  private static final String branchName = "default branch";
  private static final String planName = "StandardPlan";
  private static final ImmutableBranchSettings immutableBranchSettings = new ImmutableBranchSettings(branchName,
    planName);

  private static final PullRequestTriggerSettingsService settingsServiceEnabled = new SettingsService(settingsEnabled,
    immutableBranchSettings);

  private static final PullRequestTriggerSettingsService settingsServiceDisabled = new SettingsService(settingsDisabled,
    immutableBranchSettings);

  @Test
  public void askedForRetestReturnsTrueIfMessageMatchesSettingsTest() {
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn(retestMsg);
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    assertTrue("Trigger returned false on a comment that asked for retest", trigger.askedForRetest(event));
  }

  @Test
  public void askedForRetestReturnsFalseIfMessageDoesNotMatchSettingsTest() {
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn("Do Not Retest");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    assertFalse("Trigger returned true on a comment that did not ask for retest", trigger.askedForRetest(event));
  }

  @Test
  public void askedForRetestReturnsFalseIfRepositoryDisabledTest() {
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn(retestMsg);
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);

    Trigger trigger = new TriggerImpl(settingsServiceDisabled, buildTriggerer);

    assertFalse("Trigger returned true on a disabled repository", trigger.askedForRetest(event));
  }

  @Test
  public void triggeringDoesNotDoAnythingIfRepositoryIsDisabledTest() {
    BambooConnector bambooConnector = mock(BambooConnector.class);
    BuildTriggerer triggerer = new BuildTriggererImpl(settingsServiceDisabled, bambooConnector);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceDisabled, triggerer);

    trigger.triggerPullRequest(event);

    verify(bambooConnector, never()).get(anyString(), anyString(), anyString());
  }

  @Test
  public void triggeringDoesNotDoAnythingIfBranchSettingsIsNullTest() {
    BambooConnector bambooConnector = mock(BambooConnector.class);
    BuildTriggerer triggerer = new BuildTriggererImpl(settingsServiceEnabled, bambooConnector);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn("another branch");
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, triggerer);

    trigger.triggerPullRequest(event);

    verify(bambooConnector, never()).get(anyString(), anyString(), anyString());
  }

  @Test
  public void triggeringCallsTheBambooConnectorTest() {
    BambooConnector bambooConnector = mock(BambooConnector.class);
    BuildTriggerer triggerer = new BuildTriggererImpl(settingsServiceEnabled, bambooConnector);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, triggerer);

    trigger.triggerPullRequest(event);

    verify(bambooConnector).get(url + "/rest/api/latest/queue/" + planName +
      "?bamboo.variable.prnumber=1&os_authType=basic", user, password);
  }

  private static class SettingsService implements PullRequestTriggerSettingsService {
    private final PullRequestTriggerSettings settings;
    private final BranchSettings branchSettings;

    public SettingsService(PullRequestTriggerSettings settings, BranchSettings branchSettings) {
      this.settings = settings;
      this.branchSettings = branchSettings;
    }

    @Override
    public PullRequestTriggerSettings getPullRequestTriggerSettings(Repository repository) {
      return settings;
    }

    @Override
    public PullRequestTriggerSettings setPullRequestTriggerSettings(Repository repository, PullRequestTriggerSettings settings) {
      return null;
    }

    @Override
    public List<BranchSettings> getBranchSettings(Repository repository) {
      List<BranchSettings> list = new ArrayList<BranchSettings>(1);
      list.add(branchSettings);
      return list;
    }

    @Override
    public void setBranch(Repository repository, String branchName, BranchSettings settings) {

    }

    @Override
    public void deleteBranch(Repository repository, String branchName) {

    }

    @Override
    public BranchSettings getBranchSettingsForBranch(Repository repository, String branchName) {
      return branchName.equals(TriggerTest.branchName) ? branchSettings : null;
    }
  }

}
