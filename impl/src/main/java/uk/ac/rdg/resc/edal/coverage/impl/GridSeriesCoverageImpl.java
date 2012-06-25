package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.coverage.GridCoverage2D;
import uk.ac.rdg.resc.edal.coverage.GridSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.PointSeriesCoverage;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.coverage.domain.GridSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.PointSeriesDomain;
import uk.ac.rdg.resc.edal.coverage.domain.ProfileDomain;
import uk.ac.rdg.resc.edal.coverage.domain.impl.PointSeriesDomainImpl;
import uk.ac.rdg.resc.edal.coverage.domain.impl.ProfileDomainImpl;
import uk.ac.rdg.resc.edal.coverage.grid.GridCell4D;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates2D;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.plugins.Plugin;
import uk.ac.rdg.resc.edal.position.GeoPosition;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.TimePosition;
import uk.ac.rdg.resc.edal.position.VerticalPosition;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * An implementation of a {@link GridSeriesCoverage}. By basing this on
 * {@link AbstractMultimemberDiscreteGridCoverage}, we can add {@link Plugin}s
 * and add members arbitrarily by just supplying the correct
 * {@link GridValuesMatrix}
 * 
 * @author Guy Griffiths
 * 
 */
public class GridSeriesCoverageImpl extends
        AbstractMultimemberDiscreteGridCoverage<GeoPosition, GridCell4D, GridSeriesDomain>
        implements GridSeriesCoverage {

    private final DataReadingStrategy strategy;

    public GridSeriesCoverageImpl(String description, GridSeriesDomain domain,
            DataReadingStrategy strategy) {
        super(description, domain);
        this.strategy = strategy;
    }

    @Override
    public GridSeriesDomain getDomain() {
        return super.getDomain();
    }

    @Override
    public GridCoverage2D extractGridCoverage(HorizontalGrid targetGrid, VerticalPosition zPos,
            TimePosition tPos, Set<String> memberNames) {
        if (targetGrid.size() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Target grid too large");
        }

        Map<String, List<Object>> valuesMap = CollectionUtils.newLinkedHashMap();
        Map<String, ScalarMetadata> metadataMap = CollectionUtils.newLinkedHashMap();
        // Read the data from the source coverage
        Extent<Integer> xExtent = getDomain().getHorizontalGrid().getXAxis().getIndexExtent();
        Extent<Integer> yExtent = getDomain().getHorizontalGrid().getYAxis().getIndexExtent();
        int zIndex = getDomain().getVerticalAxis().findIndexOf(zPos.getZ());
        int tIndex = getDomain().getTimeAxis().findIndexOf(tPos);

        for (String name : memberNames) {
            List<Object> values = strategy.readValues(
                    this.getGridValues(name).readBlock(
                            new int[] { xExtent.getLow(), yExtent.getLow(), zIndex, tIndex },
                            new int[] { xExtent.getHigh(), yExtent.getHigh(), zIndex, tIndex }),
                    this.getDomain().getHorizontalGrid(), targetGrid);

            valuesMap.put(name, values);
            metadataMap.put(name, this.getRangeMetadata(name));
        }

        // Now assemble the remaining properties of the target coverage
        return new InMemoryGridCoverage2D(targetGrid, valuesMap, metadataMap,
                "Interpolated grid from " + getDescription());
    }

    @Override
    public ProfileCoverage extractProfileCoverage(HorizontalPosition pos, TimePosition time,
            Set<String> memberNames) {
        GridCoordinates2D hPos = getDomain().getHorizontalGrid().findContainingCell(pos);
        int x = hPos.getXIndex();
        int y = hPos.getYIndex();
        int t = getDomain().getTimeAxis().findIndexOf(time);
        Extent<Integer> vExtent = getDomain().getVerticalAxis().getIndexExtent();
        ProfileDomain domain = new ProfileDomainImpl(getDomain().getVerticalAxis()
                .getCoordinateValues(), getDomain().getVerticalCrs());
        ProfileCoverageImpl pCoverage = new ProfileCoverageImpl("Profile coverage", domain);
        for (String memberName : memberNames) {
            GridValuesMatrix<?> memberCoverage = getGridValues(memberName).readBlock(
                    new int[] { x, y, vExtent.getLow(), t },
                    new int[] { x, y, vExtent.getHigh(), t });
            ScalarMetadata memberMetadata = getRangeMetadata(memberName);
            pCoverage.addMember(memberName, domain, memberMetadata.getDescription(),
                    memberMetadata.getParameter(), memberMetadata.getUnits(),
                    memberCoverage.getValues());
        }
        return pCoverage;
    }

    @Override
    public PointSeriesCoverage extractPointSeriesCoverage(HorizontalPosition pos,
            VerticalPosition zPos, Extent<? extends TimePosition> tExtent, Set<String> memberNames) {
        GridCoordinates2D hPos = getDomain().getHorizontalGrid().findContainingCell(pos);
        int x = hPos.getXIndex();
        int y = hPos.getYIndex();
        int z = getDomain().getVerticalAxis().findIndexOf(zPos.getZ());
        int tmin = getDomain().getTimeAxis().findIndexOf((TimePosition) tExtent.getLow());
        int tmax = getDomain().getTimeAxis().findIndexOf((TimePosition) tExtent.getHigh());
        PointSeriesDomain domain = new PointSeriesDomainImpl(getDomain().getTimeAxis()
                .getCoordinateValues().subList(tmin, tmax));
        PointSeriesCoverageImpl psCoverage = new PointSeriesCoverageImpl("Point series coverage",
                domain);
        for (String memberName : memberNames) {
            GridValuesMatrix<?> memberCoverage = getGridValues(memberName).readBlock(
                    new int[] { x, y, z, tmin }, new int[] { x, y, z, tmax });
            ScalarMetadata memberMetadata = getRangeMetadata(memberName);
            psCoverage.addMember(memberName, domain, memberMetadata.getDescription(),
                    memberMetadata.getParameter(), memberMetadata.getUnits(),
                    memberCoverage.getValues());
        }
        return psCoverage;
    }
}
