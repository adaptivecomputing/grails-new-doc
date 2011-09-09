class NewDocGrailsPlugin {
    // the plugin version
    def version = "0.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

	def license = "APACHE"
	def organization = [ name:"Adaptive Computing", url:"http://adaptivecomputing.com" ]
	def issueManagement = [ system:"GitHub", url:"http://github.com/adaptivecomputing/grails-new-doc/issues" ]
	def scm = [ url:"http://github.com/adaptivecomputing/grails-new-doc" ]
	
    def author = "Brian Saville"
    def authorEmail = "bsaville@adaptivecomputing.com"
    def title = "New Grails Documentation (2.x docs in 1.x)"
    def description = '''\
This plugin is a backport of the additional functionality offered in the grails doc command in Grails 2.0.x.  It allows
YAML syntax to be used with a table of contents.  This also fixes the issue with duplicates in groovy doc by specifically
including the src/groovy, src/java and grails-app folders.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/new-doc"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
