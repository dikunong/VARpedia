package varpedia.helpers;

/**
 * Singleton class that stores the current theme state.
 *
 * @author Di Kun Ong
 */
public final class ThemeHelper {

    private static ThemeHelper _themeHelper;

    private boolean _darkModeEnabled;

    private ThemeHelper() {}

    public synchronized static ThemeHelper getInstance() {
        if (_themeHelper == null) {
            _themeHelper = new ThemeHelper();
        }
        return _themeHelper;
    }

    /**
     * @return true if dark mode is currently enabled
     */
    public boolean getDarkModeStatus() {
        return _darkModeEnabled;
    }

    /**
     * @param status set to true if dark mode has been enabled, or false for light mode
     */
    public void setDarkModeStatus(boolean status) {
        _darkModeEnabled = status;
    }
}
