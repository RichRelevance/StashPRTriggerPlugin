package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

public final class ImmutablePullRequestTriggerSettings implements PullRequestTriggerSettings{

  private final boolean enabled;
  private final @Nonnull String url;
  private final @Nonnull String user;
  private final @Nonnull String password;

  public ImmutablePullRequestTriggerSettings() {
    this.enabled = false;
    this.url = "http://localhost/bamboo";
    this.user = "user";
    this.password = "password";
  }

  public ImmutablePullRequestTriggerSettings(boolean enabled, @Nonnull String url, @Nonnull String user,
                                             @Nonnull String password) {
    this.enabled = enabled;
    this.url = url;
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Nonnull
  @Override
  public String getUrl() {
    return url;
  }

  @Nonnull
  @Override
  public String getUser() {
    return user;
  }

  @Nonnull
  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImmutablePullRequestTriggerSettings that = (ImmutablePullRequestTriggerSettings) o;

    if (enabled != that.enabled) return false;
    if (!password.equals(that.password)) return false;
    if (!url.equals(that.url)) return false;
    return user.equals(that.user);

  }

  @Override
  public int hashCode() {
    int result = (enabled ? 1 : 0);
    result = 31 * result + url.hashCode();
    result = 31 * result + user.hashCode();
    result = 31 * result + password.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ImmutablePullRequestTriggerSettings{" +
      "enabled=" + enabled +
      ", url='" + url + '\'' +
      ", user='" + user + '\'' +
      ", password='" + password + '\'' +
      '}';
  }
}
