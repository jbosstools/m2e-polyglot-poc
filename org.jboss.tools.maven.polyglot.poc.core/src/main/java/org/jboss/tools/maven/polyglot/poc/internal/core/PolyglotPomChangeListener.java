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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.jboss.tools.maven.polyglot.poc.internal.core.preferences.IPolyglotPreferenceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class PolyglotPomChangeListener implements IResourceChangeListener {
  
  private static Logger LOG = LoggerFactory.getLogger(PolyglotPomChangeListener.class);

  private IEclipsePreferences preferences;
	
  PolyglotPomChangeListener(IEclipsePreferences preferences) {
	  this.preferences = preferences;
  }
  
  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    if(isDisabled()) {
      return;
    }
    List<IFile> poms = null;
    try {
      ModifiedPolyglotPomDetector visitor = new ModifiedPolyglotPomDetector();
      event.getDelta().accept(visitor);
      poms = visitor.poms;
    } catch(CoreException e) {
      e.printStackTrace();
      return;
    }
    requestPomTranslation(poms);
  }

  private boolean isDisabled() {
	boolean isEnabled = preferences
	                    .getBoolean(IPolyglotPreferenceConstants.ENABLE_AUTOMATIC_POM_TRANSLATION, true);
    return !isEnabled;
  }

  protected void requestPomTranslation(List<IFile> poms) {
    if(poms != null && !poms.isEmpty()) {
      LOG.debug("Automatic update of {}", poms);
      new PomTranslatorJob(MavenPlugin.getMavenProjectRegistry(), MavenPluginActivator.getDefault().getMavenMarkerManager(), poms).schedule();
    }
  }
}
