/*************************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.polyglot.poc.internal.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.jboss.tools.maven.polyglot.poc.internal.core.PolyglotSupportActivator;


/**
 * Preferences initializer.
 * 
 * @author Fred Bricon
 */
public class MavenPolyglotPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	   */
	  @Override
	  public void initializeDefaultPreferences() {
	    IEclipsePreferences store = DefaultScope.INSTANCE.getNode(PolyglotSupportActivator.PLUGIN_ID);
	    store.putBoolean(IPolyglotPreferenceConstants.ENABLE_AUTOMATIC_POM_TRANSLATION, true);
	  }

}
