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

public class WikitSearchTask extends Task<Boolean> {

    private String _searchTerm;

    public WikitSearchTask(String term) {
        _searchTerm = term;
    }

    @Override
    protected Boolean call() throws Exception {
        // lookup search term on Wikipedia
        Command wikit = new Command("wikit", _searchTerm);
        wikit.run();
        
        //Wait for 10 seconds. If wikit doesn't return after that, it's probably stuck in a disambiguation. Not ideal but it works.
        try {
        	if (!wikit.getProcess().waitFor(10, TimeUnit.SECONDS)) {
        		wikit.endForcibly();
            	throw new TimeoutException("Wikit timed out, diambiguation?");
        	}
        } catch (InterruptedException e) {
        	wikit.endForcibly();
        	return Boolean.TRUE;
        }
        
        String searchOutput = wikit.getOutput();

        // do we need to handle disambiguation results???
        if (searchOutput.contains(_searchTerm + " not found :^(") || searchOutput.equals("")) {
            return Boolean.FALSE;
        }

        // janky method of removing those two spaces Wikit loves to dump at the start of its output
        if (searchOutput.startsWith("  ")) {
            searchOutput = searchOutput.substring(2);
        }

        // COPY PASTE COPY PASTE COPY PASTE
        // TODO: make this not copy-pasted from Controller.java
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
