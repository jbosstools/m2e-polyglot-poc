/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.maven.polyglot.poc.internal.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
import org.eclipse.m2e.core.internal.markers.IMavenMarkerManager;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class PomTranslatorJob extends Job {

  private static final Logger LOG = LoggerFactory.getLogger(PomTranslatorJob.class);

  private static final String TRANSLATION_PROBLEM_TYPE = "mavenPolyglotProblem.translationError";

  private static final Pattern LINE_COL_INFO_PATTERN = Pattern.compile("line (\\d+), column");

  private List<IFile> poms;

  private IMavenProjectRegistry projectManager;

  private IMavenMarkerManager markerManager;

  final private String translatePluginVersion;

  
  public PomTranslatorJob(List<IFile> poms, String translatePluginVersion) {
	  this(MavenPlugin.getMavenProjectRegistry(), MavenPluginActivator.getDefault().getMavenMarkerManager(), poms, translatePluginVersion);
  }
  
  public PomTranslatorJob(IMavenProjectRegistry projectManager, IMavenMarkerManager markerManager, List<IFile> poms, String translatePluginVersion) {
    super("Pom translator");
    Assert.isNotNull(poms);
    this.projectManager = projectManager;
    this.markerManager = markerManager;
    this.poms = new ArrayList<>(poms);
    this.translatePluginVersion = translatePluginVersion;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    LOG.debug("Translating "+poms);
    for (IFile input : poms) {
      if (monitor.isCanceled()) {
        break;
      }
      try {
        translatePom(input, monitor);
      } catch (CoreException O_o) {
        IStatus error = new Status(IStatus.ERROR, PolyglotSupportActivator.PLUGIN_ID, "Unable to translate "+input + " to pom.xml", O_o);
        return error;
      }
    }
    return Status.OK_STATUS;
  }

  private void translatePom(IFile input, IProgressMonitor monitor) throws CoreException {
    markerManager.deleteMarkers(input, TRANSLATION_PROBLEM_TYPE);

    IProject project = input.getProject();
    IFile pomXml = project.getFile(IMavenConstants.POM_FILE_NAME);
    IPath buildFolder;
    if (pomXml.exists()) {
	    	IMavenProjectFacade facade = projectManager.create(pomXml, true, monitor);
	    	MavenProject mavenProject = facade.getMavenProject(monitor);
	    	buildFolder = facade.getProjectRelativePath(mavenProject.getBuild().getDirectory());
    } else {
    		//In case where pom.xml doesn't exist, fall back to default target folder
    		buildFolder = project.getFolder("target").getProjectRelativePath();
    }

    IPath polyglotFolder = buildFolder.append("polyglot");
    IFile output = project.getFolder(polyglotFolder).getFile(IMavenConstants.POM_FILE_NAME);
    MavenExecutionResult result = translate(pomXml, input, output, monitor);
    if (result.hasExceptions()) {
      addErrorMarkers(input, result.getExceptions());
      return;
    } 
    
    if (output.exists()) {
    	  try (InputStream content = output.getContents()) {
    		  if (pomXml.exists()) {
    			  pomXml.setContents(content, true, true, monitor);
    		  } else {
    			  pomXml.create(content, true, monitor);
    		  }
    		  if (!pomXml.isDerived()) {
    			  pomXml.setDerived(true, monitor);
    		  }
    	  } catch (IOException e) {
    		  throw new CoreException(new Status(IStatus.ERROR, PolyglotSupportActivator.PLUGIN_ID, "Unable to write to pom.xml", e));
	  }
    }
  }

  private void addErrorMarkers(IFile input, Collection<? extends Throwable> errors) {
    for (Throwable O_o : errors) {
      String msg = ""+O_o.getMessage();
      Matcher m = LINE_COL_INFO_PATTERN.matcher(msg);
      if (m.find()) {
        int line = Integer.parseInt(m.group(1));
        markerManager.addMarker(input, TRANSLATION_PROBLEM_TYPE, msg, line, IMarker.SEVERITY_ERROR);
      } else {
        markerManager.addErrorMarkers(input, TRANSLATION_PROBLEM_TYPE, O_o);
      }

    }
  }

  protected MavenExecutionResult translate(IFile pom, IFile input, IFile output, IProgressMonitor monitor) throws CoreException {
    final IMaven maven = MavenPlugin.getMaven();
    IMavenExecutionContext context = maven.createExecutionContext();
    final MavenExecutionRequest request = context.getExecutionRequest();
    File pomFile = pom.getRawLocation().toFile();
    request.setBaseDirectory(pomFile.getParentFile());
	final String pluginTargetLine = "io.takari.polyglot:polyglot-translate-plugin" +
			(translatePluginVersion.isEmpty() ? "" : ":" + translatePluginVersion) +
			":translate";
	request.setGoals(Arrays.asList(pluginTargetLine));
    request.setUpdateSnapshots(false);
    Properties props = new Properties();
    props.put("input", input.getRawLocation().toOSString());
    String rawDestination = output.getRawLocation().toOSString();
    props.put("output", rawDestination);
    request.setUserProperties(props);

    new File(rawDestination).getParentFile().mkdirs();

    MavenExecutionResult result = context.execute(new ICallable<MavenExecutionResult>() {
      public MavenExecutionResult call(IMavenExecutionContext context, IProgressMonitor innerMonitor) throws CoreException {
       return ((MavenImpl)maven).lookupComponent(Maven.class)
             .execute(request);
      }
    }, monitor);

    output.refreshLocal(IResource.DEPTH_ZERO, monitor);
    return result;
  }

}
