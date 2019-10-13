package varpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Represents a Command given to the system's terminal (e.g. Bash on Linux).
 *
 * @author Di Kun Ong and Tudor Zagreanu
 */
public class Command {

    private String[] _cmd;
    private Process _process;

    public Command(String... cmd) {
        _cmd = cmd;
    }

    /**
     * Builds a process based off of the given command, then runs it.
     * @return the Process that was built
     */
    public Process run() {
        try {
            ProcessBuilder pb = new ProcessBuilder(_cmd);
            _process = pb.start();
            return _process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Process getProcess() {
    	return _process;
    }

    /**
     * Gets the output of the command via stdout and returns it in String format.
     * @return the output String from the command
     */
    public String getOutput() {
        String output = "";
        try {
            InputStream out = _process.getInputStream();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
            String line = null;
            while ((line = stdout.readLine()) != null ) {
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Gets any error from the command via stderr and returns it in String format.
     * @return the error String from the command
     */
    public String getError() {
        String output = "";
        try {
            InputStream out = _process.getErrorStream();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
            String line = null;
            while ((line = stdout.readLine()) != null ) {
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
    
    public void writeString(String str) {
    	if (!str.endsWith("\n")) {
    		str = str + '\n';
    	}
    	
    	try {
			_process.getOutputStream().write(str.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public void end() {
		_process.destroy();
		
		while (true) {
			try {
				_process.waitFor();
				break;
			} catch (InterruptedException e) {
				;
			}
		}
	}

    /**
     * Allows for the forced destruction of the running Process.
     * Used only for wikit if a timeout is needed - typically when searching for a disambiguation page, where it
     * will get stuck waiting for user "input" it can't take.
     */
    public void endForcibly() {
		_process.destroyForcibly();
	}
}
