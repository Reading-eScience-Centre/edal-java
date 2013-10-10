/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.graphics.style.util;

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

import uk.ac.rdg.resc.edal.graphics.style.ColourMap;
import uk.ac.rdg.resc.edal.graphics.style.ColourScale;
import uk.ac.rdg.resc.edal.graphics.style.ColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.FlatOpacity;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.PaletteColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.RasterLayer;

public class StyleXMLParser {
    public static class ColorAdapter extends XmlAdapter<String, Color> {
        private ColorAdapter() {
        }
        
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
        
        private static ColorAdapter adapter = new ColorAdapter();
        
        public static ColorAdapter getInstance() {
            return adapter;
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

    public static String serialise(MapImage image) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapImage.class);

        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(image, stringWriter);

        return stringWriter.toString();
    }

    public static MapImage deserialise(String xmlString) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapImage.class);

        Unmarshaller unmarshaller = context.createUnmarshaller();

        MapImage image = (MapImage) unmarshaller.unmarshal(new StringReader(xmlString));

        return image;
    }
    
    public static void generateSchema(final String path) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapImage.class);
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
        
        MapImage image = new MapImage();
//        StippleLayer layer = new StippleLayer("foam/TMP", new PatternScale(5, 305f, 280f, false));
//        image.getLayers().add(layer);
//        System.out.println(serialise(image));
//        
//        System.out.println();
//        
//        image = new Image();
        ColourScale scaleRange = new ColourScale(270f, 302f, false);
        ColourMap colourPalette = new ColourMap(Color.green, Color.cyan, new Color(0, 0, 0, 0), "rainbow", 20);
        ColourScheme colourScheme = new PaletteColourScheme(scaleRange, colourPalette);
        RasterLayer rasterLayer = new RasterLayer("TMP", colourScheme);
        rasterLayer.setOpacityTransform(new FlatOpacity(0.5f));
        image.getLayers().add(rasterLayer);
        System.out.println(serialise(image));
    }
}
