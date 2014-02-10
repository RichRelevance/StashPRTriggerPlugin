package com.richrelevance.stash.plugin.trigger;

import com.richrelevance.stash.plugin.settings.BranchSettings;

/**
 * Created by dsobral on 2/7/14.
 */
public interface BranchPredicate {
  boolean matches(BranchSettings branchSettings);
}
