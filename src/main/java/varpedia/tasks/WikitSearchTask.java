package varpedia.tasks;

import javafx.concurrent.Task;
import varpedia.Command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(searchOutput);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
