This plugin is a backport of Grails 2.x documentation (toc.yml, gdoc file structure without section numbers, fixed groovydoc duplicates, etc) to Grails 1.x projects.  It provides three scripts.

## Groovydoc Extensions

The new-doc command has been improved to include or exclude folders for use in building the groovydoc.  

### Excluding Folders

To exclude folders, use the following in Config.groovy:

```groovy
grails.plugins.newdoc.exclusions = ['grails-app', 'src/groovy']
```

Only the following folders may be excluded as these are the only ones that are loaded:

* grails-app
* Any subdirectory of grails-app (ie domain, conf, controllers), but none of their subdirectories
* src/groovy
* src/java

### Including Folders

If you wish to include certain folders, use the following syntax in Config.groovy:

```groovy
grails.plugins.newdoc.inclusions = ['grails-app/domain']
```

Beware that if you add sub-folders of src/groovy or src/java or any folder in any grails-app sub-directory, you will likely end up with duplicates.

This functionality allows you to exclude all groovydocs except for some arbitrary set, such as:

```groovy
grails.plugins.newdoc.exclusions = ['grails-app', 'src/groovy', 'src/java']
grails.plugins.newdoc.inclusions = ['grails-app/domain']
```

In this case, only the domain classes will be present in the GAPI generation.

## Commands

### grails new-doc

This command runs the documentation generation, including groovydoc and generating a guide if it exists at src/docs/guide.  See http://grails.org/doc/2.0.0.M2/guide/conf.html#docengine for more information.

### grails migrate-doc

This command migrates the Grails 1.x guide documentation into the 2.x format, including generating a links.yml file for legacy links.

### grails install-doc-templates

This command installs the documentation stylesheets and images to src/docs/template.  These templates can then be modified however is wished and when new-doc is run, these templates will be used.  NOTE: If this directory exists, the files in it will be copied to the output no matter if anything exists or not.

## Advantages of Grails 2.x Docs

* Use of a single toc.yml file to control titles of pages/sections and their placement in the guide
* No duplicates of classes in groovydoc (see http://jira.grails.org/browse/GRAILS-6530)
* Improved look for the guide (just like the grails official docs)

## Caveats

* Duplicates in groovydoc is avoided by using the patch in GRAILS-6530.  A more elegant and stable method is actually implemented in Grails 2.x, but it involved another property in the build scripts.  For this reason, only src/groovy, src/java, and grails-app are included in groovydoc generation.

## Release Notes

* 0.3.0 - Added publishing to PDF
* 0.2.0 - Added including and excluding of groovydoc
* 0.1.2 - Fixed issue #3 (Added install-doc-templates command and functionality)
* 0.1.1 - Initial public release