package org.jboss.tools.maven.polyglot.poc.internal.core;

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;

public class ArtifactSearcher {

	private ArtifactSearcher() {
	}
	
	public static String getLatestVersion(Artifact artifact) throws CoreException {
	        try {
	            final IMaven maven = MavenPlugin.getMaven();
	            final ArtifactMetadataSource source = ((MavenImpl) maven).getPlexusContainer().lookup(
	                    ArtifactMetadataSource.class, "org.apache.maven.artifact.metadata.ArtifactMetadataSource", "maven"); //$NON-NLS-1$  $NON-NLS-2$
	            List<ArtifactVersion> versions = source.retrieveAvailableVersions(artifact, 
	            																  maven.getLocalRepository(),
	            															  maven.getArtifactRepositories());
	            
	            Collections.reverse(versions);
	            for (ArtifactVersion artifactVersion : versions) {
					String version = artifactVersion.toString();
					if (!version.endsWith("-SNAPSHOT")) {
						return version;
					}
				}
	        } catch (Exception e) {
	            throw new CoreException(new Status(Status.ERROR, PolyglotSupportActivator.PLUGIN_ID, "Error resolving version range", e)); //$NON-NLS-1$
	        }
	        return null;
    }
}
