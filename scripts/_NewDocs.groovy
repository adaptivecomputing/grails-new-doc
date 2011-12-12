/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.Project
import org.codehaus.groovy.grails.documentation.DocumentationContext
import org.codehaus.groovy.grails.documentation.DocumentedMethod
import org.codehaus.groovy.grails.resolve.IvyDependencyManager

import grails.util.GrailsNameUtils

/**
 * Copied from 2.0 _GrailsDocs.groovy
 * @author Graeme Rocher, Brian Saville
 * @since 1.0
 *
 * Created: Sep 20, 2007
 * Modified: Sep 1, 2011
 */

includeTargets << grailsScript("_GrailsPackage")

if (grailsSettings.metaClass.hasProperty(grailsSettings, "docsOutputDir")) {
	javadocDir = "${grailsSettings.docsOutputDir}/api"
	groovydocDir = "${grailsSettings.docsOutputDir}/gapi"
} else {
	javadocDir = "${basedir}/docs/api"
	groovydocDir = "${basedir}/docs/gapi"
}
docEncoding = "UTF-8"
docSourceLevel = "1.5"
links = ['http://java.sun.com/j2se/1.5.0/docs/api/']

docsDisabled = { argsMap.nodoc == true }
pdfEnabled = { argsMap.pdf == true }

createdManual = false
createdPdf = false

target(newDocs: "Produces documentation for a Grails project") {
    parseArguments()
    if (argsMap.init) {
        ant.mkdir(dir:"${basedir}/src/docs/guide")
        ant.mkdir(dir:"${basedir}/src/docs/ref/Items")
        new File("${basedir}/src/docs/guide/introduction.gdoc").write '''
This is an example documentation template. The syntax format is similar to "Textile":http://textile.thresholdstate.com/.

You can apply formatting such as *bold*, _italic_ and @code@. Bullets are possible too:

* Bullet 1
* Bullet 2

As well as numbered lists:

# Number 1
# Number 2

The documentation also handles links to [guide items|guide:1. Introduction] as well as [reference|items]
        '''

        new File("${basedir}/src/docs/ref/Items/reference.gdoc").write '''
h1. example

h2. Purpose

This is an example reference item.

h2. Examples

You can use code snippets:

{code}
def example = new Example()
{code}

h2. Description

And provide a detailed description
        '''
		
		new File("${basedir}/src/docs/guide/toc.yml").write "introduction: Introduction"

        event("StatusUpdate", ["Example documentation created in ${basedir}/src/docs. Use 'grails new-doc' to publish."])
    }
    else {
        docsInternal()
    }
}

target(docsInternal:"Actual documentation task") {
    depends(compile, javadoc, groovydoc, refdocs, pdf, createIndex)
}

target(setupDoc:"Sets up the doc directories") {
    ant.mkdir(dir:grailsSettings.docsOutputDir)
    ant.mkdir(dir:groovydocDir)
    ant.mkdir(dir:javadocDir)
    IvyDependencyManager dependencyManager = grailsSettings.dependencyManager
    dependencyManager.loadDependencies('docs')
}

target(groovydoc:"Produces groovydoc documentation") {
    depends(parseArguments, createConfig, setupDoc)

    if (docsDisabled()) {
        event("DocSkip", ['groovydoc'])
        return
    }

    ant.taskdef(name:"groovydoc", classname:"org.codehaus.groovy.ant.Groovydoc")
    event("DocStart", ['groovydoc'])
	
	final Project project = new Project()
    final Path sourcePath = new Path(project)
	
	def exclusions = config?.grails?.plugins?.newdoc?.exclusions?.collect { new File(it) } ?: []
	def inclusions = config?.grails?.plugins?.newdoc?.inclusions?.collect { new File(it) } ?: []
	
	File dirFile = new File('grails-app')
	if (!exclusions.contains(dirFile)) {
		dirFile.eachDir { File subdir ->
			if (!exclusions.contains(subdir))
				sourcePath.add(new Path(project, subdir.absolutePath))
			else
				println "Excluding sub directory ${subdir}"
		}
	} else {
		println "Excluding directory ${dirFile}"
	}
	
	['src/groovy', 'src/java'].each { String dir ->
		dirFile = new File(dir)
		if (!exclusions.contains(dirFile)) {
			sourcePath.add(new Path(project, dirFile.absolutePath))
		} else {
			println "Excluding directory ${dirFile}"
		}
	}
	inclusions.each { File dir ->
		println "Including directory ${dir}"
		sourcePath.add(new Path(project, dir.absolutePath))
	}

    if (isPluginProject) {
        def pluginDescriptor = grailsSettings.baseDir.listFiles().find { it.name.endsWith "GrailsPlugin.groovy" }
        def tmpDir = new File(grailsSettings.projectWorkDir, "pluginDescForDocs")
        tmpDir.deleteOnExit()

        // Copy the plugin descriptor to a temporary directory and add that
        // directory to groovydoc's source path. This is because adding '.'
        // will cause all Groovy files in the project to be included as source
        // files (including test cases) and it will also cause duplication
        // of classes in the generated docs - see
        //
        //     http://jira.grails.org/browse/GRAILS-6530
        //
        // Also, we can't add a single file to the path. Only directories
        // seem to work. There are quite a few limitations with the GroovyDoc
        // task currently.
        ant.copy file: pluginDescriptor, todir: tmpDir, overwrite: true

        sourcePath.add new Path(ant.project, tmpDir.absolutePath)
    }

    try {
        ant.groovydoc(destdir:groovydocDir, sourcepath:sourcePath, use:"true",
                      windowtitle:grailsAppName,'private':"true")
    }
    catch(Exception e) {
        event("StatusError", ["Error generating groovydoc: ${e.message}"])
    }
    event("DocEnd", ['groovydoc'])
}

