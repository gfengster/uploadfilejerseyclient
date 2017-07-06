package net.gf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * This example shows upload/size/md5/delete/list file with restful service.
 * <ol>
 * <li><b>upload</b> files using POST requests with encoding type "multipart/form-data". 
 * If the file is partial uploaded, client will upload the rest part only. 
 * The application will compare the MD5 checksum after the upload complete.<br>
 * <code>
 * for example: java -Xmx4096M -jar ./uploadfileclient-1-jar-with-dependencies.jar -file /media/gf/Java/linux_dist/Parrot-full-3.6_amd64.iso -request upload
 * </code>
 * </li>
 * <li><b>size</b> of uploaded file can be retrieved using GET.<br>
 * <code>
 * java -Xmx4096M -jar ./target/uploadfileclient-1-jar-with-dependencies.jar -file Parrot-full-3.6_amd64.iso -request size
 * </code>
 * </li>
 * <li><b>md5</b> checksum of uploaded file using GET.<br>
 * <code>
 * java -Xmx4096M -jar ./target/uploadfileclient-1-jar-with-dependencies.jar -file Parrot-full-3.6_amd64.iso -request md5
 * </code>
 * </li>
 * <li><b>list</b> all uploaded files using GET.<br>
 * <code>
 * java -Xmx4096M -jar ./target/uploadfileclient-1-jar-with-dependencies.jar -request list
 * </code>
 * </li>
 * <li><b>delete</b> a file using DELETE.<br>
 * <code>
 * for example: java -Xmx4096M -jar ./target/uploadfileclient-1-jar-with-dependencies.jar -file Parrot-full-3.6_amd64.iso -request delete
 * </code>
 * </li>
 * </ol>
 * @author gf
 */
public class UploadFileClient {
	private static Logger logger = Logger.getLogger(UploadFileClient.class.getName());
	
	private static final String UPLOAD_URL = "/uploadfile/service/upload";
	private static final String APPEND_URL = "/uploadfile/service/append";
	private static final String SIZE_URL = "/uploadfile/service/size/";
	private static final String MD5_URL = "/uploadfile/service/md5/";
	private static final String DELETE_URL = "/uploadfile/service/delete/";
	private static final String FILES_URL = "/uploadfile/service/list";
	
	private static final long REMOTE_FILESIZE_INTERVAL = TimeUnit.MILLISECONDS.toMillis(100);
	
	
	private final String authString;
	
	private final File uploadFile;
	
	
	private final String url;
	
	public UploadFileClient(final String url, final String username, final String password, 
			final File uploadFile) {
		this.url = url;
		this.uploadFile = uploadFile;
		
		if (username != null && password != null)
			this.authString = Base64.getEncoder().encodeToString(
				username.concat(":").concat(password).getBytes());
		else
			authString = "";
		
		
	}
	
	public UploadFileClient(final String url, final String username, final String password, 
			final String uploadFileName) {
		this(url, username, password, new File(uploadFileName));
	}

	public boolean upload() {
		final long localFileSize = getLocalFileSize();
		final long remoteFileSize = getRemoteFileSize();
		
		logger.info("Local file size: " + localFileSize);
		logger.info("Remote file size: " + remoteFileSize);
		
		boolean ret = true;
		
		if (remoteFileSize == 0 || remoteFileSize > localFileSize) {
			//ret = uploadFile();
		} else if (remoteFileSize < localFileSize) {
			//ret = appendFile(remoteFileSize);
		} else if (remoteFileSize == localFileSize 
				&& getLocalFileMD5().equals(getRemoteFileMD5())) {
			logger.info("Both files are identical.");
			return true;
		}
		
		if (!ret)
			return false;
		else
			return validate();
	}
	
