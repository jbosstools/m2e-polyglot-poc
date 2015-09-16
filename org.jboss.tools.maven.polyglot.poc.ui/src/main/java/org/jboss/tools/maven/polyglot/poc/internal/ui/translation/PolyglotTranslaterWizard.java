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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * @author Fred Bricon
 */
public class PolyglotTranslaterWizard extends Wizard {

	private PolyglotTranslaterWizardPage translaterPage;
	private IMavenProjectFacade facade;

	public PolyglotTranslaterWizard(IMavenProjectFacade facade) {
		this.facade = facade;
	}

	@Override
	public void addPages() {
		translaterPage = new PolyglotTranslaterWizardPage(facade);
		addPage(translaterPage);
	}

	@Override
	public boolean performFinish() {
		final Language language = translaterPage.getLanguage();
		final boolean isAddExtension = translaterPage.isAddExtension();
		final File mvnExtensionsDir = translaterPage.getMvnExtensionsDir(); 
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				monitor.beginTask("Project Translation", 3);
				try {
					// No, this is not how Jobs are supposed to be used
					IStatus result = new PolyglotTranslaterJob(facade, language, isAddExtension,
							mvnExtensionsDir).run(monitor);
					if (!result.isOK()) {
						throw new InvocationTargetException(result.getException());
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Translation failed", realException.getMessage());
			return false;
		}
		return true;
	}
}
