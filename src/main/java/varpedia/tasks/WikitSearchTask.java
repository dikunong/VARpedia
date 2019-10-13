package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Background task that handles searching for a given term on Wikipedia via wikit.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
 */
public class WikitSearchTask extends Task<Boolean> {

    private String _searchTerm;

    public WikitSearchTask(String term) {
        _searchTerm = term;
    }

    /**
     * @return true if the wikit search is completed successfully
     */
    @Override
    protected Boolean call() throws Exception {
        // lookup search term on Wikipedia
        Command wikit = new Command("wikit", _searchTerm);
        wikit.run();
        
        // Wait for 10 seconds. If wikit doesn't return after that, it's probably stuck in a disambiguation.
        try {
        	if (!wikit.getProcess().waitFor(10, TimeUnit.SECONDS)) {
        		wikit.endForcibly();
            	throw new TimeoutException("Wikit timed out, disambiguation?");
        	}
        } catch (InterruptedException e) {
        	wikit.endForcibly();
        	return Boolean.TRUE;
        }
        
        String searchOutput = wikit.getOutput();

        // report failure if the search term is not found on Wikipedia
        if (searchOutput.contains(" not found :^(") || searchOutput.equals("")) {
            return Boolean.FALSE;
        }

        // remove the two spaces Wikit loves to dump at the start of its output
        if (searchOutput.startsWith("  ")) {
            searchOutput = searchOutput.substring(2);
        }

        // save search output to text file to be used in a later controller
        try {
            File file = new File("appfiles/search-output.txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writer.write(searchOutput);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
