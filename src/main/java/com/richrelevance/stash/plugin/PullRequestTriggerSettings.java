package com.richrelevance.stash.plugin;

/**
 */
public interface PullRequestTriggerSettings {
  boolean isEnabled();
  String getUrl();
  String getUser();
  String getPassword();
  String getPlan();
}
