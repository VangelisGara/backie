
# backie
 Backie is a simple groovy script that tracks which files from a source directory A has been backed up to a target directory B (along with their subdirectories). Backie will log files location in source directory and all different locations in target directory. It also logs the list of files that hasn't been backed up, yet. 

It works by searching all files in source directory that have the same content (bytes) with files of the target's directory.
 
 ### Run with:

    groovy backie.groovy -s <source directory> -t <target directoty> -d <boolean>
    
- -s : source directory. 
- -t: target directory.
- -d: if true, deletes all files in source directory that exist in target directory.

### Example

    groovy backie.groovy -s source -t target

### Requirements
The code is implemented and tested with groovy 3.0.7

### To implement next:
1. Export results to CSV.
2. Add concurrency with GPARS to optimize backie time.
3. Add progress bar.
4. Track elapsed time (to track possible optimizations).

### References
https://stackoverflow.com/questions/852665/command-line-progress-bar-in-java
https://stackoverflow.com/questions/687444/counting-the-number-of-files-in-a-directory-using-java
https://stackoverflow.com/questions/51994883/write-the-results-of-for-loop-to-csv-in-groovy
