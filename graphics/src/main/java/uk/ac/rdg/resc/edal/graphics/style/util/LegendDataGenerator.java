package uk.ac.rdg.resc.edal.graphics.style.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotoolkit.referencing.crs.DefaultGeographicCRS;

import uk.ac.rdg.resc.edal.dataset.DataReadingStrategy;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.grid.RegularAxisImpl;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.Extents;

/**
 * A class to generate the correct data for a legend.
 * 
 * @author guy
 * 
 */
public class LegendDataGenerator extends FeatureCollectionImpl<GridFeature> {

    private RegularAxisImpl xAxis;
    private RegularAxisImpl yAxis;
    private RegularGridImpl domain;
    private Set<NameAndRange> dataFields;
    private boolean[][] missingBits;
    
    private float fractionExtra; 

    public LegendDataGenerator(Set<NameAndRange> dataFields, int width, int height, BufferedImage backgroundMask, float fractionExtra) {
        super("", "");

        /*
         * We use 0.0001 as the spacing. Since we're working in WGS84 (for
         * convenience - it doesn't matter what CRS we use, but we need to work
         * in one) - anything outside normal lat/lon range will not be rendered.
         * 0.0001 spacing allows us to have each legend component be sized up to
         * (90 / 0.0001) pixels.
         */
        xAxis = new RegularAxisImpl("", 0, 0.001, width, false);
        yAxis = new RegularAxisImpl("", 0, 0.001, height, false);

        domain = new RegularGridImpl(xAxis, yAxis, DefaultGeographicCRS.WGS84);

        this.dataFields = dataFields;
        
        this.fractionExtra = fractionExtra;
        
        missingBits = new boolean[width][height];
        if(backgroundMask != null) {
            Image scaledInstance = backgroundMask.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bufferedImage.createGraphics().drawImage(scaledInstance, 0, 0, null);
            byte[] data = ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    if(data[i + width * (height - 1 - j)] == 0) {
                        missingBits[i][j] = true;
                    } else {
                        missingBits[i][j] = false;
                    }
                }
            }
        }
    }

    public GlobalPlottingParams getGlobalParams() {
        return new GlobalPlottingParams(xAxis.size(), yAxis.size(), domain.getCoordinateExtent(), null, null, null, null);
    }

    public Id2FeatureAndMember getId2FeatureAndMember(String xFieldName, String yFieldName) {
        final FeatureCollection<GridFeature> featureCollection = getFeatureCollection(xFieldName, yFieldName);
        return new Id2FeatureAndMember() {
            @Override
            public FeatureCollectionAndMemberName getFeatureAndMemberName(String id) {
                return new FeatureCollectionAndMemberName(featureCollection, id);
            }
        };
    }

    private FeatureCollection<GridFeature> getFeatureCollection(String xFieldName, String yFieldName) {
        ArrayList<NameAndRange> dataRangesList = new ArrayList<NameAndRange>(dataFields);

        Map<String, GridValuesMatrix<Float>> gvms = new HashMap<String, GridValuesMatrix<Float>>();

        for (NameAndRange nameAndRange : dataRangesList) {
            if(nameAndRange == null) {
                continue;
            }
            GridValuesMatrix<Float> gridValuesMatrix;
            if (nameAndRange.getFieldLabel().equals(xFieldName)) {
                gridValuesMatrix = new XYOrNullGridValuesMatrix(MatrixType.X, nameAndRange.getScaleRange());
            } else if (nameAndRange.getFieldLabel().equals(yFieldName)) {
                gridValuesMatrix = new XYOrNullGridValuesMatrix(MatrixType.Y, nameAndRange.getScaleRange());
            } else {
                gridValuesMatrix = new XYOrNullGridValuesMatrix(MatrixType.NAN, null);
            }
            gvms.put(nameAndRange.getFieldLabel(), gridValuesMatrix);
        }
        
        LegendFeatureCollection legendFeatureCollection = new LegendFeatureCollection(gvms);
        return legendFeatureCollection;
    }

    private class LegendFeatureCollection extends FeatureCollectionImpl<GridFeature> {
        private LegendFeatureCollection(Map<String, GridValuesMatrix<Float>> members) {
            super("", "");

            GridCoverage2DImpl coverage = new GridCoverage2DImpl("", domain,
                    DataReadingStrategy.PIXEL_BY_PIXEL);

            for (Entry<String, GridValuesMatrix<Float>> entry : members.entrySet()) {
                coverage.addMember(entry.getKey(), domain, "", null, null, entry.getValue());
                GridFeature f = new GridFeatureImpl(entry.getKey(), entry.getKey(), null, coverage);
                addFeature(f);
            }
        }
    }
    
    private enum MatrixType {
        X, Y, NAN
    };
    private class XYOrNullGridValuesMatrix extends InMemoryGridValuesMatrix<Float> {
        
        private MatrixType type;
        private Extent<Float> scaleRange = null;
        
        public XYOrNullGridValuesMatrix(MatrixType type, Extent<Float> scaleRange) {
            this.type = type;
            /*
             * Expand scale range to include out-of-range data
             */
            if(scaleRange != null) {
                Float width = scaleRange.getHigh() - scaleRange.getLow();
                this.scaleRange = Extents.newExtent(scaleRange.getLow() - width * fractionExtra, scaleRange.getHigh() + width * fractionExtra);
            }
        }
        
        @Override
        public Class<Float> getValueType() {
            return Float.class;
        }

        @Override
        public int getNDim() {
            return 2;
        }

        @Override
        protected GridAxis doGetAxis(int n) {
            if(n==0) return xAxis;
            if(n==1) return yAxis;
            throw new IllegalArgumentException("Only 2 dimensions in this GridValuesMatrix");
        }

        @Override
        protected Float doReadPoint(int[] coords) {
            if(missingBits[coords[0]][coords[1]]) {
                return Float.NaN;
            }
            switch (type) {
            case X:
                return scaleRange.getLow() + coords[0]
                        * (scaleRange.getHigh() - scaleRange.getLow()) / xAxis.size();
            case Y:
                return scaleRange.getLow() + coords[1]
                        * (scaleRange.getHigh() - scaleRange.getLow()) / yAxis.size();
            case NAN:
            default:
                return Float.NaN;
            }
        }
        
    }
}
