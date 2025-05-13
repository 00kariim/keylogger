package ge.edu.sangu;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public class ProcessHider {
    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        boolean FreeConsole();
    }

    public static void hideConsoleWindow() {
        try {
            Kernel32.INSTANCE.FreeConsole();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Console hiding failed: " + e.getMessage());
        }
    }
}
