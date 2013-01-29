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
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ArrowData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ColourScheme1DData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.ImageData;
import uk.ac.rdg.resc.edal.graphics.style.datamodel.model.RasterData;
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

    public static String serialise(ImageData image) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ImageData.class);

        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(image, stringWriter);

        return stringWriter.toString();
    }

    public static ImageData deserialise(String xmlString) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ImageData.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        ImageData image = (ImageData) unmarshaller.unmarshal(new StringReader(xmlString));

        return image;
    }
    
    public static void generateSchema(final String path) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(ImageData.class);
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
        ColourScheme1DData colourScheme = new ColourScheme1DData(scaleRange, null, Color.BLUE, new Color(0,
                0, 0, 0), "redblue", 100f, 254, false);
        RasterData magPlotter = new RasterData("UV_MAG", colourScheme);

        ArrowData dirPlotter = new ArrowData("UV_DIR", 15, Color.decode("#100000"));

        ImageData image = new ImageData();
        image.getLayers().add(magPlotter);
        image.getLayers().add(dirPlotter);

        System.out.println(StyleXMLParser.serialise(image));
    }
}
