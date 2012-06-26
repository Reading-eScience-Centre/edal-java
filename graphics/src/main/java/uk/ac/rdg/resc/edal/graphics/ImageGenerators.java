package uk.ac.rdg.resc.edal.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.feature.GridFeature;
import uk.ac.rdg.resc.edal.util.BigList;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Extents;

public class ImageGenerators {

    public static BufferedImage plotFeature(final GridFeature feature, String memberName,
            MapStyleDescriptor style) {
        return plotGrid(feature.getCoverage().getGridValues(memberName), style);
    }

    public static BufferedImage plotGrid(final GridValuesMatrix<?> gridVals, MapStyleDescriptor style) {
        
        int width = gridVals.getAxis(0).size();
        int height = gridVals.getAxis(1).size();
        
        return plotGrid(gridVals, style, width, height);
    }
    
    public static BufferedImage plotGrid(final GridValuesMatrix<?> gridVals, MapStyleDescriptor style, int width, int height) {
        Class<?> clazz = gridVals.getValueType();
        if (!Number.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException(
                    "Can only add frames from GridValuesMatrix objects which contain numbers");
        }
        
        /*
         * This method is generic, and cannot use auto-scaling. Any auto-scaling
         * should be done at an earlier point (when setting the
         * MapStyleDescriptor)
         */
        if(style.isAutoScale()){
            @SuppressWarnings("unchecked")
            BigList<Number> values = (BigList<Number>) gridVals.getValues();
            style.setScaleRange(Extents.newExtent(
                    new Float(CollectionUtils.minIgnoringNullsAndNans(values)), new Float(
                            CollectionUtils.maxIgnoringNullsAndNans(values))));
        }
        
        byte[] pixels = new byte[width * height];

        for (int i = 0; i < pixels.length; i++) {
            /*
             * The image coordinate system has the vertical axis increasing
             * downward, but the data's coordinate system has the vertical axis
             * increasing upwards. The method below flips the axis
             */
            int dataIndex = getDataIndex(i, width, height);
            Number num = (Number) gridVals.getValues().get(dataIndex);
            if(num != null && (num.equals(Float.NaN) || num.equals(Double.NaN))){
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
