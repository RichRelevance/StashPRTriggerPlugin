package com.richrelevance.stash.plugin.settings;

import javax.annotation.Nonnull;

public final class ImmutablePullRequestTriggerSettings implements PullRequestTriggerSettings{

  private final boolean enabled;
  private final @Nonnull String url;
  private final @Nonnull String user;
  private final @Nonnull String password;
  private final @Nonnull String retestMsg;

  public ImmutablePullRequestTriggerSettings() {
    this.enabled = false;
    this.url = "http://localhost/bamboo";
    this.user = "user";
    this.password = "password";
    this.retestMsg = "(?i)retest this,? please|klaatu barada nikto";
  }

  public ImmutablePullRequestTriggerSettings(boolean enabled, @Nonnull String url, @Nonnull String user,
                                             @Nonnull String password, @Nonnull String retestMsg) {
    this.enabled = enabled;
    this.url = url;
    this.user = user;
    this.password = password;
    this.retestMsg = retestMsg;
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
  public String getRetestMsg() {
    return retestMsg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImmutablePullRequestTriggerSettings that = (ImmutablePullRequestTriggerSettings) o;

    if (enabled != that.enabled) return false;
    if (!password.equals(that.password)) return false;
    if (!retestMsg.equals(that.retestMsg)) return false;
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
    result = 31 * result + retestMsg.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ImmutablePullRequestTriggerSettings{" +
      "enabled=" + enabled +
      ", url='" + url + '\'' +
      ", user='" + user + '\'' +
      ", password='" + password + '\'' +
      ", retestMsg='" + retestMsg + '\'' +
      '}';
  }
}
