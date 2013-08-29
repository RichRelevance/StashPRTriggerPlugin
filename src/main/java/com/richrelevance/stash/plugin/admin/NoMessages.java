package com.richrelevance.stash.plugin.admin;

/**
 */
public final class NoMessages implements StatusMessages {
  @Override
  public String getSuccessMessage() {
    return "";
  }

  @Override
  public String getInfoMessage() {
    return "";
  }

  @Override
  public String getWarningMessage() {
    return "";
  }

  @Override
  public String getErrorMessage() {
    return "";
  }
}
