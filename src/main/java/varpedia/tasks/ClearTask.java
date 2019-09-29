package varpedia.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.concurrent.Task;

/**
 * Background task that handles deletion of temporary app files.
 *
 * Author: Tudor Zagreanu
 */
public class ClearTask extends Task<Void> {
	public static void deleteTree(File root) throws IOException {
		//Delete the files (basically everything, including the stuff in audio, but not the audio directory itself)
		Files.walkFileTree(root.toPath(), new FileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
				Files.delete(arg0);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
				throw new IOException("Unable to delete");
			}
		});
	}
	
	private File _file;
	
	/**
	 * @param file The root directory to clear
	 */
	public ClearTask(File file) {
		_file = file;
	}
	
	@Override
	protected Void call() throws Exception {
		deleteTree(_file);
		return null;
	}
}
