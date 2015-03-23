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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PolyglotSupportActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.polyglot.poc.core";

	// The shared instance
	private static PolyglotSupportActivator plugin;

	private PolyglotPomChangeListener polyglotPomChangeListener;
	/**
	 * The constructor
	 */
	public PolyglotSupportActivator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    polyglotPomChangeListener = new PolyglotPomChangeListener();
	    workspace.addResourceChangeListener(polyglotPomChangeListener, IResourceChangeEvent.POST_CHANGE);

		plugin  = this;
	}

	public void stop(BundleContext context) throws Exception {
		if (polyglotPomChangeListener != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.removeResourceChangeListener(polyglotPomChangeListener);
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static PolyglotSupportActivator getDefault() {
		return plugin;
	}

}
