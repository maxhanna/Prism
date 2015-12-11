import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
	static String lastReq = "";
	static int fileSize = 0;
	static String fileName = "";
	static String currentDirectory = System.getProperty("user.home").replace("\\", "/") + "/Desktop/";
	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// main creates the server, and serves based on the contexts that are set up.
	public static void main(String[] args) throws Exception 
	{

		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		InetAddress IP = InetAddress.getLocalHost();
		String myIP = IP.toString().substring(IP.toString().indexOf("/") + 1, IP.toString().length());
		String content = 
				"PRISM IS NOW ACTIVE \n"
						+"Step one: Open your other device's web browser.\n"
						+"Step two: Navigate to : http://" + myIP + ":8000\n"
						+"Start your filesharing!\n"
						+"(Close this window to stop Prism).\n"
						+"Guide to using the website:\n"
						+"You can use any device (laptop/mobile phone/tablet/computer/etc...) to access this device.\n"
						+"(*) To navigate through the file structure:\n"
						+"- Click on the ^ button and you will go up one directory"
						+" or you may click on a folder's link to go further down the file structure.\n"
						+"(*) To upload files:\n"
						+"- Click on Choose File button,\n"
						+"- Browse for the file to send click ok\n"
						+"- Click on Upload.\n"
						+"(*) To download files:\n"
						+"- Navigate through the file structure (on the prism webpage) for the file\n"
						+"- Click on the files' link.\n"
						+"(*) To delete one or more file(s):\n"
						+"- Click on the checkbox next to the link(s)\n"
						+"- Click on Delete.\n";
		JFrame f = new JFrame("Prism");
		f.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		URL url = new URL("http://www.hannaconsultantgroup.com/Prism.png");
		Image image;
		try 
		{
			image = ImageIO.read(url);
		}
		catch (IIOException e)
		{
			image = null;
		}
		ImageIcon icon = null;
		if (image != null)
		{
			icon = new ImageIcon(image);
			f.setIconImage(image);
		}
		JPanel panel = new JPanel();
		f.getContentPane().add(panel,"Center");
		JTextArea ta = new JTextArea(content, 21, 50);
		ta.setLineWrap(true);
		panel.add(new JScrollPane(ta));
		JLabel iconLabel = new JLabel();
		if (image != null && icon != null)
			iconLabel = new JLabel(icon);
		iconLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				openWebpage(URI.create("http://www.hannaconsultantgroup.com/prism.html"));
			}
		});
		panel.add(iconLabel);
		f.pack();
		f.setVisible(true);


		server.createContext("/", new getMain());
		server.createContext("/file_upload", new getUpload());
		server.createContext("/file_seek", new getFile());
		server.createContext("/folder_seek", new getFolder());
		server.createContext("/folder_up", new getFolderUp());
		server.createContext("/make_dir", new getMakeDir());
		server.createContext("/delete", new getDelete());
		server.setExecutor(null); // creates a default executor
		server.start();
	}
	public static int ordinalIndexOf(String str, char wantedChar, int wantedCharNum) {
		int pos = str.indexOf(wantedChar, 0);
		while (wantedCharNum-- > 0 && pos != -1)
			pos = str.indexOf(wantedChar, pos+1);
		return pos;
	}

	public static int numberOfOccurrences(String source, String sentence) {
		int occurrences = 0;

		if (source.contains(sentence)) {
			int withSentenceLength    = source.length();
			int withoutSentenceLength = source.replace(sentence, "").length();
			occurrences = (withSentenceLength - withoutSentenceLength) / sentence.length();
		}

		return occurrences;
	}

	//Takes an unformatted string and returns a corrected one
	//Some symbols like # still seem to crash the program
	public static String formatString(String s) {
		/*s = s.replace("%20", " ");
		s = s.replace("%5B", "[");
		s = s.replace("%5D", "]");
		s = s.replace("%7B", "{");
		s = s.replace("%7D", "}");
		s = s.replace("%60", "`");*/
		try {
			s = URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

	// Copy file writes a byte[] into a file.
	public static void copyFile (byte[] source, File dest)
	{	
		try {
			FileOutputStream fos = new FileOutputStream(dest);
			fos.write(source);
			fos.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	// appendFile appends byte[] into a file.
	public static void appendFile (byte[] source, File dest)
	{	
		try {
			FileOutputStream fos = new FileOutputStream(dest,true);
			fos.write(source);
			fos.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}

	/* The rest of the code in this class is dedicated to specialized context handlers */
	static class getMain implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			//printRequestHeaders(t);

			//DO POST request
			if (t.getRequestHeaders().getFirst("Content-length")!=null && Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"))>1)
			{
				//System.out.println("getMain POST handler");


			}
			//File is empty, no post request
			else
			{
				//System.out.println("getMain GET Handler");
				String response = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><title>Prism</title><link rel=icon type=\"image/png\" href=\"http://www.hannaconsultantgroup.com/prism_icon.ico\"></head><body><a href=\"http://www.hannaconsultantgroup.com/prism.html\"><a href=\"http://www.hannaconsultantgroup.com/prism.html\"><img src=http://www.hannaconsultantgroup.com/Prism.jpg width=50 height=50></a></a>"
						+ currentDirectory;
				//File System Level up button
				//Handles case where user it at root level
				String newFolderDir = currentDirectory;
				newFolderDir=newFolderDir.substring(newFolderDir.indexOf(":/"), newFolderDir.length());
				newFolderDir=newFolderDir.replace("://", "");
				//System.out.println(newFolderDir);
				response = response + "<table id=functionTable><tr>";
				if(!newFolderDir.equals("")){
					response=response+ " <td><form method=post action=folder_up name=folder_up id=folder_up><input form=folder_up type=submit value=\"^\" title=\"Go up one level in the file system\"></form></td>";
				}
				response=response + "<td>"
						+ "<script>"
						+ "function myFunction() {"
						+ "    var person = prompt(\"Choose a name for your new directory\", \"Directory Name\");"
						+ "    if (person != null) {"
						+ "        document.getElementById(\"dirName\").value = person ;"
						+ "		   document.getElementById(\"mkdir\").submit();"	
						+ "    }"
						+ "}"
						+ "</script>"
						+ "<form method=post action=make_dir name=mkdir id=mkdir enctype=\"multipart/form-data\"><input type=hidden form=mkdir id=dirName name=dirName> <input type=submit value=\"Create Directory\" form=\"mkdir\" onclick=\"myFunction();\" title=\"Create a directory in the current directory\"></form></td> "
						+ "</tr>"
						+ "</tr></table>"
						+ "<form id=myform name=myform method=post action=file_upload enctype=\"multipart/form-data\">"
						+ "<input type=file id=files name=files title=\"Choose a file to upload to the current directory\">"
						+ "<input type=hidden id=filesize name=filesize>"
						+ "<input type=submit name=submit value=\"Upload\" title=\"Upload chosen file to the current directory\"></form>"
						+ "			<script>"
						+ "				function handleFileSelect(evt) {"
						+ "				    var files = evt.target.files;"
						+ "					document.getElementById('filesize').value = files[0].size;"
						+ "				    document.getElementById('file_info').textContent = (files[0].name + ' file size: ' + files[0].size + ' bytes, last modified: ' + files[0].lastModifiedDate);"
						+ "				}"
						+ "				  document.getElementById('files').addEventListener('change', handleFileSelect, false);"
						+ "				</script>"
						+ "<div id=file_info></div>"
						+ "<table id=dataTable><tr><td><form method=post action=\"delete_items\" enctype=\"multipart/form-data\" id=\"delete_items\" onsubmit=\"return confirm('Do you really want to delete the file(s)?');\"></td> "
						+ "</tr> ";				
				File folder = new File(currentDirectory);
				File[] listOfFiles = folder.listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						response = response + "<tr><td><input name="+i+" type=checkbox value=\""+ listOfFiles[i].getName() +"\">File</td> <td><a href=\"file_seek/" + URLEncoder.encode(listOfFiles[i].getName(), "UTF-8") + "\">"
								+ listOfFiles[i].getName() + "</a></td></tr>";
					} 
				}
				for (int i = 0; i < listOfFiles.length; i++) {	
					if (listOfFiles[i].isDirectory()) {
						response = response + "<tr><td><input name="+i+" type=checkbox value=\""+ listOfFiles[i].getName() +"\">Directory</td> <td><a href=\"folder_seek/" + URLEncoder.encode(listOfFiles[i].getName(), "UTF-8") + "\">"
								+ listOfFiles[i].getName() + "</a></td></tr>";
					}
				}
				response = response + "<input type=submit value=\"Delete\" form=\"delete_items\" title=\"Delete the files/folders you have selected\"></form></table></body> </html>";

				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.flush();
				os.close();

			}
			//printResponseHeaders(t);
		}
	}

	/*
	 * This will be used when users change folders
	 */
	static class getFolder implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			//printRequestHeaders(t);
			//System.out.println("getFolder handler");
			String folder = "";
			if (t.getRequestHeaders().getFirst("Referer")!=null)
			{
				folder =  t.getRequestURI().toString();
				if (folder.contains("folder_seek"))
				{
					folder = folder.substring(folder.indexOf("folder_seek"), folder.length());
					folder = folder.replace("folder_seek/", "");
					folder = formatString(folder);
					//Error Checking //TODO check over this again.
					String failDirectory = currentDirectory; //Backup if folder is unreadable
					currentDirectory = currentDirectory + folder + "/";
					try{
						File testf = new File(currentDirectory);
						if (!testf.exists()){
							currentDirectory = failDirectory;
						}
					}catch(Exception e){
						System.out.println(e);
						currentDirectory = failDirectory;
					}

				}
			}
			String response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><title>Prism</title><link rel=icon type=\"image/png\" href=\"http://www.hannaconsultantgroup.com/prism_icon.ico\"></head><body></body> </html>";

			//System.out.println(currentDirectory);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

			//printResponseHeaders(t);
		}
	}

	//Go up a level in the file system
	static class getFolderUp implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			//printRequestHeaders(t);
			//System.out.println("getFolderUp handler");
			File newFolder = new File(currentDirectory);
			currentDirectory = newFolder.getParentFile().getAbsolutePath().replace("\\", "/")+"/";

			//System.out.println(newFolderPath);

			String response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><title>Prism</title><link rel=icon type=\"image/png\" href=\"http://www.hannaconsultantgroup.com/prism_icon.ico\"></head><body></body> </html>";

			//System.out.println(currentDirectory);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

			//printResponseHeaders(t);
		}
	}

	static class getMakeDir implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			//printRequestHeaders(t);
			//System.out.println("getMakeDir handler");
			int contentLength = Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"));
			byte[] badData = new byte[contentLength]; 
			String userAgent = "";
			if (t.getRequestHeaders().getFirst("User-agent")!=null)
				userAgent = t.getRequestHeaders().getFirst("User-agent");
			if (userAgent.contains("Edge"))
				userAgent = "Edge";
			else if (userAgent.contains("Trident"))
				userAgent = "IE";
			else if (userAgent.contains("Firefox"))
				userAgent = "Firefox";
			else if (userAgent.contains("Chrome"))
				userAgent = "Chrome browser";

			BufferedInputStream bis =  new BufferedInputStream(t.getRequestBody());
			BufferedOutputStream bos = new BufferedOutputStream(t.getResponseBody());
			t.setStreams(bis, bos); 

			//System.out.println("Stream set");
			int count = 0;
			while (count < contentLength)
			{
				badData[count] = (byte)bis.read();
				count ++;
			}
			bis.close();
			//System.out.println("Raw header data gathered");
			String rawHeaderString = new String(badData);

			//System.out.println(rawHeaderString);
			rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf("Content-Disposition: form-data;"),rawHeaderString.length());
			rawHeaderString = rawHeaderString.replace("Content-Disposition: form-data; name=\"","");
			rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf("\n")+2,rawHeaderString.length());
			rawHeaderString = rawHeaderString.substring(0,rawHeaderString.indexOf("--")).trim();

			//System.out.println(rawHeaderString);
			File newDirectory = new File (currentDirectory + rawHeaderString);
			newDirectory.mkdir();
			String response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><title>Prism</title><link rel=icon type=\"image/png\" href=\"http://www.hannaconsultantgroup.com/prism_icon.ico\"></head><body></body> </html>";

			//System.out.println(currentDirectory);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

			//printResponseHeaders(t);
		}
	}

	static class getDelete implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			//printRequestHeaders(t);
			//	System.out.println("getDelete handler");
			int contentLength = Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"));
			byte[] badData = new byte[contentLength]; 
			BufferedInputStream bis =  new BufferedInputStream(t.getRequestBody());
			BufferedOutputStream bos = new BufferedOutputStream(t.getResponseBody());
			t.setStreams(bis, bos); 

			//System.out.println("Stream set");
			int count = 0;
			while (count < contentLength)
			{
				badData[count] = (byte)bis.read();
				count ++;
			}
			bis.close();
			//	System.out.println("Raw header data gathered");
			String rawHeaderString = new String(badData);
			int numberOfDeletedFiles = numberOfOccurrences(rawHeaderString,"name=");
			//	System.out.println("Number of deleted files: " + numberOfDeletedFiles);
			String[] fileNames = new String[numberOfDeletedFiles];
			rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf("Content-Disposition: form-data;"),rawHeaderString.length());
			rawHeaderString = rawHeaderString.replace("Content-Disposition: form-data; name=\"","");
			rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf("\n")+2,rawHeaderString.length());

			//System.out.println(rawHeaderString);
			for (int x = 0; x<numberOfDeletedFiles; x++)
			{
				fileNames[x] = rawHeaderString.substring(1, rawHeaderString.indexOf("--")).trim();

				rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf(fileNames[x]), rawHeaderString.length());

				rawHeaderString = rawHeaderString.replace(fileNames[x], "");

				if (rawHeaderString.contains("\""))
					rawHeaderString = rawHeaderString.substring(rawHeaderString.indexOf("\""), rawHeaderString.length());

				//System.out.println("Deleting file "+x+": "+fileNames[x]);
				File fileToDelete = new File (currentDirectory+fileNames[x]);
				fileToDelete.delete();
			}
			//rawHeaderString = rawHeaderString.substring(0,rawHeaderString.indexOf("--"));
			String response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"></head><body></body> </html>";

			//System.out.println(currentDirectory);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

			//printResponseHeaders(t);
		}
	}

	/*
	 * This will be used when the server sends files to clients
	 */
	static class getFile implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String file = t.getRequestURI().getPath();
			file = file.replace("/file_seek/", "");
			file = file.replace("+", " ");
			file = formatString(file);
			//System.out.println(file);

			File testf = new File(currentDirectory + file);
			if (testf.exists()){
				//first, if we want to transfer a file, make the file into a byte array:
				File transferFile = new File(currentDirectory + file);
				FileInputStream fin = new FileInputStream(transferFile); 
				t.sendResponseHeaders(200, transferFile.length());
				OutputStream os = t.getResponseBody();
				try{
					int numberOfBytes=0;
					byte buffer[] = new byte[(int)Math.min(1024*1024, transferFile.length()-numberOfBytes)];
					while ((numberOfBytes=fin.read(buffer)) != -1){
						os.write(buffer);
					}
				}catch(Exception e){
					System.out.println(e);
					//TODO do something with the exception.
				}finally{
					fin.close();
				}

				os.close();
				//printResponseHeaders(t);			
			} else {
				//This just changes folders to the current directory as a failsafe;
				String response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"></head><body></body> </html>";

				//		System.out.println(currentDirectory);
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.flush();
				os.close();

				//printResponseHeaders(t);
			}

		}
	}

	/* This will be used when the server receives files from users */
	static class getUpload implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {


			String response;
			String userAgent = "";
			if (t.getRequestHeaders().getFirst("User-agent")!=null)
				userAgent = t.getRequestHeaders().getFirst("User-agent");
			if (userAgent.contains("Edge"))
				userAgent = "Edge";
			else if (userAgent.contains("Trident"))
				userAgent = "IE";
			else if (userAgent.contains("Firefox"))
				userAgent = "Firefox";
			else
				userAgent = "Chrome browser";
			//DO POST request
			if (t.getRequestHeaders().getFirst("Content-length")!=null && Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"))>1)
			{

				//printRequestHeaders(t);

				/* If this file is still in process of being transfered, and needs more exchanges to complete
				 	(Usually in the case of a failed transfer)   											*/
				boolean overwrite;
				if (lastReq.equals(t.getRequestHeaders().getFirst("Content-type"))){
					//	System.out.println("Receiving additional file data");
					lastReq = t.getRequestHeaders().getFirst("Content-type");
					//	System.out.println("getUpload POST handler");
					overwrite = false;
				}else{
					lastReq = t.getRequestHeaders().getFirst("Content-type");
					//	System.out.println("getUpload POST handler");
					overwrite = true;
				}

				int contentLength = Integer.parseInt(t.getRequestHeaders().getFirst("Content-length"));
				byte[] firstData = new byte[300]; //To contain the initial header
				BufferedInputStream bis =  new BufferedInputStream(t.getRequestBody(),(1024*1024));
				BufferedOutputStream bos = new BufferedOutputStream(t.getResponseBody(),(1024*1024));
				t.setStreams(bis, bos);

				//System.out.println("Stream set");
				int count = 0;
				int i = 0;
				while (i < 3){ // End after 3 '\n'
					firstData[count] = (byte)bis.read();
					if ((char)firstData[count] == '\n')
						i++;
					count++;
				}
				firstData[count] = (byte)bis.read();
				count++;
				firstData[count] = (byte)bis.read();
				count++;

				//Assigning the filename
				String firstHeaderString = new String(firstData);
				//System.out.println(firstHeaderString);
				String fileName = firstHeaderString;
				fileName = fileName.substring(fileName.indexOf("filename="), fileName.length());
				fileName = fileName.replace("filename=\"", "");
				fileName = fileName.substring(0,fileName.indexOf("\""));


				int buffer = (1024*1024);
				int c = count;
				i = 0;
				try{
					File newFile = new File(currentDirectory +fileName);
					if (overwrite){
						copyFile(new byte[0],newFile); //Overwrites the file if it exists.
					}
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(newFile,true),buffer);
					//FileOutputStream fos = new FileOutputStream(newFile,true);
					//If the file is at least 1 megabyte (size of buffer) then
					//write to the stream in 1 megabyte chunks (1024*1024)
					if (contentLength-c-300 > buffer){
						byte[] mainData = new byte[buffer];
						try {										
							for(int k = 0; k<((contentLength-c-300)/buffer); k++){
								while(i < buffer){
									mainData[i] = (byte)bis.read();
									i++;
									count++;
								}								
								fos.write(mainData);
								i = 0;
							}							
						}catch(Exception e) {
							System.out.println(e);
						}
					}
					//Byte Array for the remainder of data
					byte[] lastData = new byte[contentLength - count];
					c = count;
					i = 0;
					while (count < contentLength)
					{
						lastData[i] = (byte)bis.read();
						count ++;
						i++;
					}
					bis.close();
					//System.out.println("Raw header data gathered");
					String lastHeaderString = new String(lastData);
					//System.out.println(lastHeaderString);
					int startAt = 0;
					if (userAgent.equals("Edge") || userAgent.equals("IE"))
					{
						startAt = lastHeaderString.indexOf("-----------------------------");
					}
					if (userAgent.equals("Firefox"))
					{
						startAt = lastHeaderString.indexOf("-----------------------------");
					}
					if (userAgent.equals("Chrome browser"))
					{
						startAt = lastHeaderString.indexOf("------WebKitFormBoundary");
					}
					lastHeaderString = lastHeaderString.substring(startAt,lastHeaderString.lastIndexOf("--"));
					//We now have a string with the ending headers

					//System.out.println("fileName: "+ fileName);
					String fileSizeString = lastHeaderString;
					fileSizeString = fileSizeString.substring(fileSizeString.indexOf("name=\"filesize\""), fileSizeString.lastIndexOf("--"));
					fileSizeString = fileSizeString.replace("name=\"filesize\"", "");
					fileSizeString = fileSizeString.substring(0,fileSizeString.indexOf("-"));
					fileSizeString = fileSizeString.trim();
					//	System.out.println("fileSize: " + fileSizeString);			

					fileSize = Integer.parseInt(fileSizeString);
					/*
					 * We now have a the file's name and size according to the headers sent
					 */

					//		System.out.println(currentDirectory + fileName);

					fos.write(lastData,0,startAt-2);
					fos.flush();
					fos.close();
				}catch(Exception e){
					System.out.println(e);
				}

				response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"></head><body></body> </html>";



			}
			//File is empty, no post request
			else
			{
				//printRequestHeaders(t);
				//	System.out.println("getUpload GET Handler");
				response = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\" /><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"></head><body></body> </html>";
			}
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();
			//printResponseHeaders(t);
			t.close();
		}
	}

	/*
	 * Used only for printing Request Headers
	 */
	public static void printRequestHeaders(HttpExchange t){
		System.out.println("---- NEW REQUEST ----");
		System.out.println("Request Method : " + t.getRequestMethod());
		System.out.println("Origin : "+ t.getRequestHeaders().getFirst("Origin"));
		System.out.println("Content-transfer-encoding : "+ t.getRequestHeaders().getFirst("Content-transfer-encoding"));
		System.out.println("Content-disposition : "+ t.getRequestHeaders().getFirst("Content-disposition"));
		System.out.println("Accept-encoding : "+ t.getRequestHeaders().getFirst("Accept-encoding"));
		System.out.println("Accept : "+ t.getRequestHeaders().getFirst("Accept"));
		System.out.println("Connection : "+ t.getRequestHeaders().getFirst("Connection"));
		System.out.println("Referer : "+ t.getRequestHeaders().getFirst("Referer"));
		System.out.println("Host : "+ t.getRequestHeaders().getFirst("Host"));
		System.out.println("User-agent : "+ t.getRequestHeaders().getFirst("User-agent"));
		System.out.println("Content-length : "+ t.getRequestHeaders().getFirst("Content-length"));
		System.out.println("Content-type : "+ t.getRequestHeaders().getFirst("Content-type"));;
		System.out.println("Content : "+ t.getRequestHeaders().getFirst("Content"));
	}

	/*
	 * Used only for printing Response Headers in the console
	 */
	public static void printResponseHeaders(HttpExchange t){
		System.out.println("---- NEW RESPONSE ----");
		System.out.println("Origin : "+ t.getResponseHeaders().getFirst("Origin"));
		System.out.println("Content-transfer-encoding : "+ t.getResponseHeaders().getFirst("Content-transfer-encoding"));
		System.out.println("Content-disposition : "+ t.getResponseHeaders().getFirst("Content-disposition"));
		System.out.println("Accept-encoding : "+ t.getResponseHeaders().getFirst("Accept-encoding"));
		System.out.println("Accept : "+ t.getResponseHeaders().getFirst("Accept"));
		System.out.println("Connection : "+ t.getResponseHeaders().getFirst("Connection"));
		System.out.println("Referer : "+ t.getResponseHeaders().getFirst("Referer"));
		System.out.println("Host : "+ t.getResponseHeaders().getFirst("Host"));
		System.out.println("User-agent : "+ t.getResponseHeaders().getFirst("User-agent"));
		System.out.println("Content-length : "+ t.getResponseHeaders().getFirst("Content-length"));
		System.out.println("Content-type : "+ t.getResponseHeaders().getFirst("Content-type"));
		System.out.println("Content : "+ t.getResponseHeaders().getFirst("Content"));

	}

}

