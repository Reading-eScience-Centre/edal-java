package org.edal;

import java.util.Set;

/**
 * <p>Superclass for all CSML FeatureTypes.</p>
 * @author Jon
 * @todo How best to represent metadata?
 */
public interface Feature 
{
    
    /**
     * Gets the {@link FeatureCollection} to which this feature belongs.  If this
     * feature does not belong to a collection, this will return null.
     * @return
     */
    public FeatureCollection<? extends Feature> getFeatureCollection();
    
    /**
     * Gets an identifier that is unique within the {@link #getFeatureCollection() 
     * feature collection to which this feature belongs}.  Must never be null.
     */
    public String getId();
    
    /**
     * Gets a human-readable short string that identifies this feature.
     * Not enforced to be unique.
     */
    public String getName();
    
    /**
     * Gets a (perhaps lengthy) human-readable description of this feature.
     * @return
     */
    public String getDescription();

    /**
     * Gets the calendar system used to interpret dates and times relating to
     * this feature.
     * @return
     */
    public CalendarSystem getCalendarSystem();

    /**
     * Gets a list of identifiers for all the members of this Feature.  These
     * identifiers are only used internally and are not generally displayed to
     * users.
     */
    public Set<String> getMemberNames();

    /**
     * Gets the {@link Phenomenon} represented by the given member name.
     * @param memberName
     * @return the {@link Phenomenon} represented by the given member name.
     * @throws IllegalArgumentException if {@code memberName} is not a member
     * of the {@link #getMemberNames() set of member names}.
     */
    public Phenomenon getPhenomenon(String memberName);
}
