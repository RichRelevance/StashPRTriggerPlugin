package com.richrelevance.stash.plugin.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.richrelevance.stash.plugin.PluginMetadata;
import com.richrelevance.stash.plugin.PullRequestHook;

public class DefaultPullRequestTriggerSettingsService implements PullRequestTriggerSettingsService {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);
  static final ImmutablePullRequestTriggerSettings DEFAULT_SETTINGS = new ImmutablePullRequestTriggerSettings();

  private static final String KEY_BRANCH_NAME = "name";
  private static final String KEY_ENABLED = "enabled";
  private static final String KEY_URL = "url";
  private static final String KEY_USER = "user";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_AUTOMATIC_BUILD_ENABLED = "automaticBuildEnabled";
  private static final String KEY_PLAN = "plan";
  private static final String KEY_RETEST_MSG = "retestMsg";
  private static final String KEY_BRANCH_LIST = "branchList:";

  private final PermissionValidationService permissionValidationService;

  @SuppressWarnings("deprecation")
  private final Map<Integer, PullRequestTriggerSettings> cache = new MapMaker()
    .makeComputingMap(new Function<Integer, PullRequestTriggerSettings>() {
      public PullRequestTriggerSettings apply(Integer repoId) {
        //noinspection unchecked
        Map<String, String> data = (Map<String, String>) pluginSettings.get(repoId.toString());
        if (data != null) {
          return deserialize(data);
        }
        return DEFAULT_SETTINGS;
      }
    });

  @SuppressWarnings("deprecation")
  private final Map<Integer, List<String>> branchListCache = new MapMaker()
    .makeComputingMap(new Function<Integer, List<String>>() {
      public List<String> apply(Integer repoId) {
        //noinspection unchecked
        List<String> data = (List<String>) pluginSettings.get(KEY_BRANCH_LIST +repoId.toString());
        if (data != null) {
          return data;
        }
        return new ArrayList<String>();
      }
    });

  @SuppressWarnings("deprecation")
  private final Map<String, BranchSettings> branchCache = new MapMaker()
    .makeComputingMap(new Function<String, BranchSettings>() {
      public BranchSettings apply(String branchAndRepo) {
        //noinspection unchecked
        Map<String, String> data = (Map<String, String>) pluginSettings.get(branchAndRepo);
        if (data != null) {
          return deserializeBranch(data);
        }
        return null;
      }
    });

  private final PluginSettings pluginSettings;

  public DefaultPullRequestTriggerSettingsService(PermissionValidationService permissionValidationService,
                                                  PluginSettingsFactory pluginSettingsFactory) {
    this.permissionValidationService = permissionValidationService;
    pluginSettings = pluginSettingsFactory.createSettingsForKey(PluginMetadata.getPluginKey());
  }

  @Override
  public PullRequestTriggerSettings getPullRequestTriggerSettings(Repository repository) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_READ);
    return cache.get(repository.getId());
  }

  @Override
  public PullRequestTriggerSettings setPullRequestTriggerSettings(Repository repository, PullRequestTriggerSettings settings) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    final Map<String, String> data;
    final Integer repositoryId = repository.getId();
    if (repositoryId == null) {
      log.error("Repository id is null when saving settings: " + repository);
      return null;
    }
    try {
      data = serialize(settings);
    } catch (NullPointerException e) {
      log.error("Error serializing PR settings object " + settings, e);
      throw e;
    }
    pluginSettings.put(Integer.toString(repositoryId), data);
    cache.remove(repositoryId);
    return deserialize(data);
  }

  @Override
  public List<BranchSettings> getBranchSettings(Repository repository) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_READ);
    final List<String> branches = branchListCache.get(repository.getId());
    final List<BranchSettings> branchSettings = new ArrayList<BranchSettings>();

    for (String branch : branches) {
      BranchSettings settings = branchCache.get(branchKeyForRepoId(repository, branch));
      if (settings != null) {
        branchSettings.add(settings);
      }
    }

    return branchSettings;
  }

  @Override
  public void setBranch(Repository repository, String branchName, BranchSettings settings) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    final String branchKey = branchKeyForRepoId(repository, branchName);
    final Integer repositoryId = repository.getId();
    final List<String> branches = branchListCache.get(repositoryId);

    if (repositoryId == null) {
      log.error("Repository id is null when saving branch settings: " + repository);
      return;
    }

    if (!branches.contains(branchName)) {
      branches.add(branchName);
      pluginSettings.put(KEY_BRANCH_LIST + repositoryId.toString(), branches);
      branchListCache.remove(repositoryId);
    }

    final Map<String, String> data;

    try {
      data = serializeBranch(settings);
    } catch (NullPointerException e) {
      log.error("Error serializing PR branch settings object " + settings, e);
      throw e;
    }

    pluginSettings.put(branchKey, data);
    branchCache.remove(branchKey);
    branchListCache.remove(repositoryId);
  }

  @Override
  public void deleteBranch(Repository repository, String branchName) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    final String branchKey = branchKeyForRepoId(repository, branchName);
    final Integer repositoryId = repository.getId();
    final List<String> branches = branchListCache.get(repositoryId);

    if (repositoryId == null) {
      log.error("Repository id is null when deleting branch settings: " + repository);
      return;
    }

    if (branches.contains(branchName)) {
      branches.remove(branchName);
      pluginSettings.remove(KEY_BRANCH_LIST + repositoryId.toString());
      if (!branches.isEmpty())
        pluginSettings.put(KEY_BRANCH_LIST + repositoryId.toString(), branches);
      branchListCache.remove(repositoryId);
    }

    pluginSettings.remove(branchKey);
    branchCache.remove(branchKey);
  }

  @Override
  public List<BranchSettings> getBranchSettingsForBranch(Repository repository, String branchName) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_READ);
    final List<String> branchList = branchListCache.get(repository.getId());
    final List<BranchSettings> result = new ArrayList<BranchSettings>();

    for (String branchPattern : branchList) {
      final Pattern regexPattern;

      try {
        regexPattern = Pattern.compile(branchPattern);
      } catch (PatternSyntaxException e) {
        log.error(String.format("Invalid regex for branch configuration: %s", branchName), e);
        return new ArrayList<BranchSettings>();
      }
      if (regexPattern.matcher(branchName).find()) {
        result.add(branchCache.get(branchKeyForRepoId(repository, branchPattern)));
      }
    }
    return result;
  }

  private String branchKeyForRepoId(Repository repository, String branch) {
    final Integer repositoryId = repository.getId();
    return branch + ":" + (repositoryId != null ? repositoryId.toString() : "null");
  }

  static public Map<String, String> serialize(PullRequestTriggerSettings settings) {
    Map<String, String> data = new HashMap<String, String>();
    data.put(KEY_ENABLED, Boolean.toString(settings.isEnabled()));
    data.put(KEY_URL, settings.getUrl());
    data.put(KEY_USER, settings.getUser());
    data.put(KEY_PASSWORD, settings.getPassword());
    return data;
  }

  static public PullRequestTriggerSettings deserialize(Map<String, String> settings) {
    return new ImmutablePullRequestTriggerSettings(
      Boolean.parseBoolean(settings.get(KEY_ENABLED)),
      settings.get(KEY_URL),
      settings.get(KEY_USER),
      settings.get(KEY_PASSWORD)
    );
  }

  static public Map<String, String> serializeBranch(BranchSettings settings) {
    Map<String, String> data = new HashMap<String, String>();
    data.put(KEY_AUTOMATIC_BUILD_ENABLED, Boolean.toString(settings.isAutomaticBuildEnabled()));
    data.put(KEY_BRANCH_NAME, settings.getName());
    data.put(KEY_PLAN, settings.getPlan());
    data.put(KEY_RETEST_MSG, settings.getRetestMsg());
    return data;
  }

  static public BranchSettings deserializeBranch(Map<String, String> settings) {
    return new ImmutableBranchSettings(
      Boolean.parseBoolean(settings.get(KEY_AUTOMATIC_BUILD_ENABLED)),
      settings.get(KEY_BRANCH_NAME),
      settings.get(KEY_PLAN),
      settings.get(KEY_RETEST_MSG)
    );
  }
}
