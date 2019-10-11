package varpedia;

public class Voice {
	private String _name;
	private String _display;
	
	public Voice(String name, String display) {
		_name = name;
		_display = display;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getDisplay() {
		return _display;
	}
	
	@Override
	public String toString() {
		return _display;
	}
}
