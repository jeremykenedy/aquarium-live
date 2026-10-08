package com.jeremykenedy.aquariumlive;

import java.io.File;

/** File handling for the saved art, kept free of Android so it can be tested on a plain JVM. */
final class ArtCache {

    private ArtCache() {
    }

    /** Deletes every file in dir whose name does not start with keep, so art from an older install is not reused. */
    static void prune(File dir, String keep) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (!f.getName().startsWith(keep)) {
                discard(f);
            }
        }
    }

    /** Deletes a file, or failing that asks for it to go when the process ends. */
    static void discard(File f) {
        if (!f.delete()) {
            f.deleteOnExit();
        }
    }
}
