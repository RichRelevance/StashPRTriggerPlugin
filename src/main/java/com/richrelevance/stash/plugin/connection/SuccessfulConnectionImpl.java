package com.richrelevance.stash.plugin.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class SuccessfulConnectionImpl implements SuccessfulConnection {
  // Needs a log4j.properties
  private static final Logger log = LoggerFactory.getLogger(SuccessfulConnectionImpl.class);

  private final URLConnection connection;

  public SuccessfulConnectionImpl(URLConnection connection) {
    this.connection = connection;
  }

  @Override
  public String getResponse() {
    String response = null;
    try {
      response = readResponse();
    } catch (IOException e) {
      log.error("unable to get POST response", e);
    }
    return response;
  }

  private String readResponse() throws IOException {
    String encoding = connection.getContentEncoding();
    encoding = encoding == null ? "UTF-8" : encoding;

    final InputStream buildRequisitionResponse = connection.getInputStream();
    final String response = IOUtils.toString(buildRequisitionResponse, encoding);
    log.info("response from " + connection.getURL() + ": " + response);
    buildRequisitionResponse.close();

    return response;
  }
}
