Proof of Concept POM Polyglot support for Eclipse M2E.

[Maven 3.3.1](http://takari.io/2015/03/19/polyglot-maven.html) introduced the possibility of using other formats than xml to represent the project object model (POM).

In the Eclipse IDE though, M2E currently requires a pom.xml file to be able to work.

This experimental plugin will allow you to edit any `pom.<groovy|scala|rb|yaml|atom>` and a `pom.xml` file will automatically be (re)generated (and marked as derived), allowing M2E to pick up your changes.

If errors occur during the translation (due to a malformed pom for instance), the pom.xml will not be updated, so the project stays in a buildable form within Eclipse (else you'd get a blank pom.xml file).

You can now convert an existing Maven project to Polyglot Maven via right-click on a project and `Configure > Convert to Maven Polyglot...`

There are a few caveats though :

- don't try that with the projects you care too much about, you **will** loose your original pom.xml
- you can't import/start without pom.xml files first, even then you **will** loose your original pom.xml after you edit your polyglot pom
- it's enabled even if no `.mvn/extensions.xml` are defined in the project ancestry, so you **will** loose your original pom.xml
- M2E will most likely use a different mechanism to support polyglot Maven
- did I mention you **will** loose your original pom.xml?

Now projects without a polyglot pom should not be impacted. Also, you can always disable the plugin under Preferences > Maven > Polyglot Support (Experimental).

Now that you've been warned, please read http://takari.io/2015/03/21/polyglot-maven.html to understand how to convert your existing pom.xml files to `pom.<whatever>`.

This screencast shows you how to do it from within Eclipse directly : http://screencast.com/t/ls1In7Uq (without sound)

So if still want to have fun while loosing your original pom.xml files, you can install `M2E Polyglot Support Experiment` from this p2 update site : `http://dl.bintray.com/jbosstools/m2e-polyglot-poc/`

This feature requires Java 1.8. If needed, you can edit your eclipse.ini (or the equivalent .ini configuration of your Eclipse based product) and add the -vm parameter, pointing at a JDK 1.8. See https://wiki.eclipse.org/Eclipse.ini#Specifying_the_JVM