target(javadoc:"Produces javadoc documentation") {
    depends(parseArguments, setupDoc)

    if (docsDisabled()) {
        event("DocSkip", ['javadoc'])
        return
    }

    event("DocStart", ['javadoc'])
    File javaDir = new File("${grailsSettings.sourceDir}/java")
    if (javaDir.listFiles().find{ !it.name.startsWith(".")}) {
        try {
            ant.javadoc(access:"protected",
                        destdir:javadocDir,
                        encoding:docEncoding,
                        classpathref:"grails.compile.classpath",
                        use:"yes",
                        windowtitle:grailsAppName,
                        docencoding:docEncoding,
                        charset:docEncoding,
                        source:docSourceLevel,
                        useexternalfile:"yes",
                        breakiterator:"true",
                        linksource:"yes",
                        maxmemory:"128m",
                        failonerror:false,
                        sourcepath:javaDir.absolutePath) {
                for (i in links) {
                    link(href:i)
                }
            }
        }
        catch (Exception e) {
            event("StatusError", ["Error generating javadoc: ${e.message}"])
            // ignore, empty src/java directory
        }
    }
    event("DocEnd", ['javadoc'])
}

target(refdocs:"Generates Grails style reference documentation") {
    depends(parseArguments, createConfig,loadPlugins, setupDoc)

    if (docsDisabled()) {
        event("DocSkip", ['refdocs'])
		return
    }
		
    def srcDocs = new File("${basedir}/src/docs")

    def context = DocumentationContext.getInstance()
    if (context?.hasMetadata()) {
        for (DocumentedMethod m in context.methods) {
            if (m.artefact && m.artefact != 'Unknown') {
                String refDir = "${srcDocs}/ref/${GrailsNameUtils.getNaturalName(m.artefact)}"
                ant.mkdir(dir:refDir)
                def refFile = new File("${refDir}/${m.name}.gdoc")
                if (!refFile.exists()) {
                    event("StatusUpdate", ["Generating documentation ${refFile}"])
                    refFile.write """
h1. ${m.name}

h2. Purpose

${m.text ?: ''}

h2. Examples

{code:java}
foo.${m.name}(${m.arguments?.collect {GrailsNameUtils.getPropertyName(it)}.join(',')})
{code}

h2. Description

${m.text ?: ''}

Arguments:

${m.arguments?.collect { '* @'+GrailsNameUtils.getPropertyName(it)+'@\n' }}
"""
                }
            }
        }
    }

    if (srcDocs.exists()) {
		event("DocStart", ["refdocs"])
        File refDocsDir = grailsSettings.docsOutputDir
        def publisher = loadClass("grails.plugins.newdoc.DocPublisher").newInstance(srcDocs, refDocsDir, newDocPluginDir)
        publisher.ant = ant
        publisher.title = grailsAppName
        publisher.subtitle = grailsAppName
        publisher.version = grailsAppVersion
        publisher.authors = ""
        publisher.license = ""
        publisher.copyright = ""
        publisher.footer = ""
        publisher.engineProperties = config?.grails?.doc
        println ">> ${config.grails.doc}"
        // if this is a plugin obtain additional metadata from the plugin
        readPluginMetadataForDocs(publisher)
        readDocProperties(publisher)
        configureAliases()

        try {
            publisher.publish()

            createdManual = true
            event("StatusUpdate", ["Built user manual at ${refDocsDir}/index.html"])
			event("DocEnd", ["refdocs"])
        }
        catch (RuntimeException ex) {
            if (ex.message) {
                event("StatusError", ["Failed to build user manual: ${ex.message}"])
            }
            else {
                event("StatusError", ["Failed to build user manual."])
            }
            exit 1
        }

    }
}

