package com.richrelevance.stash.plugin.connection;

/**
*/
public class FailedConnection implements URLConnectionBuilder, Connector, OpenConnection, SuccessfulConnection {
  private static FailedConnection failedConnection = new FailedConnection();

  private FailedConnection() {
  }

  public static FailedConnection getInstance() {
    return failedConnection;
  }

  @Override
  public String getResponse() {
    return null;
  }

  @Override
  public SuccessfulConnection checkResult() {
    return this;
  }

  @Override
  public OpenConnection connect() {
    return this;
  }

  @Override
  public Connector createConnection() {
    return this;
  }
}
