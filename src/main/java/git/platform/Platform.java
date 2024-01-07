package git.platform;

public class Platform {
    public static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static boolean isWindows() {
        return isWindows;
    }
}
