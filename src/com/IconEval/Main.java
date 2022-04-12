package com.IconEval;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String filepath = "icons/";

        File f = new File(filepath);

        // This filter will only include pngs and jpegs
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        };

        String[] fileNames = f.list(filter);
        try{
            int numIcons = fileNames.length;
            ImageStats[] imgStats = new ImageStats[numIcons];
            for (int i = 0; i < numIcons; i++) {
                System.out.println(fileNames[i]);
                imgStats[i] = new ImageStats(Imgcodecs.imread(filepath + fileNames[i]), fileNames[i]);
            }
            for (int i = 0; i < numIcons; i++) {
                for (int j = i + 1; j < numIcons; j++) {
                    new ImageCompare(imgStats[i], imgStats[j]);
                }
            }
        }
        catch(Exception e){
            System.out.println("The icons directory is empty or inaccessible.");
        }
        ///*
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
        //*/
    }
}