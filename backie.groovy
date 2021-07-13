@Grab(group='commons-io', module='commons-io', version='2.6')

import java.util.concurrent.TimeUnit
import org.apache.commons.io.FileUtils
import static groovy.io.FileType.FILES

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

String date = new Date().format('yyyy-MM-dd hh-mm')
String logFileName = "backie_${date}.out"
File logFile = new File(logFileName)

def printProgress = { long startTime, long total, long current ->
    StringBuilder string = new StringBuilder(140);   
    int percent = (int) (current * 100 / total);
    string
        .append('\r')
        .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
        .append(String.format(" %d%% [", percent))
        .append(String.join("", Collections.nCopies(percent, "=")))
        .append('>')
        .append(String.join("", Collections.nCopies(100 - percent, " ")))
        .append(']')
        .append(String.join(
	   "", 
           Collections.nCopies(current == 0 
              ? (int) (Math.log10(total)) 
              : (int) (Math.log10(total)) - (int) (Math.log10(current)), " "
            )
         ))
        .append(String.format(" %d/%d", current, total));

    System.out.print(string);
}

println "Inspecting which files from destination [${sourceFolderPath}] is backed up to target destination [${targetFolderPath}]..."
logFile << "\nSource File Destination[${sourceFolderPath}]   Target File Destination(s)[${targetFolderPath}]\n\n"

int current = 0;
int total = 0;

new File(sourceFolderPath).traverse(type: groovy.io.FileType.FILES) { it ->
   total = total + 1
}

long startTime = System.currentTimeMillis();

List<String> filesNotBackedUp = []

new File(sourceFolderPath).traverse(type: groovy.io.FileType.FILES) { source_it ->
    printProgress(startTime,total,current)
    current = current + 1
    String currentSourceFileName = source_it.toString()
    File currentSourceFile = new File(currentSourceFileName)
    logFile << currentSourceFileName + "   "
    Boolean found = false
    new File(targetFolderPath).traverse(type: groovy.io.FileType.FILES) { target_it ->
	String currentTargetFileName = target_it.toString()
        File currentTargetFile = new File(currentTargetFileName)
        if(FileUtils.contentEquals(currentSourceFile, currentTargetFile)) {
            found = true    
	    logFile << currentTargetFileName + "   "
	    if(shouldDeleteBackedUpFiles) {
	       logFile << "\nDeleting file from source target..."
	       currentSourceFile.delete()
            }
        }
    }
    if(!found) filesNotBackedUp.add(currentSourceFileName) 
    logFile << "\n"
}

printProgress(0,total,total)

println "\nAlmost done..."

logFile << "\n\n\nSource target's files that have not beed backed up\n\n"
filesNotBackedUp.forEach { 
   logFile << it + "\n"
}

println "Inspection complete...Output file: ${logFileName}"
