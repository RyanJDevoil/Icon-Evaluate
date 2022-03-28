package com.IconEval;
import java.util.HashMap;

public class ImageCompare {
    String fileNameA;
    String fileNameB;
    double backgroundDifferenceScore;
    double foregroundIntersectScore;
    double[][] ForegroundColourDiff;
    HashMap<String, Integer> colourDiff;
    double similarity;

    public ImageCompare(ImageStats a, ImageStats b){
        fileNameA = a.getFileName();
        fileNameB = b.getFileName();
        backgroundDifferenceScore = calcColourDifferenceScore(a.getBgCol(), b.getBgCol());
        foregroundIntersectScore = calcForegroundIntersectScore(a.getReducedImage().getRPS(), b.getReducedImage().getRPS());


        System.out.println(backgroundDifferenceScore);
        System.out.println(foregroundIntersectScore);
    }
    private double calcColourDifferenceScore(Double[] colA, Double[] colB){
        Double[] colARealLab = {(colA[0]/256)*100, colA[1], colA[2]};
        Double[] colBRealLab = {(colB[0]/256)*100, colB[1], colB[2]};
        double difBetweenAB = (Math.sqrt(Math.pow(colARealLab[0]-colBRealLab[0], 2) + Math.pow(colARealLab[1]-colBRealLab[1], 2) + Math.pow(colARealLab[2]-colBRealLab[2], 2)));
        return (Math.max((100-difBetweenAB), 0));
    }

    //Scale score based on distance from centre - we expect more central overlap than we see on the outside. get max possible score from Union.
    private double calcForegroundIntersectScore(int[][] reducedProfileA, int[][] reducedProfileB){
        if (reducedProfileA.length != reducedProfileB.length || reducedProfileA[0].length != reducedProfileB[0].length){
            return (-1.0);
        }
        double midY= (double)(reducedProfileB.length-1)/2;
        double midX= (double)(reducedProfileA[0].length-1)/2;
        double maxScore = 0;
        double score = 0;
        for (int y = 0; y < reducedProfileA.length; y++){
            for (int x = 0; x < reducedProfileA[0].length; x++){
                if (reducedProfileA[y][x] == 1 || reducedProfileB[y][x] == 1){
                    double weightedVal = Math.pow((Math.sqrt(Math.pow(y-midY, 2)+Math.pow(x-midX, 2))/4), 2);
                    if (reducedProfileA[y][x] == 1 && reducedProfileB[y][x] == 1){
                        maxScore += weightedVal;
                        score += weightedVal;
                    }
                    else {
                        maxScore += weightedVal;
                    }
                }
            }
        }
        double normalisedScore = (score/maxScore)*100;
        return normalisedScore;
    }
}
