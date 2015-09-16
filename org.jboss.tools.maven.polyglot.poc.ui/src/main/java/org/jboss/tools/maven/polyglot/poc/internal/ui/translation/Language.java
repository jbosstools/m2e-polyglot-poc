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

/**
 * @author Fred Bricon
 */
public enum Language {

	atom,
	//clojure("clj"), clojure seems broken
	groovy,
	ruby("rb"),
	scala,
	yaml;
	
	private String fileExtension;
	private String mavenPluginId;

	Language() {
		this.fileExtension = name();
		this.mavenPluginId = "takari-polyglot-"+name();
	}
	
	Language(String extension) {
		this();
		this.fileExtension = extension;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public String getMavenPluginId() {
		return mavenPluginId;
	}
}
