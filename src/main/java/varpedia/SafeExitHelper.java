package varpedia;

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

    public boolean isSafeToExit() {
        return _safeToExit;
    }

    public void setSafeToExit(boolean status) {
    	_safeToExit = status;
    }
}
