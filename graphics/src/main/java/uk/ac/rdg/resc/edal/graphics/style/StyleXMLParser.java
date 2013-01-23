package uk.ac.rdg.resc.edal.graphics.style;

import java.awt.Color;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.rdg.resc.edal.Extent;
import uk.ac.rdg.resc.edal.util.Extents;

public class StyleXMLParser {
    static class ColorAdapter extends XmlAdapter<String, Color> {
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

    public static void main(String[] args) throws JAXBException {
        Extent<Float> scaleRange = Extents.newExtent(270.0f, 310.0f);
        ColourScheme1D colourScheme = new ColourScheme1D(scaleRange, null, Color.BLUE, new Color(0,
                0, 0, 0), "redblue", 100, 254, false);
        RasterPlotter magPlotter = new RasterPlotter();
        magPlotter.setColourScheme(colourScheme);

        ArrowPlotter dirPlotter = new ArrowPlotter();
        dirPlotter.setArrowColor(Color.red);
        dirPlotter.setArrowSize(10);

        ImageLayer magLayer = new ImageLayer(magPlotter, "UV_MAG");
        ImageLayer dirLayer = new ImageLayer(dirPlotter, "UV_DIR");

        Image image = new Image();
        image.addLayer(magLayer);
        image.addLayer(dirLayer);

        System.out.println(StyleXMLParser.serialise(image));
    }
}
