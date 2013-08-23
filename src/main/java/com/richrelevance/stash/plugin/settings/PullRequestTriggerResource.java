package com.richrelevance.stash.plugin.settings;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;
import com.richrelevance.stash.plugin.settings.RestPullRequestTriggerSettings;
import com.sun.jersey.spi.resource.Singleton;

@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON})
@InterceptorChain(ResourceContextInterceptor.class)
@Path(ResourcePatterns.REPOSITORY_URI + "/pullrequest-trigger-settings")
@Produces({RestUtils.APPLICATION_JSON_UTF8})
@Singleton
public class PullRequestTriggerResource extends RestResource {

  private final PullRequestTriggerSettingsService pullRequestTriggerSettingsService;

  public PullRequestTriggerResource(I18nService i18nService, PullRequestTriggerSettingsService pullRequestTriggerSettingsService) {
    super(i18nService);
    this.pullRequestTriggerSettingsService = pullRequestTriggerSettingsService;
  }

  @GET
  public Response getPullRequestTriggerSettings(@Context Repository repository) {
    PullRequestTriggerSettings pullRequestTriggerSettings = pullRequestTriggerSettingsService.getPullRequestTriggerSettings(repository);
    return ResponseFactory.ok(new RestPullRequestTriggerSettings(pullRequestTriggerSettings))
      .build();
  }

  @PUT
  public Response setPullRequestTriggerSettings(@Context Repository repository, RestPullRequestTriggerSettings settings) {
    PullRequestTriggerSettings pullRequestTriggerSettings = pullRequestTriggerSettingsService.setPullRequestTriggerSettings(repository, settings);
    return ResponseFactory.ok(new RestPullRequestTriggerSettings(pullRequestTriggerSettings))
      .build();
  }
}
