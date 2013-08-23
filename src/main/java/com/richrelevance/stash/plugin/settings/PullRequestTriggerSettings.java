package com.richrelevance.stash.plugin.settings;

/**
 */
public interface PullRequestTriggerSettings {
  boolean isEnabled();
  String getUrl();
  String getUser();
  String getPassword();
  String getPlan();
}