target(pdf: "Produces PDF documentation") {
    depends(parseArguments)

    File refDocsDir = grailsSettings.docsOutputDir
    File singleHtml = new File(refDocsDir, 'guide/single.html')

    if (docsDisabled() || !pdfEnabled() || !singleHtml.exists()) {
        event("DocSkip", ['pdf'])
        return
    }

    event("DocStart", ['pdf'])

    loadClass("grails.plugins.newdoc.PdfBuilder").build(grailsSettings.docsOutputDir.canonicalPath, grailsHome.toString())

    createdPdf = true

    println "Built user manual PDF at ${refDocsDir}/guide/single.pdf"

    event("DocEnd", ['pdf'])
}

target(createIndex: "Produces an index.html page in the root directory") {
    if (docsDisabled()) {
         return
    }

    new File("${grailsSettings.docsOutputDir}/all-docs.html").withWriter { writer ->
        writer.write """\
<html>

    <head>
        <title>$grailsAppName Documentation</title>
    </head>

    <body>
        <a href="api/index.html">Java API docs</a><br />
        <a href="gapi/index.html">Groovy API docs</a><br />
"""

        if (createdManual) {
            writer.write '\t\t<a href="guide/index.html">Manual (Page per chapter)</a><br />\n'
            writer.write '\t\t<a href="guide/single.html">Manual (Single page)</a><br />\n'
        }

        if (createdPdf) {
            writer.write '\t\t<a href="guide/single.pdf">Manual (PDF)</a><br />\n'
        }

        writer.write """\
    </body>
</html>
"""
    }
}

target(migrateNewDocs: "Migrates an old-style gdoc user guide to the current approach using a YAML TOC file.") {
    depends createConfig

    def guideDir = new File(grailsSettings.baseDir, "src/docs/guide")
    if (guideDir.exists()) {
        def outDir = new File(guideDir.parentFile, "migratedGuide")
        def migrator = loadClass("grails.plugins.newdoc.LegacyDocMigrator").newInstance(guideDir, outDir, config.grails.doc.alias)
        migrator.migrate()

        event("StatusUpdate", ["Migrated user guide at ${outDir.path}"])
    }
}

def readPluginMetadataForDocs(publisher) {
    def basePlugin = loadBasePlugin()?.instance
    if (basePlugin) {
        if (basePlugin.hasProperty("title")) {
            publisher.title = basePlugin.title
        }
        if (basePlugin.hasProperty("description")) {
            publisher.subtitle = basePlugin.description
        }
        if (basePlugin.hasProperty("version")) {
            publisher.version = basePlugin.version
        }
        if (basePlugin.hasProperty("license")) {
            publisher.license = basePlugin.license
        }
        if (basePlugin.hasProperty("author")) {
            publisher.authors = basePlugin.author
        }
    }
}

def readDocProperties(publisher) {
    ['copyright', 'license', 'authors', 'footer', 'images', 'tocDepth', "title", "subtitle",
     'css', 'style', 'encoding', 'logo', 'sponsorLogo'].each { readIfSet publisher, it }
}

def configureAliases() {
    // See http://jira.codehaus.org/browse/GRAILS-6484 for why this is soft loaded
    def docEngineClassName = "grails.doc.DocEngine"
    def docEngineClass = classLoader.loadClass(docEngineClassName)
    if (!docEngineClass) {
        throw new IllegalStateException("Failed to load $docEngineClassName to configure documentation aliases")
    }
    docEngineClass.ALIAS.putAll(config.grails.doc.alias)
}

private readIfSet(publisher,String prop) {
    if (config.grails.doc."$prop") {
        publisher[prop] = config.grails.doc."$prop"
    }
}

private loadBasePlugin() {
    pluginManager?.allPlugins?.find { it.basePlugin }
}


// Workaround for GRAILS-6453
loadClass = { String clazz ->
	def doLoad = { -> classLoader.loadClass(clazz) }
	try {
		doLoad()
	} catch (ClassNotFoundException e) {
		includeTargets << grailsScript("_GrailsCompile")
		compile()
		doLoad()
	}
}
