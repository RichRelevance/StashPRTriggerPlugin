package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

public class ImmutablePullRequestTriggerSettings implements PullRequestTriggerSettings{

  private final boolean enabled;
  private final @Nonnull String url;
  private final @Nonnull String user;
  private final @Nonnull String password;
  private final @Nonnull String plan;

  public ImmutablePullRequestTriggerSettings() {
    this.enabled = false;
    this.url = "http://localhost/bamboo";
    this.user = "user";
    this.password = "password";
    this.plan = "plan";
  }

  public ImmutablePullRequestTriggerSettings(boolean enabled, @Nonnull String url, @Nonnull String user,
                                             @Nonnull String password, @Nonnull String plan) {
    this.enabled = enabled;
    this.url = url;
    this.user = user;
    this.password = password;
    this.plan = plan;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getPlan() {
    return plan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImmutablePullRequestTriggerSettings that = (ImmutablePullRequestTriggerSettings) o;

    if (enabled != that.enabled) return false;
    if (!password.equals(that.password)) return false;
    if (!plan.equals(that.plan)) return false;
    if (!url.equals(that.url)) return false;
    if (!user.equals(that.user)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (enabled ? 1 : 0);
    result = 31 * result + url.hashCode();
    result = 31 * result + user.hashCode();
    result = 31 * result + password.hashCode();
    result = 31 * result + plan.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ImmutablePullRequestTriggerSettings{" +
      "enabled=" + enabled +
      ", url='" + url + '\'' +
      ", user='" + user + '\'' +
      ", password='" + password + '\'' +
      ", plan='" + plan + '\'' +
      '}';
  }
}
