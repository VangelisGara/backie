@Grab(group='commons-io', module='commons-io', version='2.6')

import org.apache.commons.io.FileUtils
import static groovy.io.FileType.FILES

// Get command line arguments
String sourceFolderPath, targetFolderPath
Boolean shouldDeleteBackedUpFiles = false
this.args.eachWithIndex { arg, indx ->
	if(arg == "-s") sourceFolderPath = this.args[indx + 1]
	if(arg == "-t") targetFolderPath = this.args[indx + 1]
	if(arg == "-d") shouldDeleteBackedUpFiles = this.args[indx + 1].toBoolean()
}

// Error handling for user input
if(!sourceFolderPath) { 
	println "Specify the source folder with -s flag..."; 
	System.exit(0) 
}

if(!targetFolderPath) { 
	println "Specify the target folder with -t flag..."; 
	System.exit(0)
}

if(shouldDeleteBackedUpFiles) { 
	println "Backie will delete source files that have already been bucked up in target directory"
	println("Are you sure that you want to proceed? (y|n)")
	char answer = System.in.newReader().readLine() as char
	if(answer != "y") System.exit(0)
	
}

// Create the log files
String date = new Date().format('yyyy-MM-dd hh-mm-ss')
String outputDirName = "backie_output_${date}"

File outputDir = new File(outputDirName)
outputDir.mkdir()

String logFileNameForBackedUp = "${outputDirName}/backed_up_files.out"
String logFileNameForNotBackedUp = "${outputDirName}/not_backed_up_files.out"

File logFileForBackedUp = new File(logFileNameForBackedUp)
File logFileForNotBackedUp = new File(logFileNameForNotBackedUp)

// The status bar closure
def printProgress = { long current, long total ->
	StringBuilder string = new StringBuilder(140)   
	int percent = (int) (current * 100 / total)
	string
		.append('\r')
		.append(" "*(percent == 0 ? 2 : 2 - (int) (Math.log10(percent))))
		.append(String.format("%d%% ", percent))
		.append("."*percent)
		.append(" "*(100-percent))
		.append(" "*(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current))))
        	.append(String.format(" %d/%d", current, total))

	System.out.print(string)
}

// Initialization phase
int current = 0;
int total = 0;

// Count total number of files in source directory
new File(sourceFolderPath).traverse(type: groovy.io.FileType.FILES) { it ->
	total = total + 1
}

// Backie logic starts here
println "Inspecting which files from destination [${sourceFolderPath}] is backed up to target destination [${targetFolderPath}]...\n"
logFileForBackedUp << "\nSource File Destination[${sourceFolderPath}]   Target File Destination(s)[${targetFolderPath}]\n\n"

List<String> filesNotBackedUp = []

// For each file in source folder
new File(sourceFolderPath).traverse(type: groovy.io.FileType.FILES) { source_it ->
	
	printProgress(current, total)

	String currentSourceFileName = source_it.toString()
	File currentSourceFile = new File(currentSourceFileName)
	
	Boolean found = false
	List<String> whereIsBackedUp = []
	
	// Check all files in target folder
	new File(targetFolderPath).traverse(type: groovy.io.FileType.FILES) { target_it ->
		
		String currentTargetFileName = target_it.toString()
		File currentTargetFile = new File(currentTargetFileName)
		
		// that have the same content
		if(FileUtils.contentEquals(currentSourceFile, currentTargetFile)) {
			found = true
			whereIsBackedUp.add(currentTargetFileName)
		}

	}

	// Backup logic 
	if(!found) { 
		filesNotBackedUp.add(currentSourceFileName)
	}
	else {
		if(shouldDeleteBackedUpFiles) {
			logFileForBackedUp << "\nDeleting file from source target..."
			currentSourceFile.delete()
		}
		
		logFileForBackedUp << currentSourceFileName + "   " + whereIsBackedUp + "\n"
	}
	
	current = current + 1
}

printProgress(total,total)

// Log not backed up files
println "\n\nAlmost done..."

logFileForNotBackedUp << "\n\n\nSource target's files that have not beed backed up\n\n"
filesNotBackedUp.forEach { 
	logFileForNotBackedUp << it + "\n"
}

// Final step
println "Inspection complete...See output files in folder: ${outputDirName}"
