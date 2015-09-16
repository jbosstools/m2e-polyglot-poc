package org.jboss.tools.maven.polyglot.poc.internal.ui.translation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

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
		final String language = translaterPage.getLanguage();
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
