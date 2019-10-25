package varpedia;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Wraps the FFMPEG command for creating with a set of images.
 *
 * @author Tudor Zagreanu
 */
public class FFMPEGCommand {
	private static final String[] ARGS = new String[] {"ffmpeg", "-y", "-f", "concat", "-protocol_whitelist", "file,pipe", "-i", "-"};
	private static final String[] ARGS_OLD = new String[] {"ffmpeg", "-y", "-f", "concat", "-i", "-"};
	private static final String[] ARGS_LINUX = new String[] {"ffmpeg", "-y", "-f", "image2pipe", "-framerate", "", "-i", "-"};
	private static final String[] ARGS_IMAGE = new String[] {"ffmpeg", "-y", "-f", "lavfi", "-t", "", "-i", "color=color=white:size=960x540"};
	
	private static String[] generateArguments(String[] base, String[] extra, String output, int suffixArgs) {
		String[] args = new String[base.length + extra.length + suffixArgs];
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
	
	/**
	 * This implements the "ffmpeg -y -f concat (-protocol_whitelist file,pipe) -i - [extra args] &ltoutput&gt" command,
	 * or "ffmpeg -y -f image2pipe -framerate (durationPerFile) -i - [extra args] -max_muxing_queue_size (frames) &ltoutput&gt"
	 * @param files The list of filenames to pipe in
	 * @param durationPerFile The duration per file, or -1 if there is no duration per file
	 * @param pipeMethod The method. true for image2pipe (1-2 images), false for concat (>2 images)
	 * @param output The output filename
	 * @param extraArgs Any extra arguments (e.g. drawtext filters)
	 */
	public FFMPEGCommand(String[] files, float durationPerFile, boolean pipeMethod, String output, String... extraArgs) throws Exception {
		_files = files;
		_durationPerFile = durationPerFile;
		_pipeMethod = pipeMethod;
		
		if (pipeMethod) {
			String[] args = generateArguments(ARGS_LINUX, extraArgs, output, 3);
			args[5] = "1/" + Float.toString(durationPerFile);
			args[args.length - 3] = "-max_muxing_queue_size";
			args[args.length - 2] = Integer.toString((int)(25 * durationPerFile * files.length + 1));
			_command = new Command(args);
		} else {
			_command = new Command(generateArguments(ARGS, extraArgs, output, 1));
			_commandOld = new Command(generateArguments(ARGS_OLD, extraArgs, output, 1));
		}
	
		_currentCommand = _command;
		_currentCommand.run();
	}
	
	/**
	 * This implements the "ffmpeg -y -f lavfi -t &ltduration&gt -i - [extra args] &ltoutput&gt"
	 * @param duration The duration of the whole video
	 * @param output The output filename
	 * @param extraArgs Any extra arguments (e.g. drawtext filters)
	 */
	public FFMPEGCommand(float duration, String output, String... extraArgs) {
		_altMethod = true;
		
		String[] args = generateArguments(ARGS_IMAGE, extraArgs, output, 1);
		args[5] = Float.toString(duration);
		_command = new Command(args);
		
		_currentCommand = _command;
		_currentCommand.run();
	}
	
	//Pipe in a file using concat (pipe file '<filename>', optionally followed by duration <duration>
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
	
	//Pipe in a file using image2pipe (copy from file to pipe)
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

	/**
	 * Pipe in the images using the pipe method.
	 * @param isCancelled Returns true if the loop should terminate early. pipeFilesIn returns false in this case.
	 * @return true if the loop was not cancelled.
	 */
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
	 * @return true if there is an old command
	 */
	public boolean useOldCommand() {
		_currentCommand = _commandOld;
		
		//Run the old command if it actually exists
		if (_currentCommand != null) {
			_currentCommand.run();
			return true;
		} else {
			return false;
		}
	}
}
