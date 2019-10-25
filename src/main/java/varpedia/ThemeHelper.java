package varpedia;

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

    public boolean getDarkModeStatus() {
        return _darkModeEnabled;
    }

    public void setDarkModeStatus(boolean status) {
        _darkModeEnabled = status;
    }
}
