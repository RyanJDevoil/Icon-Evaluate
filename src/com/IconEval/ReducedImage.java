package com.IconEval;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;

public class ReducedImage {
    int[][] reducedProfileShape;
    Scalar[][] reducedProfileColour;
    HashMap<String, Double> foregroundColourBreakdown;

    public ReducedImage(int[][] reducedProfileShape, Scalar[][] reducedProfileColour, HashMap<String, Double> foregroundColourBreakdown) {
        this.reducedProfileShape = reducedProfileShape;
        this.reducedProfileColour = reducedProfileColour;
        this.foregroundColourBreakdown = foregroundColourBreakdown;
    }

    public int[][] getRPS() {
        return reducedProfileShape;
    }

    public Scalar[][] getRPC() {
        return reducedProfileColour;
    }

    public HashMap<String, Double> getFCB() {
        return foregroundColourBreakdown;
    }

    public void DisplayReducedImages(Mat sizeToMatch, int height, int width, String fileName) {
        Mat bWDisplay = new Mat(sizeToMatch.size(), sizeToMatch.type());
        Mat colDisplay = new Mat(sizeToMatch.size(), sizeToMatch.type());
        float blockHeight = (float) sizeToMatch.height() / height;
        float blockWidth = (float) sizeToMatch.width() / width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int innery = (int) Math.floor(blockHeight * y); innery <= blockHeight * (y + 1) - 1; innery++) {
                    for (int innerx = (int) Math.floor(blockWidth * x); innerx <= blockWidth * (x + 1) - 1; innerx++) {
                        bWDisplay.put(innery, innerx, reducedProfileShape[y][x]*255, reducedProfileShape[y][x]*255, reducedProfileShape[y][x]*255);
                        colDisplay.put(innery, innerx, reducedProfileColour[y][x].val[0], reducedProfileColour[y][x].val[1], reducedProfileColour[y][x].val[2]);
                    }
                }
            }
        }
        HighGui.imshow("ReducedBW " + fileName, bWDisplay);
        Imgproc.cvtColor(colDisplay, colDisplay, Imgproc.COLOR_Lab2BGR);
        HighGui.imshow("ReducedCol " + fileName, colDisplay);
        System.out.println(foregroundColourBreakdown);

        //Silly Thing - Reduced Rez
        //DisplayC64isedImage(colDisplay, fileName);
    }
    //This is silly
    public static void DisplayC64isedImage(Mat imgBGR, String fileName){
        Mat hsv = new Mat();
        Imgproc.cvtColor(imgBGR, hsv, Imgproc.COLOR_BGR2HSV);
        int height = imgBGR.height();
        int width = imgBGR.width();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] hsvData = hsv.get(y, x);
                if (hsvData[2] <= 38){
                    //return "black";
                    imgBGR.put(y, x, 0,0,0);
                }
                else if (hsvData[1] <=38 &&  hsvData[2]<=102){
                    //return "dark grey";
                    imgBGR.put(y, x, 51,51,51);
                }
                else if (hsvData[1] <=38 && hsvData[2]<=138){
                    //return "grey";
                    imgBGR.put(y, x, 119,119,119);
                }
                else if (hsvData[1] <=38 && hsvData[2]<=216){
                    //return "light grey";
                    imgBGR.put(y, x, 187,187,187);
                }
                else if (hsvData[1] <= 38 && hsvData[2] > 216) {
                    //return "white";
                    imgBGR.put(y, x, 255,255,255);
                }
                else if ((hsvData[0] <= 8 || hsvData[0] > 175) && hsvData[1] > 130){
                    //return "red";
                    imgBGR.put(y, x, 0,0,136);
                }
                else if ((hsvData[0] <= 8 || hsvData[0] > 175) && hsvData[1] <= 130){
                    //return "light red";
                    imgBGR.put(y, x, 119,119,255);
                }
                else if (hsvData[0] <= 50  && hsvData[2] <= 110){
                    //return "brown";
                    imgBGR.put(y, x, 0,68,102);
                }
                else if (hsvData[0] <= 20  && hsvData[1] > 96){
                    //return "orange";
                    imgBGR.put(y, x, 85,136,221);
                }
                else if (hsvData[0] <= 35){
                    //return "yellow";
                    imgBGR.put(y, x, 119,238,238);
                }
                else if (hsvData[0] <= 70 && hsvData[1] > 96){
                    //return "green";
                    imgBGR.put(y, x, 85,204,0);
                }
                else if (hsvData[0] <= 70 && hsvData[1] <= 96){
                    //return "light green";
                    imgBGR.put(y, x, 102,255,170);
                }
                else if (hsvData[0] <= 97){
                    //return "teal";
                    imgBGR.put(y, x, 238,225,170);
                }
                else if (hsvData[0] <= 125 && hsvData[1] > 128){
                    //return "blue";
                    imgBGR.put(y, x, 170,0,0);
                }
                else if (hsvData[0] <= 125 && hsvData[1] <= 128){
                    //return "light blue";
                    imgBGR.put(y, x, 255,136,0);
                }
                else if (hsvData[0] > 125){
                    //return "purple";
                    imgBGR.put(y, x, 204,68,204);
                }
                else {
                    imgBGR.put(y, x, 0,0,0);
                    //return "unknown";
                }
            }
        }
        HighGui.imshow("C64 " + fileName, imgBGR);
    }
}
