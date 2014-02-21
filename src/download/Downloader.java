package download;

import gui.Gui;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentSkipListSet;

public class Downloader {
	BufferedReader inputFileReader = null;
	ConcurrentSkipListSet<String> downloadFileList = new ConcurrentSkipListSet<String>();
	LinkedList<Thread> threadList = new LinkedList<Thread>();
	String outputDir;

	Gui gui;

	public Downloader(String filePath, String outputDir, Gui graphicalInterface) throws IOException {
		this.outputDir = outputDir;
		this.gui = graphicalInterface;

		File outputDirFile = new File(outputDir);
		outputDirFile.mkdir();

		File inputFile = new File(filePath);

		if (inputFile.isDirectory()) {

			for (File f : inputFile.listFiles()) {
				if (f.isDirectory()) {
					continue;
				}

				try {
					System.out.println("Load from list file:" + f.getAbsolutePath());
					inputFileReader = new BufferedReader(new FileReader(f.getAbsolutePath()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					continue;
				}
				loadFileList();
			}
		} else {

			try {
				System.out.println("Load from list file:" + inputFile.getAbsolutePath());
				inputFileReader = new BufferedReader(new FileReader(inputFile.getAbsolutePath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			loadFileList();
		}

		if (inputFileReader != null && inputFileReader.ready()) {
			try {
				inputFileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void loadFileList() {
		System.out.println("Start load and check mp3 list...");

		while (true) {
			try {
				String fileLine = inputFileReader.readLine(); 

				if (fileLine == null) {
					return;
				}

				while (true) {
					int startLine = fileLine.indexOf("http://");
					int finishLine = fileLine.indexOf(".mp3");

					if (startLine == -1) {
						startLine = fileLine.indexOf("https://");
					}

					if (startLine != -1 && finishLine != -1) {
						finishLine += 4; 

						String parsedLink;
						try {
							parsedLink = fileLine.substring(startLine, finishLine);
						} catch (IndexOutOfBoundsException e) {
							break;
						}

						System.out.println("Parse link:" + parsedLink);

						int lastSlashIndex = parsedLink.lastIndexOf('/');
						File f = new File(outputDir + "\\" + parsedLink.substring(lastSlashIndex + 1));
						if(!f.exists()) { 
							downloadFileList.add(parsedLink);
						} else {	
							int urlFileSize = getFileSize(new URL(parsedLink));
							if (urlFileSize != -1) {
								if (urlFileSize != f.length()) {
									System.out.println("\nDetected bad file:" + parsedLink);
									downloadFileList.add(parsedLink);
								} 
							}
						}

						System.out.println("\nLoad file list. Size:" + downloadFileList.size());
						try {
							fileLine = fileLine.substring(finishLine);
						} catch (IndexOutOfBoundsException e) {
							break;
						}

					} else {
						break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				//return;
			}
		}
	}

	public void startDownload() {
		final int startFilesCount = downloadFileList.size();
		final int chainSize = startFilesCount / 100;

		for (int i = 0; i < 10; i++) {
			threadList.add( new Thread() {
				public void run() {
					while (!downloadFileList.isEmpty()) {
						String url = downloadFileList.pollFirst();
						System.out.println("Start download:" + url);
						download(url);
						int downloadedCount = startFilesCount - downloadFileList.size();
						gui.setBarTo(downloadedCount / chainSize);
						gui.drawBarString(downloadedCount + "/" + startFilesCount);
					}

					gui.downloadFinish();
				}
			} );
		}

		for (Thread t : threadList) {
			t.start();
		}
	}

	@SuppressWarnings("deprecation")
	public void stopDownload() {
		for (Thread t : threadList) {
			if (t != null && t.isAlive()) {
				t.destroy();
			}
		}
		threadList.clear();
	}

	public void download(String address, String localFileName) {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;

		try {
			URL url = new URL(address);
			conn = url.openConnection();
			in = conn.getInputStream();

			out = new BufferedOutputStream(new FileOutputStream(outputDir + "\\" + localFileName));

			byte[] buffer = new byte[1024];

			int numRead;
			long numWritten = 0;

			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}

			System.out.println(localFileName + "\t" + numWritten + " bytes.");
		} 
		catch (Exception exception) { 
			System.out.println("Connection error:" + exception.getMessage());
			//exception.printStackTrace();
		} 
		finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} 
			catch (IOException ioe) {
			}
		}
	}

	public void download(String address) {		
		int lastSlashIndex = address.lastIndexOf('/');
		if (lastSlashIndex >= 0 &&
				lastSlashIndex < address.length() - 1) {
			download(address, address.substring(lastSlashIndex + 1));
		} 
		else {
			System.err.println("Could not figure out local file name for "+address);
		}
	}

	private int getFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			conn.disconnect();
		}
	}
}
