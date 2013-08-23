package com.richrelevance.stash.plugin.connection;

/**
 */
public class URLConnectionBuildError extends Exception {
  public URLConnectionBuildError(String errorMessage, Throwable e) {
    super(errorMessage, e);
  }

  public URLConnectionBuildError(String errorMessage) {
    super(errorMessage);
  }
}
