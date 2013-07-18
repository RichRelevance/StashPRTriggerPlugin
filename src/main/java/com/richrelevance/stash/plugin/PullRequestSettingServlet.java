package com.richrelevance.stash.plugin;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class PullRequestSettingServlet extends HttpServlet {

  private final PermissionValidationService permissionValidationService;
  private final PullRequestTriggerSettingsService pullRequestTriggerSettingsService;
  private final RepositoryService repositoryService;
  private final WebResourceManager webResourceManager;
  private final SoyTemplateRenderer soyTemplateRenderer;

  public PullRequestSettingServlet(PermissionValidationService permissionValidationService,
                                   PullRequestTriggerSettingsService pullRequestTriggerSettingsService,
                                   RepositoryService repositoryService,
                                   WebResourceManager webResourceManager,
                                   SoyTemplateRenderer soyTemplateRenderer) {
    this.permissionValidationService = permissionValidationService;
    this.pullRequestTriggerSettingsService = pullRequestTriggerSettingsService;
    this.repositoryService = repositoryService;
    this.webResourceManager = webResourceManager;
    this.soyTemplateRenderer = soyTemplateRenderer;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String[] pathParts = pathInfo.substring(1).split("/");
    if (pathParts.length != 2) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String projectKey = pathParts[0];
    String repoSlug = pathParts[1];
    Repository repository = repositoryService.getBySlug(projectKey, repoSlug);
    if (repository == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    doView(repository, req, resp);

  }

  private void doView(Repository repository, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    PullRequestTriggerSettings pullRequestTriggerSettings = pullRequestTriggerSettingsService.getPullRequestTriggerSettings(repository);
    render(resp,
      "stash.page.pullrequest.trigger.settings.viewPullRequestTriggerSettings",
      ImmutableMap.<String, Object>builder()
        .put("repository", repository)
        .put("prTriggerSettings", pullRequestTriggerSettings)
        .build()
    );
  }

  private void render(HttpServletResponse resp, String templateName, Map<String, Object> data) throws IOException, ServletException {
    webResourceManager.requireResourcesForContext("plugin.page.prevent.deletion");
    resp.setContentType("text/html;charset=UTF-8");
    try {
      soyTemplateRenderer.render(resp.getWriter(), PluginMetadata.getCompleteModuleKey("soy-templates"), templateName, data);
    } catch (SoyException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new ServletException(e);
    }
  }
}
