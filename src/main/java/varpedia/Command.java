package varpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {

    private String[] _cmd;

    public Command(String... cmd) {
        _cmd = cmd;
    }

    public Process run() {
        try {
            ProcessBuilder pb = new ProcessBuilder(_cmd);
            Process process = pb.start();
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOutput(Process process) {
        String output = null;
        try {
            InputStream out = process.getInputStream();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(out));
            String line = null;
            while ((line = stdout.readLine()) != null ) {
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
