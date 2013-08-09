package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.graphics.style.util.Id2FeatureAndMember;
import uk.ac.rdg.resc.edal.graphics.style.util.LegendDataGenerator;

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
            @XmlElement(name = "Raster2DLayer", type = Raster2DLayer.class),
            @XmlElement(name = "StippleLayer", type = StippleLayer.class),
            @XmlElement(name = "ContourLayer", type = ContourLayer.class),
            @XmlElement(name = "BasicGlyphLayer", type = BasicGlyphLayer.class),
            @XmlElement(name = "SubsampledGlyphLayer", type = SubsampledGlyphLayer.class),
            @XmlElement(name = "ConfidenceIntervalLayer", type = ConfidenceIntervalLayer.class)})
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
        Color bgColour = Color.black;
        Color fgColour = Color.white;
        /*
         * This is the fraction of the colourbar which *gets added* as
         * out-of-range data.
         * 
         * i.e. if it's 1, the result would be 1/3 below min, 1/3 in range,
         * 1/3 above max.
         */
        float extraAmountOutOfRange = 0.1f;
        
        if(noOfIndependentFields == 0) {
            /*
             * TODO Return an empty image - we have no data fields
             */
            return null;
        } else if(noOfIndependentFields == 1) {
            /*
             * Case where we have a 1D colour bar
             */
            
            /*
             * Get the field name and scale range.
             */
            NameAndRange nameAndRange = fieldsWithScales.iterator().next();

            /*
             * Get the data for the colourbar and draw it. 
             */
            LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales, COLOURBAR_WIDTH, componentSize, null, extraAmountOutOfRange);
            BufferedImage colourbar = drawImage(dataGenerator.getGlobalParams(), dataGenerator.getId2FeatureAndMember(null, nameAndRange.getFieldLabel()));
            Graphics2D graphics = colourbar.createGraphics();
            graphics.setColor(fgColour);
            graphics.drawRect(0, 0, colourbar.getWidth() - 1, colourbar.getHeight() - 1);
            graphics.dispose();

            /*
             * Now generate the labels for this legend
             */
            BufferedImage labels = getLabels(nameAndRange, extraAmountOutOfRange, componentSize, fgColour);
            
            /*
             * Now create the correctly-sized final image...
             */
            finalImage = new BufferedImage(COLOURBAR_WIDTH + labels.getWidth(), componentSize, BufferedImage.TYPE_INT_ARGB);
            /*
             * ...and draw everything into it
             */
            graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fill(new Rectangle(finalImage.getWidth(), finalImage.getHeight()));
            graphics.drawImage(colourbar, 0, 0, null);
            graphics.drawImage(labels, COLOURBAR_WIDTH, 0, null);
        } else {
            /*
             * General case, where we need to generate each possible combination
             * of 2D legends.
             */
            int numberOfImagesInOneDirection = noOfIndependentFields - 1;
            List<NameAndRange> fields = new ArrayList<Drawable.NameAndRange>(fieldsWithScales);

            /*
             * Before we can start this process, we need to calculate how much
             * room to leave for the labels. We can do this by generating all
             * the labels up front
             */
            BufferedImage[] labels = new BufferedImage[fields.size()];
            int borderSize = 0;
            for(int i = 0; i < fields.size(); i++) {
                labels[i] = getLabels(fields.get(i), extraAmountOutOfRange, componentSize, fgColour);
                if(labels[i].getWidth() > borderSize) {
                    borderSize = labels[i].getWidth() + 8; 
                }
            }
            
            int totalImageSize = (componentSize + borderSize) * numberOfImagesInOneDirection;
            
            finalImage = new BufferedImage(totalImageSize, totalImageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = finalImage.createGraphics();
            graphics.setColor(bgColour);
            graphics.fillRect(0, 0, totalImageSize, totalImageSize);
            
            /*
             * Load the background image and the data mask
             */
            BufferedImage bg = null;
            BufferedImage bgMask = null;
            try {
                bg = ImageIO.read(Image.class.getResource("/img/map_bg_200.png"));
                bgMask = ImageIO.read(Image.class.getResource("/img/map_bg_200_mask.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < fields.size(); i++) {
                String yName = fields.get(i).getFieldLabel();
                int yStart = 2+ (i * (componentSize + borderSize));
                for(int j = i + 1; j < fields.size(); j++) {
                    int xStart = 2 +((j-i-1) * (componentSize + borderSize));
                    String xName = fields.get(j).getFieldLabel();
                    LegendDataGenerator dataGenerator = new LegendDataGenerator(fieldsWithScales, componentSize, componentSize, bgMask, 0.1f);
                    BufferedImage colourbar2d = drawImage(dataGenerator.getGlobalParams(), dataGenerator.getId2FeatureAndMember(xName, yName));
                    if(bg != null) {
                        graphics.drawImage(bg, xStart, yStart, componentSize, componentSize, null);
                    }
                    /*
                     * Draw the 2d legend and outline it
                     */
                    graphics.drawImage(colourbar2d, xStart, yStart, null);
                    graphics.setColor(fgColour);
                    graphics.drawRect(xStart, yStart, colourbar2d.getWidth() - 1, colourbar2d.getHeight() - 1);
                    graphics.drawRect(xStart - 2, yStart - 2, borderSize + colourbar2d.getWidth() - 2, borderSize + colourbar2d.getHeight() - 2);
                    graphics.drawRect(xStart - 1, yStart - 1, borderSize + colourbar2d.getWidth() - 2, borderSize + colourbar2d.getHeight() - 2);
                    /*
                     * Now draw the labels
                     */
                    AffineTransform at = new AffineTransform();
                    at.translate(xStart + componentSize, yStart + componentSize);
                    at.rotate(Math.PI/2);
                    graphics.drawImage(labels[j], at, null);
                    graphics.drawImage(labels[i], xStart + componentSize, yStart, null);
                }                
            }
        }
        return finalImage;
    }
    
    /**
     * This returns an image suitable for plotting next to a vertical colourbar.
     * Rotate it if required.
     * 
     * @param nameAndRange
     * @param extraAmountOutOfRange
     * @param componentSize
     * @return
     */
    private static BufferedImage getLabels(NameAndRange nameAndRange, float extraAmountOutOfRange, int componentSize, Color textColor) {
        String fieldName = nameAndRange.getFieldLabel();
        
        Font textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        AffineTransform at = new AffineTransform();
        at.rotate(-Math.PI/2.0);
        Font sidewaysFont = textFont.deriveFont(at);
        DecimalFormat formatter = new DecimalFormat("0.###E0");
        
        int textBorder = 4;
        
        Float lowVal = nameAndRange.getScaleRange().getLow();
        Float highVal = nameAndRange.getScaleRange().getHigh();
        String lowStr = formatter.format(lowVal);;
        String medLowStr = formatter.format(lowVal + (highVal - lowVal) / 3.0);
        String medHighStr = formatter.format(lowVal + 2.0 * (highVal - lowVal) / 3.0);
        String highStr = formatter.format(highVal);
        
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
         * The offset needed to account for the fact that the position of
         * text refers to the position of the baseline, not the centre
         */
        int textHeightOffset = lineHeight / 3;
        /*
         * This is how much of an offset we need so that the high/low scale
         * labels are in the right place
         */
        int outOfRangeOffset = (int) (componentSize * extraAmountOutOfRange / (1 + 2 * extraAmountOutOfRange));
        
        int lowYPos = componentSize - outOfRangeOffset + textHeightOffset;
        int highYPos = outOfRangeOffset + textHeightOffset;
        int medLowYPos = (int) (highYPos + 2.0 * (lowYPos - highYPos) / 3.0);
        int medHighYPos = (int) (highYPos + 1.0 * (lowYPos - highYPos) / 3.0);
        /*
         * The length required to write the field name
         */
        int fieldLength = fontMetrics.stringWidth(fieldName);
        /*
         * Number of lines of text needed for field name. The 20 is in there to
         * get around the fact that characters don't take up equal space. It's
         * an empirical value. Feel free to empiricise it more.
         */
        int nLines = (int) Math.ceil((double)(fieldLength + 20) / componentSize);
        if(nLines > 1) {
            /*
             * It needs splitting.
             */
            int charsPerLine = 1 + fieldName.length() / nLines;
            StringBuilder newFieldName = new StringBuilder();
            for(int i = 0; i < nLines; i++) {
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
        if(fontMetrics.stringWidth(medLowStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(medLowStr);
        }
        if(fontMetrics.stringWidth(medHighStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(medHighStr);
        }
        if(fontMetrics.stringWidth(highStr) > numberSpace) {
            numberSpace = fontMetrics.stringWidth(highStr);
        }
        /*
         * Total space needed for all text 
         */
        int sideSpace = numberSpace + lineHeight * nLines + 2*textBorder;
        // Dispose of the unused graphics context.
        graphics.dispose();
        
        BufferedImage ret = new BufferedImage(sideSpace, componentSize, BufferedImage.TYPE_INT_ARGB);
        graphics = ret.createGraphics();
        /*
         * Now draw text for the scale limits
         */
        graphics.setColor(textColor);
        graphics.setFont(textFont);
        graphics.drawString(highStr, textBorder, highYPos);
        graphics.drawString(medHighStr, textBorder, medHighYPos);
        graphics.drawString(medLowStr, textBorder, medLowYPos);
        graphics.drawString(lowStr, textBorder, lowYPos);
        
        graphics.setFont(sidewaysFont);
        
        int offset = 0;
        for (String line : fieldName.split("\n")) {
            graphics.drawString(line, textBorder + numberSpace + lineHeight + offset, componentSize - textBorder);
            offset += lineHeight;
        }
        
        return ret;
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
        Drawable layer = new RasterLayer("colouredness", new PaletteColourScheme(new ColourScale(-00.0000000050f,
                5.0f, false), new ColourMap(Color.black, Color.black, new Color(0, true),
                "default", 250)));
//        Drawable raster2 = new RasterLayer("raster2", new ColourScheme(new ColourScale(-5.0f,
//                5.0f, false), new ColourMap(Color.black, Color.black, new Color(0, true),
//                        "redblue", 250)));
//        layer.setOpacityTransform(new FlatOpacity(0.5f));
        
        ArrowLayer layer2 = new ArrowLayer("pointiness", 10, Color.BLACK);
//        ContourLayer layer3 = new ContourLayer("test3", new ColourScale(0f, 50f, false), false, 10, Color.blue, 2, null, true);
        StippleLayer layer3 = new StippleLayer("stippliness", new PatternScale(8, 0f, 1f, false));
        image.getLayers().add(layer);
//        image.getLayers().add(raster2);
        image.getLayers().add(layer2);
        image.getLayers().add(layer3);
        image.setOpacityTransform(new LinearOpacity("seethoughness", 0f, 1f));
        ImageIO.write(image.getLegend(200), "png", new File("/home/guy/legendtest.png"));
    }
}
