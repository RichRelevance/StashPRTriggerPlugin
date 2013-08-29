package com.richrelevance.stash.plugin.admin;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.richrelevance.stash.plugin.PluginMetadata;
import com.richrelevance.stash.plugin.settings.BranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutableBranchSettings;
import com.richrelevance.stash.plugin.settings.ImmutablePullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettings;
import com.richrelevance.stash.plugin.settings.PullRequestTriggerSettingsService;

public class PullRequestSettingServlet extends HttpServlet {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(PullRequestSettingServlet.class);

  private final PermissionValidationService permissionValidationService;
  private final PullRequestTriggerSettingsService pullRequestTriggerSettingsService;
  private final RepositoryService repositoryService;
  @SuppressWarnings("deprecation")
  private final WebResourceManager webResourceManager;
  private final SoyTemplateRenderer soyTemplateRenderer;

  public PullRequestSettingServlet(PermissionValidationService permissionValidationService,
                                   PullRequestTriggerSettingsService pullRequestTriggerSettingsService,
                                   RepositoryService repositoryService,
                                   @SuppressWarnings("deprecation") WebResourceManager webResourceManager,
                                   SoyTemplateRenderer soyTemplateRenderer) {
    this.permissionValidationService = permissionValidationService;
    this.pullRequestTriggerSettingsService = pullRequestTriggerSettingsService;
    this.repositoryService = repositoryService;
    this.webResourceManager = webResourceManager;
    this.soyTemplateRenderer = soyTemplateRenderer;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Repository repository = getRepository(req);
    if (repository != null) {
      doView(repository, resp);
    } else {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Repository repository = getRepository(req);
    if (repository == null) {
      resp.sendError(404);
      return;
    }
    if (!saveSettings(req, repository)) {
      resp.sendError(404);
    }
    doGet(req, resp);
  }

  private boolean saveSettings(HttpServletRequest req, Repository repository) {
    String button = req.getParameter("submit-button") != null ? req.getParameter("submit-button") : "";

    if (button.equals("Save")) {
      saveGeneralSettings(req, repository);
    } else if (button.equals("Add")) {
      addBranch(req, repository);
    } else if (button.equals("Update")) {
      saveBranch(req, repository);
    } else if (button.equals("Delete")) {
      deleteBranch(req, repository);
    } else if (!button.equals("Cancel")) {
      return false;
    }

    return true;
  }

  private void addBranch(HttpServletRequest req, Repository repository) {
    String name = req.getParameter("name"); name = name != null ? name.trim() : "";
    String plan = req.getParameter("plan"); plan = plan != null ? plan.trim() : "";

    if (name.isEmpty() || plan.isEmpty()) {
      log.info(String.format("Ignoring branch update with empty field (name '%s', plan '%s')", name, plan));
      return;
    }

    BranchSettings settings = new ImmutableBranchSettings(name, plan);

    pullRequestTriggerSettingsService.setBranch(repository, name, settings);
  }

  private void saveBranch(HttpServletRequest req, Repository repository) {
    addBranch(req, repository); // adding and saving have the same result
  }

  private void deleteBranch(HttpServletRequest req, Repository repository) {
    String name = req.getParameter("name");

    pullRequestTriggerSettingsService.deleteBranch(repository, name);
  }

  private void saveGeneralSettings(HttpServletRequest req, Repository repository) {
    Boolean enabled = (req.getParameter("enabled") == null) ? false : true;
    String url = req.getParameter("url"); url = url != null ? url.trim() : "";
    String user = req.getParameter("user"); user = user != null ? user.trim() : "";
    String password = req.getParameter("password"); password = password != null ? password.trim() : "";
    String retestMsg = req.getParameter("retest-msg"); retestMsg = retestMsg != null ? retestMsg.trim() : "";

    if (url.isEmpty() || user.isEmpty() || password.isEmpty() || retestMsg.isEmpty()) {
      log.info(String.format("Ignoring settings update with empty field (url '%s', user '%s', password '%s', retestMsg '%s'", url,
        user, password.isEmpty() ? "" : "*********", retestMsg));
      return;
    }

    try {
      Pattern.compile(retestMsg);
    } catch (PatternSyntaxException e) {
      log.info(String.format("Ignoring settings update with illegal retest message pattern (%s)", retestMsg), e);
      return;
    }

    PullRequestTriggerSettings settings = new ImmutablePullRequestTriggerSettings(enabled, url, user, password, retestMsg);

    pullRequestTriggerSettingsService.setPullRequestTriggerSettings(repository, settings);
  }

  private Repository getRepository(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (!isPathValid(pathInfo)) {
      return null;
    }
    String[] pathParts = pathInfo.substring(1).split("/");
    if (pathParts.length != 2) {
      return null;
    }

    String projectKey = pathParts[0];
    String repoSlug = pathParts[1];

    return repositoryService.getBySlug(projectKey, repoSlug);
  }

  private boolean isPathValid(String pathInfo) {
    return !(Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/"));
  }

  private void doView(Repository repository, HttpServletResponse resp) throws ServletException, IOException {
    permissionValidationService.validateForRepository(repository, Permission.REPO_ADMIN);
    PullRequestTriggerSettings pullRequestTriggerSettings = pullRequestTriggerSettingsService.getPullRequestTriggerSettings(repository);
    ImmutableCollection<BranchSettings> branchSettings = ImmutableList.copyOf(pullRequestTriggerSettingsService.getBranchSettings(repository));
    render(resp,
      "stash.page.pullrequest.trigger.settings.viewPullRequestTriggerSettings",
      ImmutableMap.<String, Object>builder()
        .put("repository", repository)
        .put("prTriggerSettings", pullRequestTriggerSettings)
        .put("branchSettings", branchSettings)
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
