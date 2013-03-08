package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.LegendDataGenerator;

@XmlType(namespace = Image.NAMESPACE, name = "ImageType")
@XmlRootElement(namespace = Image.NAMESPACE, name = "Image")
public class Image extends Drawable {
    /*
     * This is the namespace for the XML.
     * 
     * IF YOU CHANGE IT, YOU NEED TO MODIFY pom.xml AS WELL
     */
    public static final String NAMESPACE = "http://www.resc.reading.ac.uk";

    @XmlElements({ @XmlElement(name = "Image", type = Image.class),
            @XmlElement(name = "ArrowLayer", type = ArrowLayer.class),
            @XmlElement(name = "RasterLayer", type = RasterLayer.class),
            @XmlElement(name = "StippleLayer", type = StippleLayer.class),
            @XmlElement(name = "ContourLayer", type = ContourLayer.class),
            @XmlElement(name = "BasicGlyphLayer", type = BasicGlyphLayer.class),
            @XmlElement(name = "SubsampledGlyphLayer", type = SubsampledGlyphLayer.class) })
    private List<Drawable> layers = new ArrayList<Drawable>();

    public List<Drawable> getLayers() {
        return layers;
    }

    @Override
    public BufferedImage drawImage(GlobalPlottingParams params, Id2FeatureAndMember id2Feature) {
        BufferedImage finalImage = new BufferedImage(params.getWidth(), params.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = finalImage.createGraphics();

        for (Drawable drawable : layers) {
            if (drawable != null) {
                BufferedImage drawnImage = drawable.drawImage(params, id2Feature);
                OpacityTransform opacityTransform = drawable.getOpacityTransform();
                if (opacityTransform != null) {
                    opacityTransform.drawIntoImage(drawnImage, params, id2Feature);
                }
                graphics.drawImage(drawnImage, 0, 0, null);
            }
        }
        if(getOpacityTransform() != null) {
            getOpacityTransform().drawIntoImage(finalImage, params, id2Feature);
        }
        return finalImage;
    }

    /**
     * Generate a legend for this {@link Image}.
     * 
     * @param componentSize
     *            A single integer specifying the size of each component of the
     *            legend. The final image size will depend upon this number as
     *            well as the number of unique data fields which this
     *            {@link Image} depends upon
     * @return An {@link BufferedImage} representing the legend for this
     *         {@link Image}
     */
    private static final int COLOURBAR_WIDTH = 50; 
    public BufferedImage getLegend(int componentSize) {
        /*
         * TODO componentSize doesn't actually specify the total size of a
         * component, just the data bit of it. We *may* want to fix this
         */
        BufferedImage finalImage;
        Set<NameAndRange> fieldsWithScales = getFieldsWithScales();
        int noOfIndependentFields = fieldsWithScales.size();
        Font textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        int BORDER = 4;
        if(noOfIndependentFields == 0) {
            /*
             * TODO Return an empty image - we have no data fields
             */
            return null;
        } else if(noOfIndependentFields == 1) {
            /*
             * This is the fraction of the colourbar which *gets added* as
             * out-of-range data.
             * 
             * i.e. if it's 1, the result would be 1/3 below min, 1/3 in range,
             * 1/3 above max.
             */
            float extraAmountOutOfRange = 0.1f;
            
            /*
             * Get the field name and scale range.
             */
            NameAndRange nameAndRange = fieldsWithScales.iterator().next();
            String fieldName = nameAndRange.getFieldLabel();
            // TODO Units!
            String lowStr = nameAndRange.getScaleRange().getLow() + "";
            String highStr = nameAndRange.getScaleRange().getHigh() + "00000";
            
            /*
             * Create a temporary image so that we can get some metrics about
             * the font. We can use these to determine the size of the final
             * image.
             */
            BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D graphics = tempImage.createGraphics();
            FontMetrics fontMetrics = graphics.getFontMetrics(textFont);
            /*
             * The height of a line of text
             */
            int lineHeight = fontMetrics.getHeight();
            /*
             * This is how much of an offset we need so that the high/low scale
             * labels are in the right place
             */
            int outOfRangeOffset = (int) (componentSize * extraAmountOutOfRange / (1 + 2 * extraAmountOutOfRange));
            /*
             * The length required to write the field name
             */
            int fieldLength = fontMetrics.stringWidth(fieldName);
            /*
             * Number of lines of text needed for field name 
             */
            int nLines = (int) Math.ceil((double)fieldLength / componentSize);
            System.out.println(nLines+","+fieldLength+","+componentSize);
            if(nLines > 1) {
                /*
                 * It needs splitting.
                 */
                int charsPerLine = 1 + fieldName.length() / nLines;
                StringBuilder newFieldName = new StringBuilder();
                for(int i = 0; i < nLines; i++) {
                    System.out.println(i * charsPerLine+","+ (i+1) * charsPerLine+","+fieldName.length());
                    /*
                     * Hyphenate
                     */
                    if(i == nLines - 1) {
                        newFieldName.append(fieldName.substring(i * charsPerLine));
                    } else {
                        newFieldName.append(fieldName.substring(i * charsPerLine, (i+1) * charsPerLine)+"-\n-");
                    }
                }
                fieldName = newFieldName.toString();
            }
            
            /*
             * Space needed for labels
             */
            int numberSpace = fontMetrics.stringWidth(lowStr);
            if(fontMetrics.stringWidth(highStr) > numberSpace) {
                numberSpace = fontMetrics.stringWidth(highStr);
            }
            /*
             * Total space needed for all text 
             */
            int sideSpace = numberSpace + lineHeight * nLines + 2*BORDER;
            // Dispose of the unused graphics context.
            graphics.dispose();
            
            /*
             * Create the final image with enough space
             */
            finalImage = new BufferedImage(COLOURBAR_WIDTH + sideSpace, componentSize, BufferedImage.TYPE_INT_ARGB);
            
            /*
             * Get the data for the colourbar and draw it. 
             */
            LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales, COLOURBAR_WIDTH, componentSize, null, extraAmountOutOfRange);
            BufferedImage colourbar = drawImage(dataGenerator.getGlobalParams(), dataGenerator.getId2FeatureAndMember(null, nameAndRange.getFieldLabel()));
            graphics = finalImage.createGraphics();
            graphics.setColor(Color.white);
            graphics.fill(new Rectangle(finalImage.getWidth(), finalImage.getHeight()));
            graphics.drawImage(colourbar, 0, 0, null);
            
            graphics.setColor(Color.black);
            graphics.setFont(textFont);
            /*
             * Offset due to drawString specifying the base of the text
             */
            int textHeightOffset = lineHeight / 3;
            graphics.drawString(highStr, COLOURBAR_WIDTH + BORDER, outOfRangeOffset + textHeightOffset);
            graphics.drawString(lowStr, COLOURBAR_WIDTH + BORDER, componentSize - outOfRangeOffset + textHeightOffset);
            
            AffineTransform at = new AffineTransform();
            at.rotate(-Math.PI/2.0);
            Font sidewaysFont = textFont.deriveFont(at);
            graphics.setFont(sidewaysFont);
            
            int offset = 0;
            for (String line : fieldName.split("\n")) {
                graphics.drawString(line, COLOURBAR_WIDTH + BORDER + numberSpace + lineHeight + offset, componentSize - BORDER);
                offset += lineHeight;
            }
        } else {
            /*
             * TODO general case.  We need to generate every possible combination of 2D diagram.
             */
            int numberOfImagesInOneDirection = noOfIndependentFields - 1;
            int borderPerImageSide = 50;
            int totalImageSize = (componentSize + 2 * borderPerImageSide) * numberOfImagesInOneDirection;
            
            finalImage = new BufferedImage(totalImageSize, totalImageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = finalImage.createGraphics();
            BufferedImage bg = null;
            BufferedImage bgMask = null;
            try {
                bg = ImageIO.read(Image.class.getResource("/img/map_bg_200.png"));
                bgMask = ImageIO.read(Image.class.getResource("/img/map_bg_200_mask.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            List<NameAndRange> fields = new ArrayList<Drawable.NameAndRange>(fieldsWithScales);
            
            for(int i = 0; i < fields.size(); i++) {
                String yName = fields.get(i).getFieldLabel();
                int yStart = (i * (componentSize + 2 * borderPerImageSide)) + borderPerImageSide;
                for(int j = i + 1; j < fields.size(); j++) {
                    int xStart = ((j-i-1) * (componentSize + 2 * borderPerImageSide)) + borderPerImageSide;
                    String xName = fields.get(j).getFieldLabel();
                    LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales, componentSize, componentSize, bgMask, 10);
                    BufferedImage colourbar2d = drawImage(dataGenerator.getGlobalParams(), dataGenerator.getId2FeatureAndMember(xName, yName));
                    if(bg != null) {
                        graphics.drawImage(bg, xStart, yStart, componentSize, componentSize, null);
                    }
                    graphics.drawImage(colourbar2d, xStart, yStart, null);
                    /*
                     * TODO add labels + units etc.
                     */
                }                
            }
        }
        return finalImage;
    }
    
    @Override
    protected Set<NameAndRange> getFieldsWithScales() {
        Set<NameAndRange> ret = new LinkedHashSet<Drawable.NameAndRange>();
        for (Drawable drawable : layers) {
            if (drawable != null) {
                Set<NameAndRange> fieldsWithScales = drawable.getFieldsWithScales();
                if (drawable.getOpacityTransform() != null) {
                    fieldsWithScales.addAll(drawable.getOpacityTransform().getFieldsWithScales());
                }
                if (fieldsWithScales != null) {
                    ret.addAll(fieldsWithScales);
                }
            }
        }
        if(getOpacityTransform() != null) {
            ret.addAll(getOpacityTransform().getFieldsWithScales());
        }
        return ret;
    }
    
    public static void main(String[] args) throws IOException {
        Image image = new Image();
        Drawable layer = new RasterLayer("temp", new ColourScheme(new ColourScale(-5.0f,
                5.0f, false), new ColourMap(Color.black, Color.black, new Color(0, true),
                "default", 250)));
//        Drawable raster2 = new RasterLayer("raster2", new ColourScheme(new ColourScale(-5.0f,
//                5.0f, false), new ColourMap(Color.black, Color.black, new Color(0, true),
//                        "redblue", 250)));
//        layer.setOpacityTransform(new FlatOpacity(0.5f));
        
        ArrowLayer layer2 = new ArrowLayer("test2", 10, Color.BLACK);
//        ContourLayer layer3 = new ContourLayer("test3", new ColourScale(0f, 50f, false), false, 10, Color.blue, 2, null, true);
        StippleLayer layer3 = new StippleLayer("test3", new PatternScale(8, 0f, 1f, false));
        image.getLayers().add(layer);
//        image.getLayers().add(raster2);
//        image.getLayers().add(layer2);
//        image.getLayers().add(layer3);
//        image.setOpacityTransform(new LinearOpacity("test4", 0f, 1f));
        ImageIO.write(image.getLegend(200), "png", new File("/home/guy/legendtest.png"));
    }
}
