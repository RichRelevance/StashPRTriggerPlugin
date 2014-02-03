package com.richrelevance.stash.plugin.connection;

/**
 */
public interface BambooConnector {
  String get(String url, String user, String password);
}
