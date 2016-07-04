/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.graphics.utils;

import java.util.List;

public class BilinearInterpolator {
    private List<Double> xVals;
    private List<Double> yVals;
    private double[][] data;
    
    private double[] xRanges;
    private double[] yRanges;

    public BilinearInterpolator(List<Double> xVals, List<Double> yVals, double[][] data) {
        this.xVals = xVals;
        this.yVals = yVals;
        this.data = data;
        
        xRanges = new double[xVals.size()];
        yRanges = new double[yVals.size()];
        
        for(int i=1; i < xVals.size(); i++) {
            xRanges[i] = xVals.get(i) - xVals.get(i-1);
        }
        for(int i=1; i < yVals.size(); i++) {
            yRanges[i] = yVals.get(i) - yVals.get(i-1);
        }
    }
    
    public double getValue(double x, double y) {
        /*
         * Find the indices above the x and y values  
         */
        int xIndex = 0;
        int yIndex = 0;
        while(xVals.get(xIndex) <= x && xIndex < xVals.size() - 1) {
            xIndex++;
        }
        while(yVals.get(yIndex) <= y && yIndex < yVals.size() - 1) {
            yIndex++;
        }
        double xFAlong;
        double yFAlong;
        if(xIndex == 0) {
            xIndex++;
            xFAlong = 1.0;
        } else {
            xFAlong = (x - xVals.get(xIndex - 1)) / xRanges[xIndex];
        }
        if(yIndex == 0) {
            yIndex++;
            yFAlong = 1.0;
        } else {
            yFAlong = (y - yVals.get(yIndex - 1)) / yRanges[yIndex];
        }
        
        return 
                xFAlong * yFAlong * data[xIndex][yIndex] +
                xFAlong * (1.0-yFAlong) * data[xIndex][yIndex-1] +
                (1.0-xFAlong) * yFAlong * data[xIndex-1][yIndex] +
                (1.0-xFAlong) * (1.0-yFAlong) * data[xIndex-1][yIndex-1];
    }
}