	private boolean uploadFile(){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(uploadFile);
			
			final long totalSize = uploadFile.length();
			

			return false;
		
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read file");
			e.printStackTrace();
			
			return false;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public long getRemoteFileSize() {
		try {
			Client client = Client.create();

			WebResource webResource = client.resource(url.concat(SIZE_URL).concat(uploadFile.getName()));

			ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

			if (response.getStatusInfo() != ClientResponse.Status.OK) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String responseString = response.getEntity(String.class);

			return Long.parseLong(responseString);
		} catch (Exception e) {
			e.printStackTrace();

		}

		return 0;
	}
	
	public boolean validate(){
		final String localMD5 = getLocalFileMD5();
		
		if (localMD5.isEmpty()) {
			logger.info("Local file does not exist.");
		}
		
		final String remoteMD5 = getRemoteFileMD5();
		
		if (remoteMD5.isEmpty()) {
			logger.info("Remote file does not exist.");
		}
		
		if (localMD5.isEmpty() || remoteMD5.isEmpty())
			return false;
		
		if(localMD5.equals(remoteMD5)){
			return true;
		} else {
			logger.info("MD5 comparison fail.");
			return false;
		}
	}
	
	public long getLocalFileSize(){
		return uploadFile.length();
	}
	
	public String getRemoteFileMD5() {
		try {
			Client client = Client.create();

			WebResource webResource = client.resource(url.concat(MD5_URL).concat(uploadFile.getName()));

			ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);

			if (response.getStatusInfo() != ClientResponse.Status.OK) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String responseString = response.getEntity(String.class);

			return responseString;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "0";
	}

	public boolean delete() {
		try {
			Client client = Client.create();

			WebResource webResource = client.resource(url.concat(DELETE_URL).concat(uploadFile.getName()));

			ClientResponse response = webResource.header("Authorization", authString).
					accept(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
		
			if (response.getStatusInfo() != ClientResponse.Status.OK) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
			
			return response.getStatusInfo() == Status.OK;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public String getLocalFileMD5(){
		if (uploadFile.exists() && !uploadFile.isDirectory()) {
			InputStream inputStream = null;
			try {
				final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
				
				inputStream = new FileInputStream(uploadFile);
				final byte[] buffer = new byte[8192];
				int len = 0;

				while ((len = inputStream.read(buffer)) != -1) {
					messageDigest.update(buffer, 0, len);
				}

				final byte[] hash = messageDigest.digest();
				
				final String md5 = String.format("%032x", new BigInteger(1,hash));
				
				logger.info("Local file MD5: " + md5);
				
				return md5;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(inputStream != null)
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		return "";
	}
	
	public Collection<String> getAllUploadedFiles(){
		final Collection<String> allfiles = new ArrayList<>();

		Scanner scanner = null;
		try {
			Client client = Client.create();

			WebResource webResource = client.resource(url.concat(FILES_URL));

			ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN)
					.get(ClientResponse.class);

			if (response.getStatusInfo() != ClientResponse.Status.OK) {
			   throw new RuntimeException("Failed : HTTP error code : "
					   	+ response.getStatus());
			}

			String responseString = response.getEntity(String.class);

			scanner = new Scanner(responseString);
			while(scanner.hasNextLine()){
				allfiles.add(scanner.nextLine());
			}
		  } catch (Exception e) {

			e.printStackTrace();

		  } finally {
			  if(scanner != null)
					scanner.close();
		}
		return allfiles;
	}
	
	public void close(){
		
	}
	
	private boolean appendFile(final long skip) {
		FileInputStream fis = null;
		try {
			final long totalSize = uploadFile.length();
			
			fis = new FileInputStream(uploadFile);
			
			fis.skip(skip);
			
		
			return false;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to read file");
			e.printStackTrace();
		
			return false;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class RemoteProgressTask extends TimerTask {
		private long transferred = 0;
		private final long totalSize;
		
		RemoteProgressTask(long totalSize, long skip) {
			this.totalSize = totalSize;
			
		}
		
		public void run() {
			updateSize();
		}
		
		public void updateSize(){
			long size = getRemoteFileSize();
			long diff = size - transferred;
			transferred = size;
			
		}
	}
}
