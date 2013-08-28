package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ColourScheme2D;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Image;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.Raster2DLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.RasterLayer;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.impl.ThresholdColourScheme2D;

public class StyleXMLParser {
    public static class ColorAdapter extends XmlAdapter<String, Color> {
        @Override
        public Color unmarshal(String s) {
            if (s.length() == 7) {
                return Color.decode(s);
            } else if (s.length() == 9) {
                Color color = Color.decode("#"+s.substring(3));
                int alpha = Integer.parseInt(s.substring(1,3), 16);
                return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            } else {
                return null;
            }
        }

        @Override
        public String marshal(Color c) {
            return "#" + String.format("%08X", c.getRGB());
        }
    }
    
    public static class FlatOpacityAdapter extends XmlAdapter<String, FlatOpacity> {
        @Override
        public FlatOpacity unmarshal(String v) throws Exception {
            return new FlatOpacity(Float.parseFloat(v));
        }

        @Override
        public String marshal(FlatOpacity v) throws Exception {
            return v.getOpacity().toString();
        }
    }

    public static String serialise(Image image) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Image.class);

        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(image, stringWriter);

        return stringWriter.toString();
    }

    public static Image deserialise(String xmlString) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Image.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        Image image = (Image) unmarshaller.unmarshal(new StringReader(xmlString));

        return image;
    }
    
    public static void generateSchema(final String path) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(Image.class);
        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                System.out.println(namespaceUri+", "+suggestedFileName);
                return new StreamResult(new File(path,suggestedFileName));
            }
        });
    }
    
    public static void main(String[] args) throws JAXBException, IOException {
//        generateSchema("/home/guy/");
        
        Image image = new Image();
//        StippleLayer layer = new StippleLayer("foam/TMP", new PatternScale(5, 305f, 280f, false));
//        image.getLayers().add(layer);
//        System.out.println(serialise(image));
//        
//        System.out.println();
//        
//        image = new Image();
//        ColourScale scaleRange = new ColourScale(270f, 302f, false);
//        ColourMap colourPalette = new ColourMap(Color.green, Color.cyan, new Color(0, 0, 0, 0), "rainbow", 20);
//        ColourScheme colourScheme = new PaletteColourScheme(scaleRange, colourPalette);
        
        ArrayList<Float> xThresholds = new ArrayList<Float>();
        xThresholds.add(276.7f);
        xThresholds.add(283.3f);
        xThresholds.add(290.0f);
        xThresholds.add(296.7f);
        xThresholds.add(303.3f);
        
        ArrayList<Float> yThresholds = new ArrayList<Float>();
        yThresholds.add(0.5f);
        yThresholds.add(1.0f);
        yThresholds.add(1.5f);
        yThresholds.add(2.0f);
        yThresholds.add(2.5f);
        
        ArrayList<Color> colours = new ArrayList<Color>();

        colours.add(Color.blue);
        colours.add(Color.cyan);
        colours.add(Color.green);
        colours.add(Color.yellow);
        colours.add(Color.orange);
        colours.add(Color.red);
        
        colours.add(new Color(0, 0, 255, 204));
        colours.add(new Color(0, 255, 255, 204));
        colours.add(new Color(0, 255, 0, 204));
        colours.add(new Color(255, 255, 0, 204));
        colours.add(new Color(255, 200, 0, 204));
        colours.add(new Color(255, 0, 0, 204));

        colours.add(new Color(0, 0, 255, 153));
        colours.add(new Color(0, 255, 255, 153));
        colours.add(new Color(0, 255, 0, 153));
        colours.add(new Color(255, 255, 0, 153));
        colours.add(new Color(255, 200, 0, 153));
        colours.add(new Color(255, 0, 0, 153));

        colours.add(new Color(0, 0, 255, 102));
        colours.add(new Color(0, 255, 255, 102));
        colours.add(new Color(0, 255, 0, 102));
        colours.add(new Color(255, 255, 0, 102));
        colours.add(new Color(255, 200, 0, 102));
        colours.add(new Color(255, 0, 0, 102));

        colours.add(new Color(0, 0, 255, 51));
        colours.add(new Color(0, 255, 255, 51));
        colours.add(new Color(0, 255, 0, 51));
        colours.add(new Color(255, 255, 0, 51));
        colours.add(new Color(255, 200, 0, 51));
        colours.add(new Color(255, 0, 0, 51));

        colours.add(new Color(0, 0, 255, 00));
        colours.add(new Color(0, 255, 255, 00));
        colours.add(new Color(0, 255, 0, 00));
        colours.add(new Color(255, 255, 0, 00));
        colours.add(new Color(255, 200, 0, 00));
        colours.add(new Color(255, 0, 0, 00));

        ColourScheme2D colourScheme = new ThresholdColourScheme2D(xThresholds, yThresholds, colours, new Color(0, 0, 0, 0));
        Raster2DLayer raster2DLayer = new Raster2DLayer("analysed_sst", "analysis_error", colourScheme);
//        rasterLayer.setOpacityTransform(new FlatOpacity(0.5f));
        image.getLayers().add(raster2DLayer);
        System.out.println(serialise(image));
    }
}
