package tech.kepp.chia.archiver;

import java.io.File;

public class TargetFolderInfo implements Comparable<TargetFolderInfo> {
	private File folder;
	private int jobsInProgress = 0;

	public TargetFolderInfo(File folder) {
		setFolder(folder);
	}
	public long getAvailableSpace() {
		return folder.getFreeSpace();
	}
	/**
	 * Get a weighted score of the suitability of this target based on available space and jobs in progress. Hogher score means better target.
	 * 
	 */
	public long getScore() {
		return getAvailableSpace() - jobsInProgress * 1000000000000L;
	}
	
	
	public void addJobInProgress() {
		jobsInProgress++;
	}
	public void markJobCompleted() {
		jobsInProgress--;
		
		
	}
	@Override
	public int compareTo(TargetFolderInfo o) {
		Long myScore = getScore();
		Long otherScore = o.getScore();
		return myScore.compareTo(otherScore);
		
	}
	/**
	 * @return the folder
	 */
	public File getFolder() {
		return folder;
	}
	/**
	 * @param folder the folder to set
	 */
	public void setFolder(File folder) {
		this.folder = folder;
	}
	/**
	 * @return the jobsInProgress
	 */
	public int getJobsInProgress() {
		return jobsInProgress;
	}
	
}
