package varpedia.helpers;

/**
 * Singleton class that stores whether it is safe to close VARpedia on this screen.
 *
 * @author PisuCat
 */
public final class SafeExitHelper {

    private static SafeExitHelper _safeExitHelper;

    private boolean _safeToExit;

    private SafeExitHelper() {}

    public synchronized static SafeExitHelper getInstance() {
        if (_safeExitHelper == null) {
        	_safeExitHelper = new SafeExitHelper();
        }
        return _safeExitHelper;
    }

    /**
     * @return true if no warning needs to be shown on exit.
     */
    public boolean isSafeToExit() {
        return _safeToExit;
    }

    /**
     * @param status Set to true if no warning needs to be shown on exit.
     */
    public void setSafeToExit(boolean status) {
    	_safeToExit = status;
    }
}
