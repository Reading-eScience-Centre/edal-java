package uk.ac.rdg.resc.edal.graphics.style.sld;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.rdg.resc.edal.metadata.Parameter.Category;
import uk.ac.rdg.resc.edal.util.GraphicsUtils;

public class ColorSLDMapFunction extends AbstractSLDMapFunction<Color> {

    public static void main(String[] args) throws FileNotFoundException, SLDException {
        StyleSLDParser.createImage(new File(
                "/home/guy/Code/edal-java/wms/src/main/resources/example_styles/rastermap.xml"));
    }

    private Map<Integer, Category> categories;

    public ColorSLDMapFunction(XPath xPath, Node function) throws SLDException {
        super(xPath, function);
        try {
            // get the fallback value
            this.fallbackValue = parseColorFallbackValue();

            // get list of colours
            NodeList colourNodes = parseValues();

            // transform to list of Color objects
            valueMap = new HashMap<>();
            categories = new HashMap<>();
            for (int j = 0; j < colourNodes.getLength(); j++) {
                Node colourNode = colourNodes.item(j);
                String mapValue = xPath.evaluate("./@dataValue", colourNode);
                String catLabel = xPath.evaluate("./@dataLabel", colourNode);
                if (!mapValue.isEmpty()) {
                    valueMap.put(Integer.parseInt(mapValue),
                            GraphicsUtils.parseColour(colourNode.getTextContent()));
                    if (catLabel == null || catLabel.isEmpty()) {
                        catLabel = "No label";
                    }
                    categories.put(
                            Integer.parseInt(mapValue),
                            new Category(catLabel, GraphicsUtils.parseColour(colourNode
                                    .getTextContent()), catLabel));
                } else {
                    throw new SLDException(
                            "For a int-colour map, each element must contain the attribute \"dataValue\"");
                }
            }
        } catch (Exception e) {
            throw new SLDException(e);
        }
    }

    public Map<Integer, Category> getCategories() {
        return categories;
    }
}
