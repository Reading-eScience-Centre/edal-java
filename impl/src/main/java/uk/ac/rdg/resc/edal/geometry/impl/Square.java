package uk.ac.rdg.resc.edal.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.Polygon;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.impl.HorizontalPositionImpl;

/**
 * Immutable implementation of the {@link Polygon} interface which defines a
 * square in {@link Double} coordinates. Useful for defining cells on a
 * rectilinear grid.
 * 
 * @author Guy Griffiths
 * 
 */
public class Square implements Polygon {

    private final Double minX;
    private final Double maxX;
    private final Double minY;
    private final Double maxY;

    private final CoordinateReferenceSystem crs;

    /**
     * Instantiate the {@link Square} with the minimum and maximum bounds in
     * both directions
     * 
     * @param minX The minimum x coordinate
     * @param minY The minimum y coordinate
     * @param maxX The maximum x coordinate
     * @param maxY The maximum y coordinate
     * @param crs The {@link CoordinateReferenceSystem} to which the x and y values apply
     */
    public Square(Double minX, Double minY, Double maxX, Double maxY, CoordinateReferenceSystem crs) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.crs = crs;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    @Override
    public List<HorizontalPosition> getVertices() {
        List<HorizontalPosition> positions = new ArrayList<HorizontalPosition>();
        positions.add(new HorizontalPositionImpl(minX, minY, crs));
        positions.add(new HorizontalPositionImpl(maxX, minY, crs));
        positions.add(new HorizontalPositionImpl(maxX, maxY, crs));
        positions.add(new HorizontalPositionImpl(minX, maxY, crs));
        return Collections.unmodifiableList(positions);
    }

    @Override
    public boolean contains(HorizontalPosition position) {
        return (position.getX() >= minX && position.getX() <= maxX && position.getY() >= minY && position.getY() <= maxY);
    }
}
