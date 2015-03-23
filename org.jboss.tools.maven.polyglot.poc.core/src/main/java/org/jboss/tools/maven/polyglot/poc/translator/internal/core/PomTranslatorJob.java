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
package org.jboss.tools.maven.polyglot.poc.translator.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ICallable;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenExecutionContext;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomTranslatorJob extends Job {

    private static Logger LOG = LoggerFactory.getLogger(PomTranslatorJob.class);

	private List<IFile> poms;

	public PomTranslatorJob(List<IFile> poms) {
		super("Pom translator");
		Assert.isNotNull(poms);
		this.poms = new ArrayList<>(poms);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		LOG.debug("Translating "+poms);
		for (IFile source : poms) {
			IFile destination = source.getProject().getFile(IMavenConstants.POM_FILE_NAME);
			try {
				translateToXml(source, destination, monitor);
			} catch (CoreException O_o) {
				IStatus error = new Status(IStatus.ERROR, PolyglotSupportActivator.PLUGIN_ID, "Unable to translate "+source + " to pom.xml", O_o);
				return error;
			}
		}
		return Status.OK_STATUS;
	}

	protected void translateToXml(IFile source, IFile destination, IProgressMonitor monitor) throws CoreException {
		final IMaven maven = MavenPlugin.getMaven();
		IMavenExecutionContext context = maven.createExecutionContext();
	    final MavenExecutionRequest request = context.getExecutionRequest();
	    request.setPom(destination.getRawLocation().toFile());//Wow that's retarded in our case!!! pom.xml needs to already exist
	    request.setBaseDirectory(source.getRawLocation().toFile().getParentFile());
	    request.setGoals(Arrays.asList("io.takari.polyglot:polyglot-translate-plugin:translate"));
	    request.setUpdateSnapshots(false);
	    Properties props = new Properties();
	    props.put("input", source.getRawLocation().toOSString());
	    //TODO set output under target and if no translation errors, copy content to actual pom.xml
	    props.put("output", destination.getRawLocation().toOSString());
		request.setUserProperties(props);
	    
	    MavenExecutionResult result = context.execute(new ICallable<MavenExecutionResult>() {
	      public MavenExecutionResult call(IMavenExecutionContext context, IProgressMonitor innerMonitor) throws CoreException {
	    	 return ((MavenImpl)maven).lookupComponent(Maven.class)
	    			 .execute(request);
	      }
	    }, monitor);
	    
	    if (result.hasExceptions()) {
	    	for (Throwable O_o : result.getExceptions()) {
	    		//XXX yeah I know
	    		O_o.printStackTrace();
	    	}
	    }
	    
	    if (destination.exists()) {
	    	destination.refreshLocal(IResource.DEPTH_ZERO, monitor);
	    	destination.setDerived(true, monitor);
	    }
	}

}
