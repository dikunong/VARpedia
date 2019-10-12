package varpedia;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class FFMPEGCommand {
	private static final String[] ARGS = new String[] {"ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-"};
	private static final String[] ARGS_OLD = new String[] {"ffmpeg", "-y", "-f", "concat", "-i", "-"};
	private static final String[] ARGS_LINUX = new String[] {"ffmpeg", "-y", "-f", "image2pipe", "-framerate", "", "-i", "-"};
	private static final String[] ARGS_IMAGE = new String[] {"ffmpeg", "-y", "-f", "lavfi", "-t", "", "-i", "color=color=white:size=960x540"};
	
	private static String[] generateArguments(String[] base, String[] extra, String output) {
		String[] args = new String[base.length + extra.length + 1];
		System.arraycopy(base, 0, args, 0, base.length);
		System.arraycopy(extra, 0, args, base.length, extra.length);
		args[args.length - 1] = output;
		return args;
	}
	
	private Command _command;
	private Command _commandOld;
	private Command _currentCommand;
	
	private String[] _files;
	private float _durationPerFile;
	private boolean _pipeMethod;
	private boolean _altMethod;
	
	//This implements a commandline ffmpeg -y -f concat (-protocol_whitelist file,pipe) -i - [extra args] <output>
	public FFMPEGCommand(String[] files, float durationPerFile, boolean pipeMethod, String output, String... extraArgs) throws Exception {
		_files = files;
		_durationPerFile = durationPerFile;
		_pipeMethod = pipeMethod;
		
		if (pipeMethod) {
			String[] args = generateArguments(ARGS_LINUX, extraArgs, output);
			args[5] = "1/" + Float.toString(durationPerFile);
			_command = new Command(args);
		} else {
			_command = new Command(generateArguments(ARGS, extraArgs, output));
			_commandOld = new Command(generateArguments(ARGS_OLD, extraArgs, output));
		}
	
		_currentCommand = _command;
		_currentCommand.run();
	}
	
	//This implements a commandline ffmpeg -y -f lavfi -t <duration> -i - [extra args] <output>
	public FFMPEGCommand(float duration, String output, String... extraArgs) {
		_altMethod = true;
		
		String[] args = generateArguments(ARGS_IMAGE, extraArgs, output);
		args[5] = Float.toString(duration);
		_command = new Command(args);
		
		_currentCommand = _command;
		_currentCommand.run();
	}
	
	private boolean pipeConcat(String name, float duration, Callable<Boolean> isCancelled) throws Exception {
		if (isCancelled.call()) {
			_currentCommand.end();
			return false;
		}
		
		_currentCommand.writeString("file '" + name.replace("'", "\\'") + "'");
		
		if (duration >= 0) {
			_currentCommand.writeString("duration " + duration);
		}
		
		return true;
	}
	
	private boolean pipeFile(String name, Callable<Boolean> isCancelled) throws Exception {
		try (InputStream in = new FileInputStream(name)) {
			byte[] transfer = new byte[4096];
			int count;
			
			while ((count = in.read(transfer)) != -1) {
				if (isCancelled.call()) {
					_currentCommand.end();
					return false;
				}
				
				_currentCommand.getProcess().getOutputStream().write(transfer, 0, count);
			}
		}
		
		return true;
	}

	public boolean pipeFilesIn(Callable<Boolean> isCancelled) throws Exception {
		if (_altMethod) {
			return true;
		}
		
		if (_pipeMethod) {
			for (String s : _files) {
				if (!pipeFile(s, isCancelled)) {
					return false;
				}
			}
		} else {
			for (String s : _files) {
				if (!pipeConcat(s, _durationPerFile, isCancelled)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Waits for the command to complete
	 * @throws Exception
	 */
	public void waitFor() throws Exception {
		_currentCommand.getProcess().getOutputStream().close();
		new Thread(() -> {_currentCommand.getError();}).start(); //FFmpeg needs its stderr to be emptied

		//Wait for it to be done
		try {
			if (_currentCommand.getProcess().waitFor() != 0) {
				throw new Exception("Failed to merge video" + _currentCommand.getProcess().exitValue());
			}
		} catch (InterruptedException e) {
			_currentCommand.end();
		}
	}
	
	/**
	 * Set the command to be the old command
	 */
	public boolean useOldCommand() {
		_currentCommand = _commandOld;
		
		if (_currentCommand != null) {
			_currentCommand.run();
			return true;
		} else {
			return false;
		}
	}
}
