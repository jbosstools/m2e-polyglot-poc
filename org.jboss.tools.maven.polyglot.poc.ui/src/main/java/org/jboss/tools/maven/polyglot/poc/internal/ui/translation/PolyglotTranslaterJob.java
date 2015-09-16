package org.jboss.tools.maven.polyglot.poc.internal.ui.translation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collections;

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.maven.polyglot.poc.internal.core.PomTranslatorJob;
import org.jboss.tools.maven.polyglot.poc.internal.ui.PolyglotSupportUIActivator;

public class PolyglotTranslaterJob extends PomTranslatorJob {

	private IMavenProjectFacade facade;
	private String language;
	private boolean addExtension;
	private File mvnExtensionsDir;

	public PolyglotTranslaterJob(IMavenProjectFacade facade, String language, boolean addExtension, File mvnExtensionsDir) {
		super(Collections.singletonList(facade.getPom()));
		this.facade = facade;
		this.language = language;
		this.addExtension = addExtension;
		this.mvnExtensionsDir = mvnExtensionsDir;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IFile output = facade.getPom().getParent().getFile(new Path("pom."+language));
		try {
			if (addExtension) {
				Artifact extension = findLatestExtensionFor(language, monitor);
				addExtension(mvnExtensionsDir, extension);
			}
			monitor.worked(1);
			translate(facade.getPom(), facade.getPom(), output, monitor);
			monitor.worked(1);
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
		if (mvnExtension.exists()) {
			try (FileInputStream fis = new FileInputStream(mvnExtension)) {
				dom = new Xpp3DomBuilder().build(fis, "UTF-8");
			}
		} else {
			dom = new Xpp3DomBuilder().build(new StringReader("<extensions>\n</extensions>"));
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
		Xpp3Dom aid = new Xpp3Dom("artifactId");
		newExtension.addChild(aid);
		
		aid.setValue(extension.getArtifactId());
		Xpp3Dom gid = new Xpp3Dom("groupId");
		newExtension.addChild(gid);
		
		gid.setValue(extension.getGroupId());
		Xpp3Dom version = new Xpp3Dom("version");
		version.setValue(extension.getVersion());
		newExtension.addChild(version);
		
		
		dom.addChild(newExtension);
		try (Writer writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mvnExtension),"UTF-8")))) {
			Xpp3DomWriter.write(new PrettyPrintXMLWriter(writer), dom);
		}
	}

	private Artifact findLatestExtensionFor(String language, IProgressMonitor monitor) {
		Artifact extension = new DefaultArtifact("io.takari.polyglot", "polyglot-"+language, "jar", "0.1.13");
		//TODO search for latest version
		return extension;
	}

}
