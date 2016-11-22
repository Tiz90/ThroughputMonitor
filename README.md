# MonitorFileDir

Java Application that monitors the size of a file or a folder

The application have to be runned via command line: #> java -jar jars/MonitorFileDir.jar <options>

Lista options:
 -b, --basedir <arg>: path of the directory / file that we want to monitor
 -n, --monitorname <arg>: monitor name. It is showed in the log file generated by the application
 -o, --overhead <arg>: you can specify an overhead of the size of files: for example if your file is written 100KB/s and you set an overhead of 20, the application will output a rate of 80KB/s (instead of 100). If you don't specify this option the overhead will be 0.
 -l, --log <arg>: path where the info about the rate will be stored. The file is a tab separated csv: line format is: <$date	$monitorname Throughput	x.yz	Kb/s>. Application will generate a line each 5 seconds.

# Example:
java -jar MonitorFileDir.jar -b /path/to/folder-or-file/ -o 20 -n Collector -l /path/to/logfile.log