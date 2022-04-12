package com.IconEval;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ImageStats{
    String fileName;
    Double[] bgCol;
    ReducedImage reduced;

    public ImageStats(Mat img, String fileName){
        this.fileName = fileName;

        Mat CIELab = new Mat();
        Mat HSV = new Mat();

        Imgproc.cvtColor(img, CIELab, Imgproc.COLOR_BGR2Lab);
        Imgproc.cvtColor(img, HSV, Imgproc.COLOR_BGR2HSV);

        int sliceWidth = img.width()/4;
        bgCol = detBG(CIELab, sliceWidth, 8);

        Mat foregroundMask = thresholdLab(CIELab, bgCol);


        int rowsErosion = Math.max(img.rows()/100, 1);
        int colsErosion = Math.max(img.cols()/100, 1);
        int rowsDilation = Math.max(img.rows()/175, 1);
        int colsDilation = Math.max(img.cols()/175, 1);
        ///*
        Mat kernelErosion = Mat.ones(rowsErosion,colsErosion, CvType.CV_32F);
        Mat kernelDilation = Mat.ones(rowsDilation,colsDilation, CvType.CV_32F);
        Imgproc.erode(foregroundMask, foregroundMask, kernelErosion);
        Imgproc.dilate(foregroundMask, foregroundMask, kernelDilation);
        //*/
        int reducedHeight = 32;
        int reducedWidth = 32;

        /*
        //These scales are for C64 Display
        reducedHeight = 120;
        reducedWidth = 160;
        //*/

        reduced = reduceProfileComplexity(foregroundMask, CIELab, HSV, reducedHeight, reducedWidth, 8);

        //Debug code
        /*
        Mat foreground = mask(foregroundMask, img);
        Mat bgFromLab = new Mat();
        Mat bglab = new Mat(img.size(), img.type(), new Scalar(bgCol[0], bgCol[1], bgCol[2]));
        Imgproc.cvtColor(bglab, bgFromLab, Imgproc.COLOR_Lab2BGR);
        HighGui.imshow("Original " + fileName, img);
        HighGui.imshow("BGLab " + fileName, bgFromLab);
        HighGui.imshow("ThresholdingLab " + fileName, foregroundMask);
        HighGui.imshow("Foreground" + fileName, foreground);
        */
        ///*
        reduced.DisplayReducedImages(img, reducedHeight, reducedWidth, fileName);
        //*/

        //Silly Thing - Full Rez
        //ReducedImage.DisplayC64isedImage(img, fileName);

    }
    public Double[] getBgCol() {
        return bgCol;
    }
    public ReducedImage getReducedImage() {
        return reduced;
    }
    public String getFileName() {
        return fileName;
    }

    private static Double[] detBG(Mat img, int sliceWidth, int precision){
        HashMap<List<Double>, Integer> detColours = new HashMap<>();
        for(int y = 0; y < img.height(); y++) {
            for(int x = 0; x < img.width(); x++){
                if(y < sliceWidth || y < img.height() - sliceWidth && (x < sliceWidth || x >= img.width() - sliceWidth) || y >= img.height() - sliceWidth){
                    double[] colData = img.get(y, x);
                    //img.put(y, x, 192, 128+64, 128-32);
                    double[] colRound = new double[3];
                    for (int i = 0; i< colRound.length; i++) {
                        colRound[i] = roundTo(colData[i], precision);
                    }
                    List<Double> colKey = DoubleStream.of(colRound).boxed().collect(Collectors.toList());
                    if(detColours.containsKey(colKey)){
                        detColours.put(colKey, detColours.get(colKey)+1);
                    }
                    else{
                        detColours.put(colKey, 1);
                    }

                }

            }
        }
        //Imgproc.cvtColor(img, img, Imgproc.COLOR_Lab2BGR);
        //HighGui.imshow("Original ", img);
        return (Collections.max(detColours.entrySet(), Map.Entry.comparingByValue()).getKey()).toArray(new Double[3]);
    }
    private static Mat thresholdLab(Mat hsv, Double[] bgCol){
        Mat threshold = new Mat();
        Core.inRange(hsv, new Scalar(bgCol[0]-64, bgCol[1]-32, bgCol[2]-32), new Scalar(bgCol[0]+64, bgCol[1]+32, bgCol[2]+32), threshold );
        Core.bitwise_not(threshold, threshold);
        return threshold;
    }

    public static Mat mask(Mat bwMask, Mat img){
        Mat mask = new Mat();
        Mat foreground = new Mat();
        Imgproc.cvtColor(bwMask, mask, Imgproc.COLOR_GRAY2BGR);
        Core.bitwise_and(img, mask, foreground);
        return foreground;
    }


    private static ReducedImage reduceProfileComplexity(Mat bwProfile, Mat colour, Mat colourHSV, int height, int width, int precision){
        int[][] reducedProfileShape = new int[height][width];
        Scalar[][] reducedProfileColour = new Scalar[height][width];
        HashMap<String, Double> foregroundColourTotal= new HashMap<>();
        HashMap<String, Double> foregroundColourBreakdown= new HashMap<>();

        int matHeight = bwProfile.height();
        int matWidth = bwProfile.width();
        float blockHeight = (float) matHeight/height;
        float blockWidth = (float) matWidth/width;

        int totalForegroundCount = 0;

        for(int y = 0; y<height; y++){
            for(int x = 0; x<width; x++){
                HashMap<List<Double>, Integer> detColours = new HashMap<>();
                int countWhite = 0;
                int countBlack = 0;
                for(int innery = (int) Math.floor(blockHeight*y); innery<=blockHeight*(y+1)-1; innery++){
                    for(int innerx = (int) Math.floor(blockWidth*x); innerx<=blockWidth*(x+1)-1; innerx++){
                        double[] bwData = bwProfile.get(innery, innerx);
                        if (bwData[0]==255.0){
                            countWhite++;
                            double[] colData = colour.get(innery, innerx);
                            double[] colRound = new double[3];

                            totalForegroundCount ++;
                            double[]colDataHSV = colourHSV.get(innery, innerx);
                            String colourName = ClassifyColour(colDataHSV);
                            if (!colourName.equals("unknown")){
                                if (foregroundColourTotal.containsKey(colourName)){
                                    foregroundColourTotal.put(colourName, foregroundColourTotal.get(colourName)+1);
                                }
                                else{
                                    foregroundColourTotal.put(colourName, 1.0);
                                }
                            }
                            for (int i = 0; i< colRound.length; i++) {
                                colRound[i] = roundTo(colData[i], precision);
                            }
                            List<Double> colKey = DoubleStream.of(colRound).boxed().collect(Collectors.toList());
                            if(detColours.containsKey(colKey)){
                                detColours.put(colKey, detColours.get(colKey)+1);
                            }
                            else{
                                detColours.put(colKey, 1);
                            }
                        } else{
                            countBlack++;

                            /*
                            //Everything after for C64 view
                            double[] colData = colour.get(innery, innerx);
                            double[] colRound = new double[3];
                            for (int i = 0; i< colRound.length; i++) {
                                colRound[i] = roundTo(colData[i], precision);
                            }
                            List<Double> colKey = DoubleStream.of(colRound).boxed().collect(Collectors.toList());
                            if(detColours.containsKey(colKey)){
                                detColours.put(colKey, detColours.get(colKey)+1);
                            }
                            else{
                                detColours.put(colKey, 1);
                            }
                             //*/


                        }
                    }
                }
                if (countWhite>=countBlack){
                    reducedProfileShape[y][x] = 1;
                    Double[] mostCommonCol = (Collections.max(detColours.entrySet(), Map.Entry.comparingByValue()).getKey()).toArray(new Double[3]);
                    reducedProfileColour[y][x] = new Scalar(mostCommonCol[0], mostCommonCol[1], mostCommonCol[2]);
                } else {
                    reducedProfileShape[y][x] = 0;
                    reducedProfileColour[y][x] = new Scalar(0.0, 128.0, 128.0);

                    /*
                    // Everything after is for C64 view
                    Double[] mostCommonCol = (Collections.max(detColours.entrySet(), Map.Entry.comparingByValue()).getKey()).toArray(new Double[3]);
                    reducedProfileColour[y][x] = new Scalar(mostCommonCol[0], mostCommonCol[1], mostCommonCol[2]);
                    //*/

                }
            }
        }
        for (Map.Entry<String, Double> entry: foregroundColourTotal.entrySet()){
            foregroundColourBreakdown.put(entry.getKey(), (entry.getValue()/totalForegroundCount)*100);
        }
        return new ReducedImage(reducedProfileShape, reducedProfileColour, foregroundColourBreakdown);
    }
    private static String ClassifyColour(double[] colDataHSV){
        if (colDataHSV[2] <= 38){
            return "black";
        }
        else if (colDataHSV[1] <=38 && colDataHSV[2]<=216){
            return "grey";
        }
        else if (colDataHSV[1] <= 38 && colDataHSV[2] > 216) {
            return "white";
        }
        else if (colDataHSV[0] <= 8 || colDataHSV[0] > 175){
            return "red";
        }
        else if (colDataHSV[0] <= 20){
            return "orange";
        }
        else if (colDataHSV[0] <= 35){
            return "yellow";
        }
        else if (colDataHSV[0] <= 70){
            return "green";
        }
        else if (colDataHSV[0] <= 85){
            return "teal";
        }
        else if (colDataHSV[0] <= 125){
            return "blue";
        }
        else if (colDataHSV[0] > 125){
            return "purple";
        }
        else {
            return "unknown";
        }
    }
    public static int roundTo(Double val, int precision){
        return (int) Math.round(val/precision) * precision;
    }

}
