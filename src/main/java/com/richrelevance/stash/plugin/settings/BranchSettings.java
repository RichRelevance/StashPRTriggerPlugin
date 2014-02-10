package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

/**
 */
public interface BranchSettings {
  @Nonnull
  String getName();

  @Nonnull
  String getPlan();

  @Nonnull
  String getRetestMsg();
}
