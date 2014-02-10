package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

/**
 */
public final class ImmutableBranchSettings implements BranchSettings {
  private final @Nonnull String name;
  private final @Nonnull String plan;
  private final @Nonnull String retestMsg;

  public ImmutableBranchSettings(@Nonnull String name, @Nonnull String plan, @Nonnull String retestMsg) {
    this.name = name;
    this.plan = plan;
    this.retestMsg = retestMsg;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPlan() {
    return plan;
  }

  @Override
  public String getRetestMsg() {
    return retestMsg;
  }

  @Override
  public String toString() {
    return "ImmutableBranchSettings{" +
      "name='" + name + '\'' +
      ", plan='" + plan + '\'' +
      ", retestMsg='" + retestMsg + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImmutableBranchSettings that = (ImmutableBranchSettings) o;

    if (!name.equals(that.name)) return false;
    if (!plan.equals(that.plan)) return false;
    if (!retestMsg.equals(that.retestMsg)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + plan.hashCode();
    result = 31 * result + retestMsg.hashCode();
    return result;
  }

}
