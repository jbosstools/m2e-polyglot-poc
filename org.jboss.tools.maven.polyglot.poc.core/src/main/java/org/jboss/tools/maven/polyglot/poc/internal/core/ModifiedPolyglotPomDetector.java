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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class ModifiedPolyglotPomDetector implements IResourceDeltaVisitor {

	private static final Pattern POLYGLOT_POM_PATTERN = Pattern.compile("^pom\\.(rb|groovy|yaml|scala|atom|js|kt|clj)$");

	private static final String MAVEN_NATURE_ID = "org.eclipse.m2e.core.maven2Nature";
	
	public List<IFile> poms = new ArrayList<>();

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		boolean added = false;
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			added = true;
		case IResourceDelta.CHANGED:
			int flags = delta.getFlags();
			if (added || (flags & IResourceDelta.CONTENT) != 0) {
				IResource res = delta.getResource();
				if (!(res instanceof IFile) || res.isDerived()) {
					return true;
				}
				
				IFile file = (IFile) res;
				if (!isMavenProject(file.getProject())) {
					return false;
				}

				if (isAtRoot(file) && 
						isPolyglotPom(file)) {
					poms.add(file);
				}
				return false;
			}
			return true;
		default:
			break;
		}
		return false; // visit the children
	}

	private boolean isAtRoot(IFile file) {
		return file.getProjectRelativePath().segmentCount() == 1;
	}

	protected boolean isPolyglotPom(IFile file) {
		Matcher m = POLYGLOT_POM_PATTERN.matcher(file.getName());
		return m.matches();
	}

	private boolean isMavenProject(IProject project) throws CoreException {
		return project != null && project.isAccessible() && project.hasNature(MAVEN_NATURE_ID);
	}

}
