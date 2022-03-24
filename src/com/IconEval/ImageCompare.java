package com.IconEval;

import com.IconEval.ImageStats;

import java.util.HashMap;

public class ImageCompare {
    String fileNameA;
    String fileNameB;
    double backgroundDifferenceScore;
    int[][] combinedForegroundShape;
    double[][] ForegroundColourDiff;
    HashMap<String, Integer> colourDiff;
    double similarity;

    public ImageCompare(ImageStats a, ImageStats b){
        fileNameA = a.getFileName();
        fileNameB = b.getFileName();
        backgroundDifferenceScore = calcColourDifferenceScore(a.getBgCol(), b.getBgCol());
        combinedForegroundShape = calcProfileOverlap(a.getReducedImage().getRPS(), b.getReducedImage().getRPS());
        System.out.println(backgroundDifferenceScore);
    }
    private double calcColourDifferenceScore(Double[] colA, Double[] colB){
        System.out.println(colA[0] + ", " + colA[1] + ", " + colA[2] + ". " + colB[0] + ", " + colB[1] + ", " + colB[2] + ". ");
        Double[] colARealLab = {(colA[0]/256)*100, colA[1], colA[2]};
        Double[] colBRealLab = {(colB[0]/256)*100, colB[1], colB[2]};
        double difBetweenAB = (Math.sqrt(Math.pow(colARealLab[0]-colBRealLab[0], 2) + Math.pow(colARealLab[1]-colBRealLab[1], 2) + Math.pow(colARealLab[2]-colBRealLab[2], 2)));
        return (Math.max((100-difBetweenAB), 0));
    }

    //Needs something for calculating percentage overlap
    private int[][] calcProfileOverlap(int[][] reducedProfileA, int[][] reducedProfileB){
        if (reducedProfileA.length != reducedProfileB.length || reducedProfileA[0].length != reducedProfileB[0].length){
            return (new int[1][1]);
        }
        int[][] output = new int[reducedProfileA.length][reducedProfileA[0].length];
        for (int y = 0; y < reducedProfileA.length; y++){
            for (int x = 0; x < reducedProfileA[0].length; x++){
                if (reducedProfileA[y][x] == 1 && reducedProfileB[y][x] == 1){
                    output[y][x] = 1;
                }
                else{
                    output[y][x] = 0;
                }
            }
        }
        return output;
    }
}
