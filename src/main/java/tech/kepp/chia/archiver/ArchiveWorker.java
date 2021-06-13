package tech.kepp.chia.archiver;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.*;


public class ArchiveWorker implements Runnable {
	private Log log = LogFactory.getLog(getClass());
	private PlotArchiver coordinator;
	
	private File source;
//	private static final int buffer_size = 64 *1024;
	
	private byte[] buffer = new byte[64 *1024];

	public ArchiveWorker(PlotArchiver coordinator, File source ) {
		setCoordinator(coordinator);
		setSource(source);
	}


	private boolean isPlot(File file) {
		String name = file.getName();
		return name.endsWith(".plot");
		
	}

	@Override
	public void run() {
		debug("archiving [" + source.getPath() + "]");
		if (source.isDirectory()) {
			// recursively process each file in folder
			for (File child : source.listFiles()) {
				ArchiveWorker  childWorker =new ArchiveWorker(coordinator, child);
				coordinator.submitTask(childWorker);
			}
		} else {
			// this is an ordinary file
			if (isPlot(source)) {
				info("archiving plot [" + source.getPath() + "]");
				TargetFolderInfo targetFolder = coordinator.chooseTargetFolderRandom();
				try {
					moveFile(source, targetFolder);
				} catch (IOException e) {
					log.error("Failed to move File ["+ source + "] to ["+ targetFolder.getFolder().getPath() + "]", e);
				}
			} else {
				log.info("skipping file [" + source.getPath() + "]");
			}
		}

	}

	private void moveFile(File src, TargetFolderInfo targetFolder) throws IOException {
		targetFolder.addJobInProgress();
		File target = new File(targetFolder.getFolder(), src.getName());
		info("Moving file [" + src.getPath() + "] to [" + target.getPath());
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(target);
		for ( int len=in.read(buffer); len > 0;len=in.read(buffer)) {
			out.write(buffer, 0, len);
			info(".");
		}
		out.close();
		in.close();
		info("File copied successfully");
		targetFolder.markJobCompleted();
		if (src.delete()) {
			info("Deleted original file["+ src.getPath()+ "]");
		} else {
			warn("Failed to delete original file["+ src.getPath()+ "]");
			src.deleteOnExit();
		}
		
	}
	


	/**
	 * @return the coordinator
	 */
	public PlotArchiver getCoordinator() {
		return coordinator;
	}

	/**
	 * @param coordinator the coordinator to set
	 */
	public void setCoordinator(PlotArchiver coordinator) {
		this.coordinator = coordinator;
	}

	/**
	 * @return the source
	 */
	public File getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(File source) {
		this.source = source;
	}

	private void debug(String msg) {
		log.debug(msg);
	}

	private void info(String msg) {
		log.info(msg);

	}

	private void warn(String msg) {
		log.warn(msg);

	}
	private void error(String msg) {
		log.error(msg);
	
	}


}
