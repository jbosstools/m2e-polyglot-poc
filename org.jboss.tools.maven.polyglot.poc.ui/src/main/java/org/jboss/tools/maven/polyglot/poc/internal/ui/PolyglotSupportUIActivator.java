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
package org.jboss.tools.maven.polyglot.poc.internal.ui;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jboss.tools.maven.polyglot.poc.internal.core.PolyglotSupportActivator;
import org.jboss.tools.maven.polyglot.poc.internal.core.preferences.IPolyglotPreferenceConstants;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PolyglotSupportUIActivator extends Plugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.maven.polyglot.poc.ui";

	// The shared instance
	private static PolyglotSupportUIActivator plugin;

	private IPreferenceStore preferenceStore;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		//forces activation of core plugin
		getPreferenceStore().getBoolean(IPolyglotPreferenceConstants.ENABLE_AUTOMATIC_POM_TRANSLATION);
		plugin  = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		preferenceStore = null;
		super.stop(context);
	}

	/**
	 * @return the shared instance
	 */
	public static PolyglotSupportUIActivator getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
	}

	public IPreferenceStore getPreferenceStore() {
    if (preferenceStore == null) {
      preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,PolyglotSupportActivator.PLUGIN_ID);
    }
    return preferenceStore;
	}

}
