@Grab(group='commons-io', module='commons-io', version='2.6')

import static groovy.io.FileType.FILES
import org.apache.commons.io.FileUtils

String sourceFolderPath, targetFolderPath
Boolean shouldDeleteBackedUpFiles = false
this.args.eachWithIndex { arg, indx ->
    if(arg == "-s") sourceFolderPath = this.args[indx + 1]
    if(arg == "-t") targetFolderPath = this.args[indx + 1]
    if(arg == "-d") shouldDeleteBackedUpFiles = this.args[indx + 1].toBoolean()
}

if(!sourceFolderPath) { println "Specify the source folder with -s flag..."; System.exit(0) }
if(!targetFolderPath) { println "Specify the target folder with -t flag..."; System.exit(0) }
if(shouldDeleteBackedUpFiles) { 
   println "Backie will delete source files that have already been bucked up in target directory"
   println("Are you sure that you want to proceed? (y|n)")
   char answer = System.in.newReader().readLine() as char
   if(answer != "y") System.exit(0)
}

List<String> filesNotBackedUp = []

println "\nSource File Destination[${sourceFolderPath}]   Target File Destination(s)[${targetFolderPath}]\n"
new File(sourceFolderPath).traverse(type: groovy.io.FileType.FILES) { source_it ->
    String currentSourceFileName = source_it.toString()
    File currentSourceFile = new File(currentSourceFileName)
    print currentSourceFileName + "   "
    Boolean found = false
    new File(targetFolderPath).traverse(type: groovy.io.FileType.FILES) { target_it ->
	String currentTargetFileName = target_it.toString()
        File currentTargetFile = new File(currentTargetFileName)
        if(FileUtils.contentEquals(currentSourceFile, currentTargetFile)) {
            found = true    
	    print currentTargetFileName + "   "
	    if(shouldDeleteBackedUpFiles) {
	       print "\nDeleting file from source target..."
	       currentSourceFile.delete()
            }
        }
    }
    if(!found) filesNotBackedUp.add(currentSourceFileName) 
    println ""
}

println "\n\n\nSource target's files that have not beed backed up\n"
filesNotBackedUp.forEach { it -> println it }




