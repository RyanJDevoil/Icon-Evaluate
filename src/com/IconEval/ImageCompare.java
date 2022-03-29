package com.IconEval;
import java.io.*;
import java.util.HashMap;
import org.opencv.core.Scalar;

public class ImageCompare {
    String fileNameA;
    String fileNameB;
    double backgroundDifferenceScore;
    double foregroundIntersectScore;
    double foregroundColourDiffScore;
    double colourBreakdownScore;
    double similarity;

    public ImageCompare(ImageStats a, ImageStats b){
        fileNameA = a.getFileName();
        fileNameB = b.getFileName();

        //A value measuring how close the background colours are
        backgroundDifferenceScore = calcColourDifferenceScore(a.getBgCol(), b.getBgCol());

        double[] foregroundData = calcForegroundIntersectScore(a.getReducedImage().getRPS(), b.getReducedImage().getRPS(), a.getReducedImage().getRPC(), b.getReducedImage().getRPC());
        //A value scoring how much the foreground shapes overlap
        foregroundIntersectScore = foregroundData[0];
        //A value showing how close the foreground colours are in areas that overlap, high means similar colours in the same places
        foregroundColourDiffScore = foregroundData[1];

        //A value of how close the general colours used are
        colourBreakdownScore = calcColourBreakdownScore(a.getReducedImage().getFCB(), b.getReducedImage().getFCB());

        //An overall score of similarity out of 100
        similarity = calcSimilarity(backgroundDifferenceScore, foregroundIntersectScore, foregroundColourDiffScore, colourBreakdownScore);

        /*
        System.out.println("Similarity for " + fileNameA + " & " + fileNameB);
        System.out.println("Overall Similarity: " + similarity);
        System.out.println("Background Similarity: " + backgroundDifferenceScore);
        System.out.println("Foreground Shape Similarity: " + foregroundIntersectScore);
        System.out.println("Foreground Colour Similarity: " + foregroundColourDiffScore);
        System.out.println("Colour Usage Similarity: " + colourBreakdownScore);
         */

        try{
            File outputFile = new File("Similarity Report.txt");
            if(!outputFile.exists()){
                outputFile.createNewFile();
            }
            FileWriter outputFileWriter = new FileWriter(outputFile, true);
            BufferedWriter outputBufferedWriter = new BufferedWriter(outputFileWriter);
            PrintWriter writer = new PrintWriter(outputBufferedWriter);
            if(similarity>50){
                writer.println("High overall similarity found between: " + fileNameA + " and " +fileNameB);
                writer.println("Overall Similarity: " + similarity);
                writer.println("Background Similarity: " + backgroundDifferenceScore);
                writer.println("Foreground Shape Similarity: " + foregroundIntersectScore);
                writer.println("Foreground Colour Similarity: " + foregroundColourDiffScore);
                writer.println("Colour Usage Similarity: " + colourBreakdownScore);
                writer.println();
            }
            else{
                writer.println("Low overall similarity found between: " + fileNameA + " and " +fileNameB);
                if(backgroundDifferenceScore > 50){
                    writer.println("However background colour similarity is high.");
                    writer.println("Background Similarity: " + backgroundDifferenceScore);
                }
                if(foregroundIntersectScore > 50){
                    writer.println("However foreground shape similarity is high.");
                    writer.println("Foreground Shape Similarity: " + foregroundIntersectScore);
                }
                if(foregroundColourDiffScore > 50){
                    writer.println("However similar colours are used in similar places.");
                    writer.println("Foreground Colour Similarity: " + foregroundColourDiffScore);
                }
                if(colourBreakdownScore > 50){
                    writer.println("However similar colours are used to make up the icon.");
                    writer.println("Colour Usage Similarity: " + colourBreakdownScore);
                }
                writer.println();
            }
            writer.close();
        }
        catch(Exception e){
            System.out.println("Error accessing or writing file");
        }
    }
    private double calcColourDifferenceScore(Double[] colA, Double[] colB){
        Double[] colARealLab = {(colA[0]/256)*100, colA[1], colA[2]};
        Double[] colBRealLab = {(colB[0]/256)*100, colB[1], colB[2]};
        double difBetweenAB = (Math.sqrt(Math.pow(colARealLab[0]-colBRealLab[0], 2) + Math.pow(colARealLab[1]-colBRealLab[1], 2) + Math.pow(colARealLab[2]-colBRealLab[2], 2)));
        return (Math.max((100-difBetweenAB), 0));
    }

    //Scale score based on distance from centre - we expect more central overlap than we see on the outside. get max possible score from Union.
    private double[] calcForegroundIntersectScore(int[][] reducedProfileA, int[][] reducedProfileB, Scalar[][] reducedColourA, Scalar[][] reducedColourB){
        if (reducedProfileA.length != reducedProfileB.length || reducedProfileA[0].length != reducedProfileB[0].length){
            return new double[2];
        }
        double midY= (double)(reducedProfileB.length-1)/2;
        double midX= (double)(reducedProfileA[0].length-1)/2;
        double maxScore = 0;
        double score = 0;
        double totalColourDistance = 0;
        int numIntersecting = 0;
        for (int y = 0; y < reducedProfileA.length; y++){
            for (int x = 0; x < reducedProfileA[0].length; x++){
                if (reducedProfileA[y][x] == 1 || reducedProfileB[y][x] == 1){
                    double weightedVal = Math.pow((Math.sqrt(Math.pow(y-midY, 2)+Math.pow(x-midX, 2))/4), 2);
                    if (reducedProfileA[y][x] == 1 && reducedProfileB[y][x] == 1){
                        maxScore += weightedVal;
                        score += weightedVal;

                        numIntersecting += 1;
                        totalColourDistance += Math.sqrt(Math.pow(reducedColourA[y][x].val[0]-reducedColourB[y][x].val[0], 2)+Math.pow(reducedColourA[y][x].val[1]-reducedColourB[y][x].val[1], 2)+Math.pow(reducedColourA[y][x].val[2]-reducedColourB[y][x].val[2], 2));

                    }
                    else {
                        maxScore += weightedVal;
                    }
                }
            }
        }
        double normalisedScore = (score/maxScore)*100;
        double avgColourDistanceScore = Math.max(100-(totalColourDistance/numIntersecting), 0);
        double[] output = {normalisedScore, avgColourDistanceScore};
        return output;
    }
    private double calcColourBreakdownScore(HashMap<String, Double> colourBreakdownA, HashMap<String, Double> colourBreakdownB){
        HashMap<String, Double> colourBreakdownCombined = new HashMap<String, Double>();
        Double sharedColourBreakdown = 0.0;
        for (String key : colourBreakdownA.keySet()){
            if(colourBreakdownB.containsKey(key)){
                colourBreakdownCombined.put(key, Math.min(colourBreakdownA.get(key), colourBreakdownB.get(key)));
            }
        }
        for(Double value : colourBreakdownCombined.values()){
            sharedColourBreakdown += value;
        }
        return (sharedColourBreakdown);
    }

    private double calcSimilarity(double backgroundDifferenceScore, double foregroundIntersectScore, double foregroundColourDiffScore, double colourBreakdownScore){
        //40, 30, 15, 15
        double similarity = 0.4*backgroundDifferenceScore+0.3*foregroundIntersectScore+0.15*foregroundColourDiffScore+0.15*colourBreakdownScore;
        return similarity;
    }
}
