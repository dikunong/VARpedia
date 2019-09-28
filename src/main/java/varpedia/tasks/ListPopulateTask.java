package varpedia.tasks;

import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ListPopulateTask extends Task<List<String>> {
    private File root;

    public ListPopulateTask(File rootFile) {
        root = rootFile;
    }

    @Override
    protected List<String> call() throws Exception {
        DirectoryStream<Path> str = Files.newDirectoryStream(root.toPath());
        List<String> ret = new ArrayList<String>();

        for (Path p : str) {
            if (isCancelled()) {
                return null;
            }

            // remove file extension for clean display to the user
            String filename = p.getFileName().toString();
            if (filename.contains(".")) {
                filename = filename.substring(0, filename.lastIndexOf('.'));
            }

            ret.add(filename);
        }

        str.close();
        ret.sort(null);
        return ret;
    }
}
