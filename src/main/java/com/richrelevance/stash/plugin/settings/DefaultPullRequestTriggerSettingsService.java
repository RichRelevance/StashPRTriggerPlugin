package com.richrelevance.stash.plugin.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  // add log4j.logger.attlassian.plugin=DEBUG  to stash-config.properties on Stash home directory to use this logger
  // private static final Logger log = LoggerFactory.getLogger("atlassian.plugin");

  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestHook.class);

  static final ImmutablePullRequestTriggerSettings DEFAULT_SETTINGS = new ImmutablePullRequestTriggerSettings();

  private static final String KEY_ENABLED = "enabled";
  private static final String KEY_URL = "url";
  private static final String KEY_USER = "user";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_PLAN = "plan";

  private final PermissionValidationService permissionValidationService;

  private final Map<Integer, PullRequestTriggerSettings> cache = new MapMaker()
    .makeComputingMap(new Function<Integer, PullRequestTriggerSettings>() {
      public PullRequestTriggerSettings apply(Integer repoId) {
        Map<String, String> data = (Map<String, String>) pluginSettings.get(repoId.toString());
        if (data != null) {
          return deserialize(data);
        }
        return DEFAULT_SETTINGS;
      }
    });

  private final PluginSettings pluginSettings;

  public DefaultPullRequestTriggerSettingsService(PermissionValidationService permissionValidationService, PluginSettingsFactory pluginSettingsFactory) {
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
    try {
      data = serialize(settings);
    } catch (NullPointerException e) {
      log.error("Error serializing PR settings object " + settings, e);
      throw e;
    }
    pluginSettings.put(Integer.toString(repository.getId()), data);
    cache.remove(repository.getId());
    return deserialize(data);
  }

  @Override
  public List<BranchSettings> getBranchSettings(Repository repository) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_READ);
    return new ArrayList<BranchSettings>();
  }

  @Override
  public void setBranch(Repository repository, String name, BranchSettings settings) {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private Map<String, String> serialize(PullRequestTriggerSettings settings) {
    Map<String, String> data = new HashMap<String, String>();
    data.put(KEY_ENABLED, Boolean.toString(settings.isEnabled()));
    data.put(KEY_URL, settings.getUrl());
    data.put(KEY_USER, settings.getUser());
    data.put(KEY_PASSWORD, settings.getPassword());
    data.put(KEY_PLAN, settings.getPlan());
    return data;
  }

  private PullRequestTriggerSettings deserialize(Map<String, String> settings) {
    return new ImmutablePullRequestTriggerSettings(
      Boolean.parseBoolean(settings.get(KEY_ENABLED)),
      settings.get(KEY_URL),
      settings.get(KEY_USER),
      settings.get(KEY_PASSWORD),
      settings.get(KEY_PLAN)
    );
  }
}
