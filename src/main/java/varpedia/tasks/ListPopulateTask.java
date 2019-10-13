package varpedia.tasks;

import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Background task that handles listing of all files in a directory.
 *
 * Author: Di Kun Ong
 */
public class ListPopulateTask extends Task<List<String>> {
    private File root;
    private String ext;
    
    public ListPopulateTask(File rootFile, String extension) {
        root = rootFile;
        ext = extension;
    }

    @Override
    protected List<String> call() throws Exception {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root.toPath());
        List<String> fileList = new ArrayList<>();

        for (Path p : directoryStream) {
            if (isCancelled()) {
                return null;
            }

            // remove file extension for clean display to the user
            String filename = p.getFileName().toString();

            if (filename.endsWith(".dir")) {
            	continue;
            }
            
            if (filename.contains(".")) {
            	if (!filename.substring(filename.lastIndexOf('.')).equals(ext)) {
            		continue;
            	}
            	
            	filename = filename.substring(0, filename.lastIndexOf('.'));
            }

            fileList.add(filename);
        }

        directoryStream.close();
        fileList.sort(null);
        return fileList;
    }
}
