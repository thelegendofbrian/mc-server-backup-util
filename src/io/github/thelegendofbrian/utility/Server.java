package io.github.thelegendofbrian.utility;

import java.io.File;

public class Server {
	
	private File folderFile;
	
	public Server(File folderFile) {
		this.folderFile = folderFile;
	}
	
	public File getFolderFile() {
		return folderFile;
	}
	
	public String getFolderName() {
		return folderFile.getName();
	}
	
	public long getLastModified() {
		return folderFile.lastModified();
	}
	
}
