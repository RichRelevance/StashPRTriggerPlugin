package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

/**
 */
public class ImmutableBranchSettings implements BranchSettings {
  private final @Nonnull String name;
  private final @Nonnull String plan;

  public ImmutableBranchSettings(@Nonnull String name, @Nonnull String plan) {
    this.name = name;
    this.plan = plan;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPlan() {
    return plan;
  }
}
