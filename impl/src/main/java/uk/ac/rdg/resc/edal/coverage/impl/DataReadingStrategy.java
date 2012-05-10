/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

/**
 * Strategy for reading data from a {@link GridDataSource}.
 * @author Jon
 */
public enum DataReadingStrategy {
    
    PIXEL_BY_PIXEL, SCANLINE, BOUNDING_BOX;
    
}
