package com.richrelevance.stash.plugin;

import com.atlassian.stash.rest.data.RestMapEntity;

public class RestPullRequestTriggerSettings extends RestMapEntity implements PullRequestTriggerSettings {
  private static final String KEY_ENABLED = "enabled";
  private static final String KEY_URL = "url";
  private static final String KEY_USER = "user";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_PLAN = "plan";

  public RestPullRequestTriggerSettings() {
  }

  public RestPullRequestTriggerSettings(PullRequestTriggerSettings pullRequestTriggerSettings) {
    put(KEY_ENABLED, pullRequestTriggerSettings.isEnabled());
    put(KEY_URL, pullRequestTriggerSettings.getUrl());
    put(KEY_USER, pullRequestTriggerSettings.getUser());
    put(KEY_PASSWORD, pullRequestTriggerSettings.getPassword());
    put(KEY_PLAN, pullRequestTriggerSettings.getPlan());
  }

  @Override
  public boolean isEnabled() {
    return getBoolProperty(KEY_ENABLED);
  }

  @Override
  public String getUrl() {
    return getStringProperty(KEY_URL);
  }

  @Override
  public String getUser() {
    return getStringProperty(KEY_USER);
  }

  @Override
  public String getPassword() {
    return getStringProperty(KEY_PASSWORD);
  }

  @Override
  public String getPlan() {
    return getStringProperty(KEY_PLAN);
  }
}
