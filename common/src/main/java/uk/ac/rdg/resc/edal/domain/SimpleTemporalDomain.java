package uk.ac.rdg.resc.edal.domain;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A simple {@link TemporalDomain} containing just an extent and a
 * {@link Chronology}
 * 
 * @author Guy Griffiths
 */
public class SimpleTemporalDomain implements TemporalDomain {

    private final Extent<DateTime> extent;
    private final Chronology chronology;
    
    public SimpleTemporalDomain(DateTime min, DateTime max, Chronology chronology) {
        this.chronology = chronology;
        if(min == null || max == null) {
            extent = Extents.emptyExtent(DateTime.class);
        } else {
            extent = Extents.newExtent(min, max);
        }
    }
    
    @Override
    public boolean contains(DateTime position) {
        return extent.contains(position);
    }

    @Override
    public Extent<DateTime> getExtent() {
        return extent;
    }

    @Override
    public Chronology getChronology() {
        return chronology;
    }
}
