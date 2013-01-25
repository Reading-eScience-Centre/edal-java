package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.graphics.style.model.ArrowPlotter;
import uk.ac.rdg.resc.edal.graphics.style.model.ColourScheme1D;
import uk.ac.rdg.resc.edal.graphics.style.model.Image;
import uk.ac.rdg.resc.edal.graphics.style.model.ImageLayer;
import uk.ac.rdg.resc.edal.graphics.style.model.RasterPlotter;
import uk.ac.rdg.resc.edal.util.Extents;

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
            return "#" + Integer.toHexString(c.getRGB());
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
        StyleXMLParser.generateSchema("/home/guy");
        
        Extent<Float> scaleRange = Extents.newExtent(270.0f, 310.0f);
        ColourScheme1D colourScheme = new ColourScheme1D(scaleRange, null, Color.BLUE, new Color(0,
                0, 0, 0), "redblue", 100, 254, false);
        RasterPlotter magPlotter = new RasterPlotter();
        magPlotter.setColourScheme(colourScheme);
        magPlotter.setDataLayerId("UV_MAG");

        ArrowPlotter dirPlotter = new ArrowPlotter();
        dirPlotter.setArrowColor(Color.decode("#100000"));
        dirPlotter.setArrowSize(15);
        dirPlotter.setDataLayerId("UV_DIR");

        Image image = new Image();
//        image.addLayer(magPlotter);
        image.addLayer(dirPlotter);

        System.out.println(StyleXMLParser.serialise(image));
    }
}
