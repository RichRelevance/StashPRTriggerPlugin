package com.richrelevance.stash.plugin;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.rest.interceptor.ResourceContextInterceptor;
import com.atlassian.stash.rest.util.ResourcePatterns;
import com.atlassian.stash.rest.util.ResponseFactory;
import com.atlassian.stash.rest.util.RestResource;
import com.atlassian.stash.rest.util.RestUtils;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
