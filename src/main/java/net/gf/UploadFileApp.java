package net.gf;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

public class UploadFileApp {
	private static Logger logger = Logger.getLogger(UploadFileApp.class.getName());
	
	public static void main(String[] args) {
		String fileName = "";
		String request = "";
		
		if (args.length >= 2) {
			for (int i = 0; i < args.length; i++) {
				if ("-file".equals(args[i])) {
					fileName = args[++i];
				} else if ("-request".equals(args[i])) {
					request = args[++i];
				} else if("-help".equals(args[i])) {
					printUsage();
				}
			}
		} else {
			printUsage();
			System.exit(0);
		}
	
		final File inFile = new File(fileName);
		
		//final Collection<ProgressCallback> progresses = new ArrayList<>();
		//progresses.add(new ProgressPrinter());
		
		final UploadFileClient client = new UploadFileClient(
				"http://localhost:8080",
				"abc", "xyz", inFile);
		
		if ("upload".equals(request)) {
			//client.delete();
			logger.info("upload: " + client.upload());
		} else if("delete".equals(request)) {
			logger.info("delete:" + client.delete());
		} else if("size".equals(request)) {
			logger.info("Remote file size: " + client.getRemoteFileSize());
		} else if ("md5".equals(request)) {
			logger.info("Remote file MD5: " + client.getRemoteFileMD5());
		} else if("list".equals(request)) {
			final StringBuilder filesString = new StringBuilder("All upload files: \r\n");
			
			final Collection<String> allfiles = client.getAllUploadedFiles();
			
			for(String file : allfiles) {
				filesString.append(file).append("\r\n");
			}
			
			logger.info(filesString.toString());
		}
		client.close();
	}
	
	private static void printUsage(){
		logger.info("Usage: -file <upload file> -request <request> -help");
		logger.info("<request> is one of upload, delete, size, md5, list");
	}
	
	

}
