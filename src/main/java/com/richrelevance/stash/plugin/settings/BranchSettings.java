package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

/**
 */
public interface BranchSettings {
  boolean isAutomaticBuildEnabled();

  @Nonnull
  String getName();

  @Nonnull
  String getPlan();

  @Nonnull
  String getRetestMsg();
}
