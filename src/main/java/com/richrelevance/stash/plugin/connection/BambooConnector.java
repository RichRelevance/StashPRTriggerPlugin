package com.richrelevance.stash.plugin.connection;

/**
 * This class establishes a connection to Bamboo on a given URL, to
 * cause it to start a build.
 */
public interface BambooConnector {
  String get(String url, String user, String password);
}
