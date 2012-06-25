package uk.ac.rdg.resc.edal.coverage.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.PixelMapEntry;
import uk.ac.rdg.resc.edal.coverage.impl.PixelMap.Scanline;

/**
 * Strategy for reading data from a {@link GridValuesMatrix}.
 * 
 * @author Jon
 * @author Guy Griffiths
 */
public enum DataReadingStrategy {

    PIXEL_BY_PIXEL {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            for (PixelMapEntry pme : pixelMap) {
                Object value = gridValues.readPoint(new int[] { pme.getSourceGridIIndex(),
                        pme.getSourceGridJIndex() });
                for (int index : pme.getTargetGridPoints()) {
                    values.set(index, value);
                }
            }
        }
    },

    SCANLINE {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            Iterator<Scanline> it = pixelMap.scanlineIterator();
            while (it.hasNext()) {
                Scanline scanline = it.next();
                List<PixelMapEntry> entries = scanline.getPixelMapEntries();
                int entriesSize = entries.size();

                int j = scanline.getSourceGridJIndex();
                int imin = entries.get(0).getSourceGridIIndex();
                int imax = entries.get(entriesSize - 1).getSourceGridIIndex();

                GridValuesMatrix<?> block = gridValues.readBlock(new int[] { imin, j }, new int[] {
                        imax, j });

                for (PixelMapEntry pme : entries) {
                    int i = pme.getSourceGridIIndex() - imin;
                    Object val = block.readPoint(new int[] { i, 0 });
                    for (int p : pme.getTargetGridPoints()) {
                        values.set(p, val);
                    }
                }
                // This will probably do nothing, because the result of
                // readBlock()
                // will
                // be an in-memory structure.
                block.close();
            }
        }
    },

    BOUNDING_BOX {
        @Override
        protected void extractCoverageValues(GridValuesMatrix<?> gridValues, PixelMap pixelMap,
                List<Object> values) {
            int imin = pixelMap.getMinIIndex();
            int imax = pixelMap.getMaxIIndex();
            int jmin = pixelMap.getMinJIndex();
            int jmax = pixelMap.getMaxJIndex();

            GridValuesMatrix<?> block = gridValues.readBlock(new int[] { imin, jmin }, new int[] {
                    imax, jmax });

            for (PixelMapEntry pme : pixelMap) {
                int i = pme.getSourceGridIIndex() - imin;
                int j = pme.getSourceGridJIndex() - jmin;
                Object val = block.readPoint(new int[] { i, j });
                for (int targetGridPoint : pme.getTargetGridPoints()) {
                    values.set(targetGridPoint, val);
                }
            }
            /*
             * This will probably do nothing, because the result of readBlock()
             * will be an in-memory structure.
             */
            block.close();
        }
    };

    public List<Object> readValues(GridValuesMatrix<?> gridValuesMatrix,
            HorizontalGrid sourceDomain, HorizontalGrid targetDomain) {

        PixelMap pixelMap = PixelMap.forGrid(sourceDomain, targetDomain);

        List<Object> values = listOfNulls(pixelMap.getTargetDomainSize());

        extractCoverageValues(gridValuesMatrix, pixelMap, values);

        gridValuesMatrix.close();

        return values;
    }

    protected abstract void extractCoverageValues(GridValuesMatrix<?> gridValues,
            PixelMap pixelMap, List<Object> values);

    /**
     * Creates and returns a new mutable list consisting entirely of null
     * values. The values of the list can be altered through set(), but the size
     * of the list cannot be altered.
     * 
     * @param size
     *            The size of the list to create
     * @return a new mutable list consisting entirely of null values.
     */
    private static List<Object> listOfNulls(int size) {
        final Object[] arr = new Object[size];
        Arrays.fill(arr, null);
        return new AbstractList<Object>() {

            @Override
            public Object get(int index) {
                return arr[index];
            }

            @Override
            public Object set(int index, Object newValue) {
                Object oldValue = arr[index];
                arr[index] = newValue;
                return oldValue;
            }

            @Override
            public int size() {
                return arr.length;
            }

        };
    }
}
