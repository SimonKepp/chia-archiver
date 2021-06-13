package tech.kepp.chia.archiver;

import java.io.*;
import java.util.*;

public class Configuration {
	private static final String DEFALT_CONFIG_FILE = "chia-archiver.properties";
	private File config;
	private Properties properties = new Properties();
	private List<File> stagingFolders = new ArrayList<File>(10);
	private List<File> farmingFolders = new ArrayList<File>(10);
	private String pathDelimiter = ":";

	public Configuration(String configFile) throws IOException {
		config = new File(configFile);
		readConfigFile();

	}

	public Configuration() {
		File configDir = new File(System.getProperty("user.home"));
		config = new File(configDir, DEFALT_CONFIG_FILE);

	}

	private void readConfigFile() throws IOException {
		InputStream in = new FileInputStream(config);

		properties.load(in);
		String staging = properties.getProperty("staging-dirs");
		StringTokenizer sts = new StringTokenizer(staging, pathDelimiter);
		while (sts.hasMoreTokens()) {
			String path = sts.nextToken();
			stagingFolders.add(new File(path));
		}
		String farming = properties.getProperty("farming-dirs");
		StringTokenizer stf = new StringTokenizer(farming, pathDelimiter);
		while (stf.hasMoreTokens()) {
			String path = stf.nextToken();
			farmingFolders.add(new File(path));
		}

	}

	public List<File> getStagingFolders() {
		return stagingFolders;
	}

	public List<File> getFarmingFolders() {
		return farmingFolders;
	}

	public int getThreads() {
		String t = properties.getProperty("threads", "8");
		return Integer.valueOf(t);
	}

}
