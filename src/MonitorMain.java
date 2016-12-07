import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.cli.*;


public class MonitorMain {

	public static long folderSize(File directory, boolean firsttime) {
		if (firsttime){
			if (directory.isFile()){
				return directory.length();
			}
		}
	    long length = 0;
	    long filelength = 0;
	    File[] files = directory.listFiles();
	   
	    List<File> filesOkList = new ArrayList<File>();

	    // faccio il conto solamente con i file regolari e le directory
	    for (File file : files){
	    	if (file.isDirectory() || file.isFile()){
	    		filesOkList.add(file);
	    	}
	    }

	    File[] filesOk = filesOkList.toArray(new File[0]);
	    
	    for (File file : filesOk) {
	        if (file.isFile()) {
	        	filelength = file.length(); 
	            length += filelength;
	        }
	        else {
	        	try {
	        		filelength = folderSize(file, false);
	        		length += filelength;
	        	}
	        	catch (Exception e){
	        		System.out.println("Warning: Skipping size of " + file.getAbsolutePath() + " Here is stack trace:");
	        		e.printStackTrace();
	        	}
	        }
	    }
	    return length;
	}
	
	public static boolean run = true;
	public static float tot_kb_written = 0;
	
	public static void main(String[] args) throws IOException {
		
		Options options = new Options();
		
		Option directory = new Option("b", "basedir", true, "input dir path");
		directory.setRequired(true);
        options.addOption(directory);

		Option overhead = new Option("o", "overhead", true, "input overhead in %, da 1 a 100");
		overhead.setRequired(false);
        options.addOption(overhead);

		Option mname = new Option("n", "monitorname", true, "input monitor name");
		mname.setRequired(true);
        options.addOption(mname);

		Option logfile = new Option("l", "log", true, "input path of the log file");
		logfile.setRequired(true);
        options.addOption(logfile);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar MonitorFileDir.jar", options);

            System.exit(1);
            return;
        }
        
        System.out.println("::Global Settings::");
        
        String dir = cmd.getOptionValue("basedir");
        int overheadperc;
        float overheadfactor;
        if (cmd.hasOption("overhead")){
        	overheadperc = Integer.parseInt(cmd.getOptionValue("overhead"));
        	
        }
        else {
        	overheadperc = 0;
        }
        
        String monitorname = cmd.getOptionValue("monitorname");
        String logFile = cmd.getOptionValue("log");
        
        System.out.println(" -- Overhead impostato: " + overheadperc + "%");
        System.out.println(" -- monitor name: " + monitorname);
        
        
		
		// starting..
        monitorStart(dir, overheadperc, logFile, monitorname);


	}
	
	public static void monitorStart(String dir, int overheadperc, String logFile, String monitorname) throws IOException {
		float overheadfactor = (float)overheadperc / 100;
		File f = new File(dir);
		int sleepTimeMillis = 5000;
		double size = 0;
		double newsize = 0;
		double thr = 0;
		DecimalFormat df = new DecimalFormat("#0.00");
		System.out.println("::Monitor Started::");
		
		FileWriter fw = new FileWriter (logFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Shutting down...");
                System.out.println("Total Received: " + tot_kb_written + "Kb");
                // out.println(output);
                run = false;
            }
        });
		run = true;
		tot_kb_written = 0;
		boolean firsttime = true;
		



		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		while (run){
			newsize = (folderSize(f, true) / 1024);
			// df.format(newsize);
			// System.out.println(newsize+ " Mb");
			thr = ((newsize - size) / (double)(sleepTimeMillis/1000)  * (1 - overheadfactor));
			if (firsttime){
				size = newsize;
				firsttime = false;
			}
			else{
				cal = Calendar.getInstance();
				String date = dateFormat.format(cal.getTime());
				String output_std = date + " - " + monitorname + " Throughput: " + df.format(thr) + "Kb/s";
				String output_file = date + '\t' + monitorname + " Throughput" + '\t' + df.format(thr) + '\t' + "Kb/s";

				System.out.println(output_std);
				out.println(output_file);
				out.flush();
				tot_kb_written += thr * ((float)sleepTimeMillis / 1000);
				size = newsize;
			}
			try{
				Thread.sleep(sleepTimeMillis);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		out.close();
	}

}
