package com.tfg.backend.global.utils;
import java.io.File;

public class DirectoryDeleter {

    // Method to delete a directory recursively
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectory(child)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete(); // Delete the empty directory or file
    }
}
