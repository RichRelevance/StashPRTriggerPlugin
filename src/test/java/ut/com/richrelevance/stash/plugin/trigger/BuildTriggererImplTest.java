package ut.com.richrelevance.stash.plugin.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.richrelevance.stash.plugin.trigger.BuildTriggerer;
import com.richrelevance.stash.plugin.trigger.BuildTriggererImpl;
import com.richrelevance.stash.plugin.connection.BambooConnector;
import com.richrelevance.stash.plugin.settings.ImmutableBranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;

/**
 * Created by dsobral on 2/6/14.
 */
public class BuildTriggererImplTest {
  private static final String user = "fake user";
  private static final String password = "fake password";
  private static final String url = "fakeUrl";
  private static final String retestMsg = "Retest Message";
  private static final PullRequestTriggerSettings settingsEnabled =
    new ImmutablePullRequestTriggerSettings(true, url, user, password);

  private static final String branchName = "default branch";
  private static final String planName = "StandardPlan";
  private static final ImmutableBranchSettings immutableBranchSettings =
    new ImmutableBranchSettings(true, branchName, planName, retestMsg);

  @Test
  public void testInvoke() {
    BambooConnector bambooConnector = mock(BambooConnector.class);

    BuildTriggerer triggerer = new BuildTriggererImpl(bambooConnector);

    triggerer.invoke(1L, settingsEnabled, immutableBranchSettings);

    verify(bambooConnector).get(url + "/rest/api/latest/queue/" + planName +
      "?bamboo.variable.prnumber=1&os_authType=basic", user, password);

  }
}
