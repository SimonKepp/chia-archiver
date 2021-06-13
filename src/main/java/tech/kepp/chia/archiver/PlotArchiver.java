package tech.kepp.chia.archiver;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class PlotArchiver {
	private long bytesMoved =0;
	private long blocksMoved=0;
	private long filesMoved=0;
	private long startTime;
	private ExecutorService executor;
	private List<File> stagingDirs = new ArrayList<File>(10);
	private ArrayList<TargetFolderInfo> farmingDirs = new ArrayList<TargetFolderInfo>(10);
	FileMover fileMover = new FileMover();

	public PlotArchiver(Configuration config) {
		setStagingDirs(config.getStagingFolders());
		for(File target : config.getFarmingFolders()) {
			TargetFolderInfo info = new TargetFolderInfo(target);
			farmingDirs.add(info);
			int threads = config.getThreads()
					info("Using " + threads + "threads");
			executor= Executors.newFixedThreadPool(threads);
		}

	}

	public static void main(String[] args) {

		startTime = System.currentTimeMillis();
		Configuration config = new Configuration();
		PlotArchiver archiver = new PlotArchiver(config);
		archiver.run();
	}

	public void run() {
		info("scanning folders...");
		for (File source : getStagingDirs()) {
			ArchiveWorker worker = new ArchiveWorker(this, source);
			submitTask(worker);
			
		}
		executor.awaitTermination();
	}
	}
	public List<File> getStagingDirs() {
		return stagingDirs;
	}

	public void setStagingDirs(List<File> stagingDirs) {
		this.stagingDirs = stagingDirs;
	}

	public List<File> getFarmingDirs() {
		return farmingDirs;
	}

	public void setFarmingDirs(List<File> farmingDirs) {
		this.farmingDirs = farmingDirs;
	}

	@Override
	public void run() {
		info("scanning folders...");
		for (File source : getStagingDirs()) {
			archive(source);
		}

	}

	private void archive(File source) {
		debug("archiving [" + source.getPath() + "]");
		if (source.isDirectory()) {
			// recursively process each file in folder
			for (File child : source.listFiles()) {
				archive(child);
			}
		} else {
			// this is an ordinary file
			if (isPlot(source)) {
				info("archiving plot [" + source.getPath() + "]");
				TargetFolderInfo targetFolder = chooseTargetFolder();
				moveFile(source, targetFolder);
			} else {
				debug("skipping file [" + source.getPath() + "]");
			}
		}
	}
		
	}
	/**
	 * Standard implementation of choosing a targetFolder for a given plot.Balances available space versus number of jobs in progress for a given target.
	 * @return
	 */
		public TargetFolderInfo chooseTargetFolder() {
			TargetFolderInfo tfi = Collections.max(getFarmingDirs());
		}

		/**
		 * Alternative implementation of choosing a targetFolder for a given plot.Ignores space and number of jobs in progress for a given target. intead chooses a random target
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
		System.out.println("INFO: " + msg);

	}

	public void submitTask(ArchiveWorker childWorker) {
		executor.execute(childWorker);
	}
	
	public void logBlockMoved(int size);
	blocksMoved++;
	bytesMoved+=size;
	}
	public void logFileMoved() {
		filesMoved++;
		info("Moved "+ filesMoved + " files of "+ blocksMoved + " blocks and " +bytesMoved + " bytes" in getDuration()+ "seconds" );

}
