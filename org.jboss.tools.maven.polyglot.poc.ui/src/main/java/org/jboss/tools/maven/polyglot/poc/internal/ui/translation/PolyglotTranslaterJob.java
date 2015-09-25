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
package org.jboss.tools.maven.polyglot.poc.internal.ui.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.maven.polyglot.poc.internal.core.ModulesCollector;
import org.jboss.tools.maven.polyglot.poc.internal.core.PomTranslatorJob;
import org.jboss.tools.maven.polyglot.poc.internal.ui.PolyglotSupportUIActivator;

/**
 * @author Fred Bricon
 */
public class PolyglotTranslaterJob extends PomTranslatorJob {

	private IMavenProjectFacade facade;
	private Language language;
	private boolean addExtension;
	private File mvnExtensionsDir;

	public PolyglotTranslaterJob(IMavenProjectFacade facade, Language language, boolean addExtension, File mvnExtensionsDir) {
		super(Collections.singletonList(facade.getPom()));
		this.facade = facade;
		this.language = language;
		this.addExtension = addExtension;
		this.mvnExtensionsDir = mvnExtensionsDir;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (addExtension) {
				Artifact extension = findLatestExtensionFor(language.getMavenPluginId(), monitor);
				monitor.worked(1);
				addExtension(mvnExtensionsDir, extension);
				monitor.worked(1);
			}
			
			IMavenProjectFacade[] allFacades = MavenPlugin.getMavenProjectRegistry().getProjects();
			Set<IMavenProjectFacade> modules = new ModulesCollector(facade, allFacades).getModules(monitor);
			List<IMavenProjectFacade> facades = new ArrayList<>(modules.size()+1);
			facades.add(facade);
			facades.addAll(modules);
			for (IMavenProjectFacade f : facades) {
				IFile output = f.getPom().getParent().getFile(new Path("pom."+language.getFileExtension()));
				translate(f.getPom(), f.getPom(), output, monitor);
				facade.getProject().refreshLocal(IResource.DEPTH_ZERO, monitor);
				monitor.worked(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof CoreException) {
				return ((CoreException)e).getStatus();
			}
			return new Status(IStatus.ERROR, PolyglotSupportUIActivator.PLUGIN_ID, e.getMessage());
		}
		return Status.OK_STATUS;
	}

	private void addExtension(File mvnExtensionsDir, Artifact extension) throws Exception {
		mvnExtensionsDir.mkdirs();
		File mvnExtension = new File(mvnExtensionsDir, "extensions.xml");
		Xpp3Dom dom = null;
		if (mvnExtension.exists()) {//TODO could make check more robust
			try (FileInputStream fis = new FileInputStream(mvnExtension)) {
				dom = Xpp3DomBuilder.build(fis, "UTF-8");
			}
		} else {
			dom = Xpp3DomBuilder.build(new StringReader("<extensions>\n</extensions>"));
		}
		Xpp3Dom[] extensions = dom.getChildren("extension");
		if (extensions != null && extensions.length > 0) {
			for (Xpp3Dom ex : extensions) {
				String groupId = ex.getChild("groupId").getValue();
				String artifactId = ex.getChild("artifactId").getValue();
				if (extension.getArtifactId().equals(artifactId) && extension.getGroupId().equals(groupId)) {
					//nothing to do
					return;
				}
			}
		}
		Xpp3Dom newExtension = new Xpp3Dom("extension");
		addNode(newExtension, "groupId", extension.getGroupId());
		addNode(newExtension, "artifactId", extension.getArtifactId());
		addNode(newExtension, "version", extension.getVersion());
		dom.addChild(newExtension);
		
		try (Writer writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mvnExtension),"UTF-8")))) {
			Xpp3DomWriter.write(new PrettyPrintXMLWriter(writer), dom);
		}
	}

	private Artifact findLatestExtensionFor(String pluginId, IProgressMonitor monitor) {
		Artifact extension = new DefaultArtifact("io.takari.polyglot", pluginId, "jar", "0.1.13");
		//TODO search for latest version
		return extension;
	}
	
	private static void addNode(Xpp3Dom parent, String key, String value) {
		Xpp3Dom node = new Xpp3Dom(key);
		node.setValue(value);
		parent.addChild(node);
	}

}
