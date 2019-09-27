package varpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Command {

    private String[] _cmd;
    private Process _process;

    public Command(String... cmd) {
        _cmd = cmd;
    }

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
    
    public String getOutput() {
        String output = "";
        try {
            InputStream out = _process.getInputStream();
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
    
    public String getError() {
        String output = "";
        try {
            InputStream out = _process.getErrorStream();
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
    
    public void writeString(String str) {
    	if (!str.endsWith("\n")) {
    		str = str + '\n';
    	}
    	
    	try {
			_process.getOutputStream().write(str.getBytes());
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
}
