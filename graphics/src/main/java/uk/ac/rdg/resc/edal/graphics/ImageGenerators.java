package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;

public class ImageGenerators {
    public static BufferedImage plotGrid(final GridValuesMatrix<?> gridVals, MapStyleDescriptor style) {
        
        int width = gridVals.getXAxis().size();
        int height = gridVals.getYAxis().size();
        
        return plotGrid(gridVals, style, width, height);
    }
    
    public static BufferedImage plotGrid(final GridValuesMatrix<?> gridVals, MapStyleDescriptor style, int width, int height) {
        Class<?> clazz = gridVals.getValueType();
        if (!Number.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException(
                    "Can only add frames from GridValuesMatrix objects which contain numbers");
        }
        
        byte[] pixels = new byte[width * height];
        /*
         * TODO this is painfully slow for NetCDF files. Currently, getValues
         * returns an instance of AbstractDiskBackedGridCoverage2D, and getAll()
         * delegates to GridDataSource.readPoint for each point. This then uses
         * readBlock in NcGridDataSource for a 1x1 block (for every point)...
         * 
         * If we replace the line 
         * 
         * Number num = (Number) values.get(dataIndex);
         * 
         * with the (more logical?)
         * 
         * Number num = (Number) gridVals.getValues().get(dataIndex);
         * 
         * we get the same situation, but more explicitly...
         */
        List<?> values = gridVals.getValues().getAll(0L, gridVals.size());
        for (int i = 0; i < pixels.length; i++) {
            /*
             * The image coordinate system has the vertical axis increasing
             * downward, but the data's coordinate system has the vertical axis
             * increasing upwards. The method below flips the axis
             */
            int dataIndex = getDataIndex(i, width, height);
//            Number num = (Number) gridVals.getValues().get(dataIndex);
            Number num = (Number) values.get(dataIndex);
            if(num.equals(Float.NaN) || num.equals(Double.NaN)){
                num = null;
            }
            pixels[i] = (byte) style.getColourIndex(num);
        }

        // Create a ColorModel for the image
        ColorModel colorModel = style.getColorModel();

        // Create the Image
        DataBuffer buf = new DataBufferByte(pixels, pixels.length);
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buf, null);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        return image;
    }
    
    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    private static int getDataIndex(int imageIndex, int width, int height) {
        int imageI = imageIndex % width;
        int imageJ = imageIndex / width;
        return getDataIndex(imageI, imageJ, width, height);
    }

    /**
     * Calculates the index of the data point in a data array that corresponds
     * with the given index in the image array, taking into account that the
     * vertical axis is flipped.
     */
    private static int getDataIndex(int imageI, int imageJ, int width, int height) {
        int dataJ = height - imageJ - 1;
        int dataIndex = dataJ * width + imageI;
        return dataIndex;
    }
}
