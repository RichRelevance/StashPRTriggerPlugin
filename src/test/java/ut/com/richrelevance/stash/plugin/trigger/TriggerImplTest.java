package ut.com.richrelevance.stash.plugin.trigger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
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
import com.richrelevance.stash.plugin.trigger.BuildTriggerer;
import com.richrelevance.stash.plugin.trigger.Trigger;
import com.richrelevance.stash.plugin.trigger.TriggerImpl;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutableBranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

/**
 * Created by dsobral on 1/31/14.
 */
public class TriggerImplTest {

  private static final String user = "fake user";
  private static final String password = "fake password";
  private static final String url = "fakeUrl";
  private static final String retestMsg = "Retest Message";
  private static final String retestRegex = "(?i)retest this,? please|klaatu barada nikto";
  private static final PullRequestTriggerSettings settingsEnabled = new ImmutablePullRequestTriggerSettings(true, url,
    user, password, retestMsg);

  private static final PullRequestTriggerSettings settingsRegexEnabled = new ImmutablePullRequestTriggerSettings(true, url,
    user, password, retestRegex);

  private static final PullRequestTriggerSettings settingsDisabled = new ImmutablePullRequestTriggerSettings(false, url,
    user, password, retestMsg);

  private static final String branchName = "default branch";
  private static final String planName = "StandardPlan";
  private static final ImmutableBranchSettings immutableBranchSettings = new ImmutableBranchSettings(branchName,
    planName);

  private static final PullRequestTriggerSettingsService settingsServiceEnabled = new SettingsService(settingsEnabled,
    immutableBranchSettings);

  private static final PullRequestTriggerSettingsService settingsServiceRegexEnabled = new SettingsService(settingsRegexEnabled,
    immutableBranchSettings);

  private static final PullRequestTriggerSettingsService settingsServiceDisabled = new SettingsService(settingsDisabled,
    immutableBranchSettings);

  @Test
  public void automaticTriggerBuildAlwaysBuildsTest() {
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    trigger.automaticTrigger(event);

    verify(buildTriggerer).invoke(1L, settingsEnabled, immutableBranchSettings);
  }

  @Test
  public void onDemandTriggerBuildsIfMessageMatchesSettingsTest() {
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
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    trigger.onDemandTrigger(event);

    verify(buildTriggerer).invoke(1L, settingsEnabled, immutableBranchSettings);
  }

  @Test
  public void onDemandTriggerBuildsIfMessageMatchesRegexTest() {
    PullRequestCommentAddedEvent event = mock(PullRequestCommentAddedEvent.class);
    Comment comment = mock(Comment.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);

    when(event.getComment()).thenReturn(comment);
    when(comment.getText()).thenReturn("KLAATU BARADA NIKTO");
    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceRegexEnabled, buildTriggerer);

    trigger.onDemandTrigger(event);

    verify(buildTriggerer).invoke(1L, settingsRegexEnabled, immutableBranchSettings);
  }

  @Test
  public void onDemandDoesNotTriggerBuildsIfMessageDoesNotMatchSettingsTest() {
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

    trigger.onDemandTrigger(event);

    verify(buildTriggerer, never()).invoke(anyLong(), any(PullRequestTriggerSettings.class), any(BranchSettings.class));
  }

  @Test
  public void triggerBuildDoesNotTriggerBuildsIfRepositoryDisabledTest() {
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);

    Trigger trigger = new TriggerImpl(settingsServiceDisabled, buildTriggerer);

    trigger.triggerBuild(event);

    verify(buildTriggerer, never()).invoke(anyLong(), any(PullRequestTriggerSettings.class), any(BranchSettings.class));
  }

  @Test
  public void triggerBuildDoesNotDoTriggerBuildsIfBranchSettingsIsNullTest() {
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn("another branch");

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    trigger.triggerBuild(event);

    verify(buildTriggerer, never()).invoke(anyLong(), any(PullRequestTriggerSettings.class), any(BranchSettings.class));
  }

  @Test
  public void triggerBuildBuildsIfEnabledAndBranchSettingsExistTest() {
    BuildTriggerer buildTriggerer = mock(BuildTriggerer.class);
    PullRequestEvent event = mock(PullRequestEvent.class);
    PullRequest pullRequest = mock(PullRequest.class);
    PullRequestRef ref = mock(PullRequestRef.class);
    Repository repository = mock(Repository.class);

    when(event.getPullRequest()).thenReturn(pullRequest);
    when(pullRequest.getToRef()).thenReturn(ref, ref);
    when(ref.getRepository()).thenReturn(repository);
    when(ref.getId()).thenReturn(branchName);
    when(pullRequest.getId()).thenReturn(1L);

    Trigger trigger = new TriggerImpl(settingsServiceEnabled, buildTriggerer);

    trigger.triggerBuild(event);

    verify(buildTriggerer).invoke(1L, settingsEnabled, immutableBranchSettings);
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
      return branchName.equals(TriggerImplTest.branchName) ? branchSettings : null;
    }
  }

}
