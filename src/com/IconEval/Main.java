package com.IconEval;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class Main {
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        String filepath = "icons/";

        File f = new File(filepath);

        // This filter will only include files ending with .png or .jpg
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        };

        String[] fileNames = f.list(filter);
        int numIcons = fileNames.length;
        ImageStats[] imgStats = new ImageStats[numIcons];
        for (int i = 0; i<numIcons; i++){
            System.out.println(fileNames[i]);
            imgStats[i] = new ImageStats(Imgcodecs.imread(filepath + fileNames[i]), fileNames[i]);
        }
        ImageCompare test = new ImageCompare(imgStats[0], imgStats[1]);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();


        /*
        Mat hsv = new Mat();
        Mat bgFromHSV = new Mat();
        Mat bgFromLab = new Mat();
        Mat CIELab = new Mat();

        Mat corner = harrisCorner(img);

        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(img, CIELab, Imgproc.COLOR_BGR2Lab);

        int sliceWidth = img.cols()/8;
        List<Double> bGColHSV = detBG(hsv, sliceWidth, 8);
        Mat bgHSV = new Mat(img.size(), img.type(), new Scalar(bGColHSV.get(0), bGColHSV.get(1), bGColHSV.get(2)));
        Imgproc.cvtColor(bgHSV, bgFromHSV, Imgproc.COLOR_HSV2BGR);

        Mat thresholdHSV = thresholdHSV(hsv, bGColHSV);

        List<Double> bGColLab = detBG(CIELab, sliceWidth, 8);
        Mat bglab = new Mat(img.size(), img.type(), new Scalar(bGColLab.get(0), bGColLab.get(1), bGColLab.get(2)));
        Imgproc.cvtColor(bglab, bgFromLab, Imgproc.COLOR_Lab2BGR);

        Mat thresholdLab = thresholdLab(CIELab, bGColLab);

        int reducedHeight = 16;
        int reducedWidth = 16;

        int[][]reducedProfile = reduceProfileComplexityMode(thresholdLab, reducedHeight,reducedWidth);
        for(int i = 0; i<reducedHeight; i++){
            System.out.println(Arrays.toString(reducedProfile[i]));
        }


        Mat edgesHSV = edges(thresholdHSV);
        Mat edgesLab = edges(thresholdLab);

        Mat foregroundHSV = mask(thresholdHSV, img);
        Mat foregroundLab = mask(thresholdLab, img);

        Mat histHSV = histogram(hsv);
        Mat histLab = histogram(CIELab);

        HighGui.imshow("Original", img);
        HighGui.imshow("Harrris", corner);
        HighGui.imshow("BGHSV", bgFromHSV);
        HighGui.imshow("BGLab", bgFromLab);
        HighGui.imshow("ThresholdingHSV", thresholdHSV);
        HighGui.imshow("ThresholdingLab", thresholdLab);
        HighGui.imshow("CannyHSV", edgesHSV);
        HighGui.imshow("CannyLab", edgesLab);
        HighGui.imshow("ForegroundHSV", foregroundHSV);
        HighGui.imshow("ForegroundLab", foregroundLab);
        HighGui.imshow("HistogramHSV", histHSV);
        HighGui.imshow("HistogramLab", histLab);
        if((HighGui.waitKey(0) & 0xff) == 27){
            HighGui.destroyAllWindows();
        } */
    }
    /*
    public static Mat harrisCorner(Mat img){
        Mat grey = Mat.zeros(img.size(), img.type());
        Mat corner = img.clone();
        Imgproc.cvtColor(corner, grey,Imgproc.COLOR_BGR2GRAY);
        Mat dst = Mat.zeros(corner.size(), corner.type());
        Imgproc.cornerHarris(grey, dst,2,3,0.04);
        Core.MinMaxLocResult dstMMLR = Core.minMaxLoc(dst);
        double dstMax = dstMMLR.maxVal;
        for(int y = 0; y < corner.cols(); y++) {
            for(int x = 0; x < corner.rows(); x++){
                if(dst.get(x, y)[0] > 0.01*dstMax){
                    corner.put(x, y, 0, 0, 255);
                }

            }
        }
        return corner;
    }
    public static Mat blobDetect(Mat imgGrey){
        SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();
        params.set_filterByColor(true);
        params.set_filterByArea(true);
        params.set_filterByCircularity(false);
        params.set_filterByConvexity(false);
        params.set_filterByInertia(false);
        SimpleBlobDetector detector = SimpleBlobDetector.create(params);
        MatOfKeyPoint kp = new MatOfKeyPoint();
        detector.detect(imgGrey, kp);
        Mat imWithKp = Mat.zeros(imgGrey.size(), imgGrey.type());
        Features2d.drawKeypoints(imgGrey, kp, imWithKp, new Scalar(0,0,255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
        return imWithKp;
    }
    public static List<Double> detBG(Mat img, int sliceWidth, int precision){
        HashMap<List<Double>, Integer> detColours = new HashMap<>();
        for(int y = 0; y < img.cols(); y++) {
            for(int x = 0; x < img.rows(); x++){
                if((y<sliceWidth-1||y>img.cols()-(sliceWidth+1))||(x<sliceWidth-1||x>img.rows()-(sliceWidth+1))){
                    List<Double> colour = Arrays.stream(img.get(x, y)).boxed().toList();
                    List<Double> colourRound = new ArrayList<>();
                    for (Double aDouble : colour) {
                        colourRound.add((double) roundTo(aDouble, precision));
                    }
                    if(detColours.containsKey(colourRound)){
                        detColours.put(colourRound, detColours.get(colourRound)+1);
                    }
                    else{
                        detColours.put(colourRound, 1);
                    }

                }

            }
        }
        return (Collections.max(detColours.entrySet(), Map.Entry.comparingByValue()).getKey());
    }

    public static Mat histogram(Mat img){
      List<Mat> bgr = new ArrayList<>();
      Core.split(img, bgr);
      int histSize = 64;

      float[] range = {0, 256};
      MatOfFloat histRange = new MatOfFloat(range);

      boolean accumulate = false;

      Mat bHist = new Mat();
      Mat gHist = new Mat();
      Mat rHist = new Mat();

        Imgproc.calcHist(bgr, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgr, new MatOfInt(1), new Mat(), gHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgr, new MatOfInt(2), new Mat(), rHist, new MatOfInt(histSize), histRange, accumulate);

        int histW = 512;
        int histH = 400;
        int binW = (int) Math.round((double) histW/histSize);

        Mat histImage = new Mat( histH, histW, CvType.CV_8UC3, new Scalar( 0,0,0) );

        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
        gHist.get(0, 0, gHistData);
        float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
        rHist.get(0, 0, rHistData);
        for( int i = 1; i < histSize; i++ ) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
        }
        return histImage;
    }
    //For HSV thresholding
    public static Mat thresholdHSV(Mat hsv, List<Double> bGCol){
        Mat threshold = new Mat();
        Core.inRange(hsv, new Scalar(bGCol.get(0)-16, bGCol.get(1)-64, bGCol.get(2)-64), new Scalar(bGCol.get(0)+16, bGCol.get(1)+64, bGCol.get(2)+64), threshold );
        Core.bitwise_not(threshold, threshold);
        return threshold;
    }
    //For CIELab thresholding
    public static Mat thresholdLab(Mat hsv, List<Double> bGCol){
        Mat threshold = new Mat();
        Core.inRange(hsv, new Scalar(bGCol.get(0)-64, bGCol.get(1)-40, bGCol.get(2)-40), new Scalar(bGCol.get(0)+64, bGCol.get(1)+40, bGCol.get(2)+40), threshold );
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
    public static Mat edges(Mat threshold){
        Mat edges = new Mat();
        Imgproc.Canny(threshold, edges, 0, 255);
        return edges;
    }

    public static int[][] reduceProfileComplexityMode(Mat bwProfile, int height, int width){
        int[][] reducedProfile = new int[height][width];

        int matHeight = bwProfile.height();
        int matWidth = bwProfile.width();
        float blockHeight = (float) matHeight/height;
        float blockWidth = (float) matWidth/width;

        for(int y = 0; y<height; y++){
            //System.out.println("Height : " + String.valueOf(Math.floor(blockHeight*y)) + " - " + String.valueOf(Math.floor((blockHeight*(y+1)))-1) + " Size: " + String.valueOf(Math.floor((blockHeight*(y+1)))-Math.floor(blockHeight*y)));
            for(int x = 0; x<width; x++){
                int countWhite = 0;
                int countBlack = 0;
                //System.out.println("Width  : " + String.valueOf(Math.floor(blockWidth*x)) + " - " + String.valueOf(Math.floor((blockWidth*(x+1)))-1) + " Size: " + String.valueOf(Math.floor((blockWidth*(x+1)))-Math.floor(blockWidth*x)));
                for(int innery = (int) Math.floor(blockHeight*y); innery<=blockHeight*(y+1)-1; innery++){
                    for(int innerx = (int) Math.floor(blockWidth*x); innerx<=blockWidth*(x+1)-1; innerx++){
                        double[] data = bwProfile.get(innery, innerx);
                        if (data[0]==255.0){
                            countWhite++;
                        } else{
                            countBlack++;
                        }
                    }
                }
                if (countWhite>=countBlack){
                    reducedProfile[y][x] = 1;
                } else {
                    reducedProfile[y][x] = 0;
                }
            }
        }
        return reducedProfile;
    }

    public static int roundTo(Double val, int prec){
        return (int) Math.round(val/prec) * prec;
    }
     */
}