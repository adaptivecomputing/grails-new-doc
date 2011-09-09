This plugin is a backport of Grails 2.x documentation (toc.yml, gdoc file structure without section numbers, fixed groovydoc duplicates, etc) to Grails 1.x projects.  It provides two scripts.

## Commands

### grails new-doc

This command runs the documentation generation, including groovydoc and generating a guide if it exists at src/docs/guide.  See http://grails.org/doc/2.0.0.M2/guide/conf.html#docengine for more information.

### grails migrate-doc

This command migrates the Grails 1.x guide documentation into the 2.x format, including generating a links.yml file for legacy links.

## Advantages of Grails 2.x Docs

* Use of a single toc.yml file to control titles of pages/sections and their placement in the guide
* No duplicates of classes in groovydoc (see http://jira.grails.org/browse/GRAILS-6530)
* Improved look for the guide (just like the grails official docs)

## Caveats

* Duplicates in groovydoc is avoided by using the patch in GRAILS-6530.  A more elegant and stable method is actually implemented in Grails 2.x, but it involved another property in the build scripts.  For this reason, only src/groovy, src/java, and grails-app are included in groovydoc generation.

## Release Notes

* 0.1
** Initial release