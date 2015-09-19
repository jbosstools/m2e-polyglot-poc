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

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Fred Bricon
 */
public class PolyglotTranslaterWizardPage extends WizardPage {

	private boolean addExtension = true;
	private File mvnExtensionsDir;

	private Combo languagesCombo;
	
	public boolean isAddExtension() {
		return addExtension;
	}

	public File getMvnExtensionsDir() {
		return mvnExtensionsDir;
	}
	
	public Language getLanguage() {
		return Language.valueOf(languagesCombo.getText());
	}
	
	protected PolyglotTranslaterWizardPage(IMavenProjectFacade facade) {
		super("Translate Project to Polyglot Maven");
		setTitle("Translate "+facade.getProject().getName() + " to Polyglot Maven");
		mvnExtensionsDir = new File(facade.getPomFile().getParentFile(), ".mvn");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));
		
		Label lblTranslateProjectTo = new Label(container, SWT.NONE);
		lblTranslateProjectTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTranslateProjectTo.setText("Translate project pom.xml to");
		
		languagesCombo = new Combo(container, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 100;
		languagesCombo.setLayoutData(gd_combo);
		languagesCombo.setItems(getLanguages());
		languagesCombo.setText(languagesCombo.getItem(0));
	}

	private String[] getLanguages() {
		return Arrays.asList(Language.values())
				.stream()
				.map(lang -> lang.name())
				.collect(Collectors.toList())
				.toArray(new String[Language.values().length]);
	}
}
