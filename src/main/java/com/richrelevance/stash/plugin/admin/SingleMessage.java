package com.richrelevance.stash.plugin.admin;

final class SingleMessage implements StatusMessages {
  private final String successMessage;
  private final String infoMessage;
  private final String warningMessage;
  private final String errorMessage;

  private SingleMessage(String successMessage, String infoMessage, String warningMessage, String errorMessage) {
    this.successMessage = successMessage != null ? successMessage : "";
    this.infoMessage = infoMessage != null ? infoMessage : "";
    this.warningMessage = warningMessage != null ? warningMessage : "";
    this.errorMessage = errorMessage != null ? errorMessage : "";
  }

  @Override
  public String getSuccessMessage() {
    return successMessage;
  }

  @Override
  public String getInfoMessage() {
    return infoMessage;
  }

  @Override
  public String getWarningMessage() {
    return warningMessage;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  public static StatusMessages success(String successMessage) {
    return new SingleMessage(successMessage, "", "", "");
  }

  public static StatusMessages info(String infoMessage) {
    return new SingleMessage(infoMessage, "", "", "");
  }

  public static StatusMessages warning(String warningMessage) {
    return new SingleMessage(warningMessage, "", "", "");
  }

  public static StatusMessages error(String errorMessage) {
    return new SingleMessage("", "", "", errorMessage);
  }
}
