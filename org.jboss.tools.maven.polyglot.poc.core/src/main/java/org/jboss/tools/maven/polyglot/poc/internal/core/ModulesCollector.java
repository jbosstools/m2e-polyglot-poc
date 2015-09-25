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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * Collects {@link IMavenProjectFacade} modules for a given {@link IMavenProjectFacade}
 * 
 * @author Fred Bricon
 */
public class ModulesCollector {

	private IMavenProjectFacade parent;

	private LinkedHashSet<IMavenProjectFacade> modules;

	private List<IMavenProjectFacade> allFacades;
	
	public ModulesCollector(IMavenProjectFacade parent, IMavenProjectFacade[] allFacades) {
		this.parent = parent;
		this.allFacades = new ArrayList<>(Arrays.asList(allFacades));
		this.allFacades.remove(parent);
	}
	
	public synchronized Set<IMavenProjectFacade> getModules(IProgressMonitor monitor) {
		if (modules == null) {
			modules = new LinkedHashSet<>();
			crawlModules(parent, modules, allFacades, monitor);
		}
		return Collections.unmodifiableSet(modules);
	}

	private static void crawlModules(IMavenProjectFacade parent, LinkedHashSet<IMavenProjectFacade> modules, List<IMavenProjectFacade> allFacades, IProgressMonitor monitor) {
		if (!parent.getPackaging().equals("pom")) {
			return;
		}
		IPath path = parent.getProject().getLocation();
		for (String module : parent.getMavenProjectModules()) {
			if (monitor.isCanceled()) {
				break;
			}
			IPath modulePath = path.append(module);
			IMavenProjectFacade moduleProject = findFacade(modulePath, allFacades);
			if (moduleProject != null) {
				modules.add(moduleProject);
				allFacades.remove(moduleProject);
				crawlModules(moduleProject, modules, allFacades, monitor);
			}
		}
	}

	private static IMavenProjectFacade findFacade(IPath modulePath, List<IMavenProjectFacade> facades) {
		return facades.stream()
	            .filter(f -> modulePath.equals(f.getProject().getLocation()))
	            .findFirst().orElse(null);
	}
}
