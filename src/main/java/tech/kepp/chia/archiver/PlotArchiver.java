package tech.kepp.chia.archiver;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlotArchiver {
	private long bytesMoved = 0;
	private long blocksMoved = 0;
	private long filesMoved = 0;
	private long startTime;
	private Configuration config;
	private ExecutorService executor;
	private List<File> stagingDirs = new ArrayList<File>(10);
	private ArrayList<TargetFolderInfo> farmingDirs = new ArrayList<TargetFolderInfo>(10);
	private static Log log = LogFactory.getLog(PlotArchiver.class);

	public PlotArchiver(Configuration config) {
		this.config = config;
		setStagingDirs(config.getStagingFolders());
		for(File target : config.getFarmingFolders()) {
			TargetFolderInfo info = new TargetFolderInfo(target);
			farmingDirs.add(info);
			int threads = config.getThreads();
					info("Using " + threads + "threads");
			executor= Executors.newFixedThreadPool(threads);
			String strategy = config.getTargetStrategy();
		}

	}

	public static void main(String[] args) {

		log.info("Reading configuration...");
		Configuration config = new Configuration();
		PlotArchiver archiver = new PlotArchiver(config);
		archiver.run();
	}

	public void run() {
		startTime = System.currentTimeMillis();
		info("scanning folders...");
		for (File source : getStagingDirs()) {
			ArchiveWorker worker = new ArchiveWorker(this, source);
			submitTask(worker);

		}

		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
			log.info("All jobs Completed");
		} catch (InterruptedException e) {
			log.error("threads interrupted", e);
		}
	}

	public List<File> getStagingDirs() {
		return stagingDirs;
	}

	public void setStagingDirs(List<File> stagingDirs) {
		this.stagingDirs = stagingDirs;
	}

	public ArrayList<TargetFolderInfo> getFarmingDirs() {
		return farmingDirs;
	}

	public void setFarmingDirs(ArrayList<TargetFolderInfo> farmingDirs) {
		this.farmingDirs = farmingDirs;
	}	
		 


	

	/**
	 * Standard implementation of choosing a targetFolder for a given plot.Balances
	 * available space versus number of jobs in progress for a given target.
	 * 
	 */
	public TargetFolderInfo chooseTargetFolderBalanced() {
		TargetFolderInfo tfi = Collections.max(getFarmingDirs());
		return tfi;
	}
	
	public TargetFolderInfo chooseTargetFolder() {
		
		String strategy = config.getTargetStrategy();
		
		if (strategy.equalsIgnoreCase(Configuration.STRATEGY_RANDOM)){
			return chooseTargetFolderRandom();
		} else {
			return chooseTargetFolderBalanced();
		}
	}

	/**
	 * Alternative implementation of choosing a targetFolder for a given
	 * plot.Ignores space and number of jobs in progress for a given target. intead
	 * chooses a random target
	 * 
	 * @return
	 */
	public TargetFolderInfo chooseTargetFolderRandom() {
		int size = farmingDirs.size();
		Random random = new Random();
		int chosenIndex = random.nextInt(size);
		TargetFolderInfo chosen = farmingDirs.get(chosenIndex);
		return chosen;
	}

	private void debug(String msg) {
		System.out.println("DEBUG: " + msg);
	}

	private boolean isPlot(File file) {
		String name = file.getName();
		return name.endsWith(".plot");
	}

	private void info(String msg) {
		log.info(msg);
	}

	public void submitTask(ArchiveWorker childWorker) {
		executor.execute(childWorker);
	}

	public void logBlockMoved(int size) {
		blocksMoved++;
		bytesMoved += size;
	}

	public void logFileMoved() {
		filesMoved++;
		String msg = "Moved ";
				msg +=filesMoved;
				msg += " files of ";
				msg +=blocksMoved;
				msg += " blocks and ";
				msg +=bytesMoved;
				msg +=" bytes";
				msg += " in ";
				msg += getDuration();
				msg += " seconds";
				info(msg );
	}

	private String getDuration() {
		long now = System.currentTimeMillis();
		long milisecs= now - startTime;
		double secs = milisecs / 1000.0;
		return NumberFormat.getInstance().format(secs);
	}
}
