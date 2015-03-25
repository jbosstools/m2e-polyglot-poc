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
package org.jboss.tools.maven.polyglot.poc.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jboss.tools.maven.polyglot.poc.internal.core.preferences.IPolyglotPreferenceConstants;
import org.jboss.tools.maven.polyglot.poc.internal.ui.PolyglotSupportUIActivator;

/**
 * @author Fred Bricon
 */
public class MavenPolyglotPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	  public MavenPolyglotPreferencePage() {
	    super(GRID);
	    setPreferenceStore(PolyglotSupportUIActivator.getDefault().getPreferenceStore());
	  }

	  public void init(IWorkbench workbench) {
	  }

	  public void createFieldEditors() {
	    addField(new BooleanFieldEditor(IPolyglotPreferenceConstants.ENABLE_AUTOMATIC_POM_TRANSLATION, 
	    		                        "Enable automatic polyglot pom translation. pom.xml will be overwritten!",
	        getFieldEditorParent()));
	  }

		@Override
		public boolean performOk() {
			if (super.performOk()) {
			}
			System.err.println(getPreferenceStore().needsSaving());
			return true;
		}
}