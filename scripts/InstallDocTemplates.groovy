/**
* Code used from Marcel Overdijk's InstallTemplates Gant script.
*/

includeTargets << grailsScript("_GrailsInit")

target ('default': "Installs the documentation template") {
   depends(checkVersion, parseArguments)

   targetDir = "${basedir}/src/docs/template"
   overwrite = false

   // only if template dir already exists in, ask to overwrite templates
   if (new File(targetDir).exists()) {
	   if (!isInteractive || confirmInput("Overwrite existing documentation templates? [y/n]","overwrite.doc.templates")) {
		   overwrite = true
	   }
   }
   else {
	   ant.mkdir(dir: targetDir)
   }
   
   ant.copy(todir: targetDir) {
	   fileset(dir:"${newDocPluginDir}/src/template")
   }
   event("StatusUpdate", [ "Documentation templates installed successfully"])
}
