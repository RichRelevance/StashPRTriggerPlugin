package ut.com.richrelevance.stash.plugin.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.collect.Lists;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.DefaultPullRequestTriggerSettingsService;
import com.richrelevance.stash.plugin.settings.ImmutableBranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.PluginMetadata;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;

/**
 * Created by dsobral on 2/3/14.
 */
public class SettingsTest {
  private static final String user = "fake user";
  private static final String password = "fake password";
  private static final String url = "fakeUrl";
  private static final String retestMsg = "Retest Message";
  private static final PullRequestTriggerSettings settingsEnabled = new ImmutablePullRequestTriggerSettings(true, url,
    user, password);

  private static final String branchName = "default branch";
  private static final String anotherBranchName = "another branch";
  private static final String planName = "StandardPlan";
  private static final BranchSettings immutableBranchSettings = new ImmutableBranchSettings(branchName,
    planName, retestMsg);
  private static final BranchSettings anotherBranchSettings = new ImmutableBranchSettings(anotherBranchName,
    "somethingElse", retestMsg);

  private static final Map<String, String> settingsMapEnabled = DefaultPullRequestTriggerSettingsService.serialize(settingsEnabled);
  private static final Map<String, String> branchSettingsMap = DefaultPullRequestTriggerSettingsService.serializeBranch(immutableBranchSettings);
  private static final Map<String, String> anotherBranchMap = DefaultPullRequestTriggerSettingsService.serializeBranch(anotherBranchSettings);

  @Test
  public void settingsAreDisabledByDefaultTest() {
    PullRequestTriggerSettings settings = new ImmutablePullRequestTriggerSettings();

    assertFalse("Settings are enabled by default, but should be disabled", settings.isEnabled());
  }

  // This property is now obtained through the UI layer
  @Test @Ignore
  public void retestThisPleaseIsTheDefaultRetestMessage() {
    BranchSettings branchSettings = immutableBranchSettings;

    final String retestMsg = branchSettings.getRetestMsg();

    assertTrue("Retest this please".matches(retestMsg));
  }

  @Test
  public void gettingSettingsForNewRepoReturnsDefaultSettingsTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    final PullRequestTriggerSettings settings = service.getPullRequestTriggerSettings(repository);

