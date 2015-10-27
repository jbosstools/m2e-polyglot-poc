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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Fred Bricon
 */
public class TranslateToPolyglotProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IProject project = getSelectedProject(event);
		if (project == null) {
			return null;
		}
		final Shell shell = HandlerUtil.getActiveShell(event);
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create(project, new NullProgressMonitor());
				if (facade == null) {
					return;
				}
				PolyglotTranslaterWizard wizard = new PolyglotTranslaterWizard(facade);
				WizardDialog wizardDialog = new WizardDialog(shell, wizard);
				wizardDialog.setPageSize(150, 100);
				wizardDialog.open();
			}
		});
		return null;
	}
	
	private IProject getSelectedProject(ExecutionEvent event) {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection)currentSelection).getFirstElement();
			if (o instanceof IResource) {
				return ((IResource)o).getProject();
			} else if (o instanceof IAdaptable) {
				return (IProject)((IAdaptable)o).getAdapter(IProject.class);
			}
		}
		return null;
	}
}
