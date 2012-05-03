/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

/**
 * A {@link RangeMetadata} object that describes the bands of a remote sensing
 * dataset.  These bands will usually be expressed in terms of wavelength ranges.
 * @author Jon
 */
public interface BandCollection extends RangeMetadata {
    
    
    /**
     * Returns the metadata descriptor for the given band
     */
    @Override
    public BandMetadata getMemberMetadata(String band);
}
