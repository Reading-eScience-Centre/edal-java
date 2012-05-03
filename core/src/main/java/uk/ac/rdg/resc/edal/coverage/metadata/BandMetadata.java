/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.metadata;

import uk.ac.rdg.resc.edal.Unit;

/**
 * <p>Metadata descriptor for a single band within a {@link BandCollection}.
 * Usually the child elements will be instances of {@link ScalarMetadata}.</p>
 * <p>This is roughly equivalent to the ISO19115 MD_Band class</p>
 * @author Jon
 */
public interface BandMetadata extends RangeMetadata {
    
    /** The minimum value of the band */
    public double getMinValue();
    /** The maximum value of the band */
    public double getMaxValue();
    
    /** The units of the band */
    public Unit getUnits();
}
