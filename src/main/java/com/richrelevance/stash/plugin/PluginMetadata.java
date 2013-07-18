package com.richrelevance.stash.plugin;

public class PluginMetadata {

  public static String getPluginKey() {
    return "com.richrelevance.stash.plugin.prTrigger";
  }

  public static String getCompleteModuleKey(String moduleKey) {
    return getPluginKey() + ":" + moduleKey;
  }

}