    assertEquals(new ImmutablePullRequestTriggerSettings(), settings);
  }

  @Test
  public void settingsCanBeSavedTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.put("1", settingsMapEnabled)).thenReturn(settingsMapEnabled);
    when(pluginSettings.get("1")).thenReturn(settingsMapEnabled);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    service.setPullRequestTriggerSettings(repository, settingsEnabled);
    final PullRequestTriggerSettings settings = service.getPullRequestTriggerSettings(repository);

    InOrder inOrder = inOrder(pluginSettings);
    inOrder.verify(pluginSettings).put("1", settingsMapEnabled);
    inOrder.verify(pluginSettings).get("1");

    assertEquals(settingsEnabled, settings);
    assertNotSame(new ImmutablePullRequestTriggerSettings(), settings);
  }

  @Test
  public void gettingBranchSettingsForNewRepoReturnsEmptyListTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    assertEquals("Branch settings list for new repo is not empty", 0, service.getBranchSettings(repository).size());
  }

  @Test
  public void gettingBranchSettingsForNewRepoAndBranchReturnsEmptyListTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    assertTrue("Branch settings retrievable in a new repo", service.getBranchSettingsForBranch(repository, branchName).isEmpty());
  }

  @Test
  public void branchSettingsCanBeSavedTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(new ArrayList<String>()).thenReturn(expectedBranchList());
    when(pluginSettings.put("branchList:1", expectedBranchList())).thenReturn(expectedBranchList());
    when(pluginSettings.put(branchName + ":1", branchSettingsMap)).thenReturn(branchSettingsMap);
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    service.setBranch(repository, branchName, immutableBranchSettings);
    final List<BranchSettings> branchSettingsList = service.getBranchSettings(repository);

    InOrder inOrder = inOrder(pluginSettings);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).put("branchList:1", expectedBranchList());
    inOrder.verify(pluginSettings).put(branchName + ":1", branchSettingsMap);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).get(branchName+":1");
    inOrder.verifyNoMoreInteractions();

    assertEquals("Branch settings list does not have expected size", 1, branchSettingsList.size());
    assertTrue("Branch settings list does not contain saved setting", branchSettingsList.contains(immutableBranchSettings));
  }

  @Test
  public void gettingSettingsForBranchReturnsBranchesMatchingNameTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(expandedBranchList());
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);
    when(pluginSettings.get(anotherBranchName + ":1")).thenReturn(anotherBranchMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    final List<BranchSettings> settingsList = service.getBranchSettingsForBranch(repository, branchName);

    assertEquals(Lists.newArrayList(immutableBranchSettings), settingsList);
  }

  @Test
  public void gettingSettingsForBranchReturnsBranchesMatchingRegexTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(expandedBranchList());
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);
    when(pluginSettings.get(anotherBranchName + ":1")).thenReturn(anotherBranchMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    final List<BranchSettings> settingsList = service.getBranchSettingsForBranch(repository, "(default|another) branch");

    assertEquals(Lists.newArrayList(immutableBranchSettings, anotherBranchSettings), settingsList);
  }

  @Test
  public void gettingSettingsForBranchReturnsBranchesWithPartialNameMatchesTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(expandedBranchList());
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);
    when(pluginSettings.get(anotherBranchName + ":1")).thenReturn(anotherBranchMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    final List<BranchSettings> settingsList = service.getBranchSettingsForBranch(repository, "another");

    assertEquals(Lists.newArrayList(anotherBranchSettings), settingsList);
  }

  @Test
  public void gettingSettingsForBranchReturnsAllBranchesMatchingNameTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(expandedBranchList());
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);
    when(pluginSettings.get(anotherBranchName + ":1")).thenReturn(anotherBranchMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    final List<BranchSettings> settingsList = service.getBranchSettingsForBranch(repository, "branch");

    assertEquals(Lists.newArrayList(immutableBranchSettings, anotherBranchSettings), settingsList);
  }

  @Test
  public void branchSettingsCanBeDeletedTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(new ArrayList<String>()).thenReturn(expectedBranchList())
      .thenReturn(new ArrayList<String>());
    when(pluginSettings.put("branchList:1", expectedBranchList())).thenReturn(expectedBranchList());
    when(pluginSettings.put(branchName + ":1", branchSettingsMap)).thenReturn(branchSettingsMap);
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    service.setBranch(repository, branchName, immutableBranchSettings);
    service.deleteBranch(repository, branchName);

    InOrder inOrder = inOrder(pluginSettings);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).put("branchList:1", expectedBranchList());
    inOrder.verify(pluginSettings).put(branchName + ":1", branchSettingsMap);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).remove("branchList:1");
    inOrder.verify(pluginSettings).remove(branchName + ":1");
    inOrder.verifyNoMoreInteractions();

    assertEquals("Branch settings list after deletion is not empty", 0, service.getBranchSettings(repository).size());
    assertTrue("Branch settings retrievable after deletion", service.getBranchSettingsForBranch(repository, branchName).isEmpty());
  }

  @Test
  public void deletingABranchDoesNotDeleteAllTest() {
    final PermissionValidationService permService = mock(PermissionValidationService.class);
    final PluginSettingsFactory factory = mock(PluginSettingsFactory.class);
    final PluginSettings pluginSettings = mock(PluginSettings.class);
    final Repository repository = mock(Repository.class);

    when(repository.getId()).thenReturn(1);
    when(factory.createSettingsForKey(PluginMetadata.getPluginKey())).thenReturn(pluginSettings);
    when(pluginSettings.get("branchList:1")).thenReturn(new ArrayList<String>()).thenReturn(expectedBranchList())
      .thenReturn(expandedBranchList())
      .thenReturn(reducedBranchList());
    when(pluginSettings.put("branchList:1", expectedBranchList())).thenReturn(expectedBranchList());
    when(pluginSettings.put("branchList:1", expandedBranchList())).thenReturn(expandedBranchList());
    when(pluginSettings.put("branchList:1", reducedBranchList())).thenReturn(reducedBranchList());
    when(pluginSettings.put(branchName + ":1", branchSettingsMap)).thenReturn(branchSettingsMap);
    when(pluginSettings.put(branchName + ":1", anotherBranchSettings)).thenReturn(anotherBranchSettings);
    when(pluginSettings.get(branchName + ":1")).thenReturn(branchSettingsMap);
    when(pluginSettings.get(anotherBranchName + ":1")).thenReturn(anotherBranchMap);

    final DefaultPullRequestTriggerSettingsService service = new DefaultPullRequestTriggerSettingsService(permService, factory);

    service.setBranch(repository, branchName, immutableBranchSettings);
    service.setBranch(repository, anotherBranchName, anotherBranchSettings);
    service.deleteBranch(repository, branchName);

    InOrder inOrder = inOrder(pluginSettings);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).put("branchList:1", expectedBranchList());
    inOrder.verify(pluginSettings).put(branchName + ":1", branchSettingsMap);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).put("branchList:1", expandedBranchList());
    inOrder.verify(pluginSettings).put(anotherBranchName + ":1", anotherBranchMap);
    inOrder.verify(pluginSettings).get("branchList:1");
    inOrder.verify(pluginSettings).remove("branchList:1");
    inOrder.verify(pluginSettings).put("branchList:1", reducedBranchList());
    inOrder.verify(pluginSettings).remove(branchName + ":1");
    inOrder.verifyNoMoreInteractions();

    assertEquals("Expected 1 branch left after deleting 1 of 2 existing", 1, service.getBranchSettings(repository).size());
    assertTrue("Branch settings retrievable after deletion", service.getBranchSettingsForBranch(repository, branchName).isEmpty());
    assertEquals("Expected settings for non-deleted branch", Lists.newArrayList(anotherBranchSettings),
      service.getBranchSettingsForBranch(repository, anotherBranchName));
  }

  private static List<String> expectedBranchList() {
    final List<String> expectedBranchList = new ArrayList<String>(1);

    expectedBranchList.add(branchName);
    return expectedBranchList;
  }

  private static List<String> expandedBranchList() {
    final List<String> expandedBranchList = expectedBranchList();
    expandedBranchList.add(anotherBranchName);
    return expandedBranchList;
  }

  private static List<String> reducedBranchList() {
    final List<String> reducedBranchList = new ArrayList<String>();

    reducedBranchList.add(anotherBranchName);
    return reducedBranchList;
  }
}
