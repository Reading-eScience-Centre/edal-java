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

//package uk.ac.rdg.resc.edal.graphics.style;
//
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElements;
//import javax.xml.bind.annotation.XmlType;
//
//import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
//import uk.ac.rdg.resc.edal.graphics.style.util.PlottingDatum;
//import uk.ac.rdg.resc.edal.util.Extents;
//
///*
// * Plot confidence interval triangles.
// */
//@XmlType(namespace = Image.NAMESPACE, name = "ConfidenceIntervalLayerType")
//public class ConfidenceIntervalLayer extends ImageLayer {
//
//	// The name of the field of lower bounds
//	@XmlElement(name = "LowerFieldName", required = true)
//    protected String lowerFieldName;
//	// The name of the field of upper bounds
//	@XmlElement(name = "UpperFieldName", required = true)
//    protected String upperFieldName;
////	@XmlElement(name = "GlyphSize")
//	private Integer glyphSize = 9;
//	// The colour scheme to use
//	@XmlElements({@XmlElement(name = "PaletteColourScheme", type = PaletteColourScheme.class),
//        @XmlElement(name = "ThresholdColourScheme", type = ThresholdColourScheme.class)})
//    protected ColourScheme colourScheme = new PaletteColourScheme();
//	/*
//     * This gets called after being unmarshalled from XML. This sets the
//     * glyph size in pixels.
//     */
//    void afterUnmarshal( Unmarshaller u, Object parent ) {
//        setSampleSize();
//    }
//
//	public ConfidenceIntervalLayer() {
//		super(PlotType.SUBSAMPLE);
//		setSampleSize();
//	}
//
//	public ConfidenceIntervalLayer(String lowerFieldName, String upperFieldName,
//			int glyphSize, ColourScheme colourSceme) {
//		super(PlotType.SUBSAMPLE);
//		
//		this.lowerFieldName = lowerFieldName;
//		this.upperFieldName = upperFieldName;
//		this.glyphSize = glyphSize;
//		this.colourScheme = colourSceme;
//		
//		setSampleSize();
//	}
//	
//	private void setSampleSize() {
//	    if(glyphSize < 1 || glyphSize == null) {
//	        throw new IllegalArgumentException("Glyph size must be non-null and > 0");
//	    }        
//	    setXSampleSize(glyphSize);
//	    setYSampleSize(glyphSize);
//	}
//
//	@Override
//	protected void drawIntoImage(BufferedImage image, DataReader dataReader) {
//		Graphics2D g = image.createGraphics();
// 
//		// Plot lower confidence interval triangles
//		List<PlottingDatum> lowerData = dataReader.getDataForLayerName(lowerFieldName);
//        for(PlottingDatum datum : lowerData) {
//        	Number value = datum.getValue();
//        	
//            int i = datum.getGridCoords().getX();
//            int j = datum.getGridCoords().getY();
//            if (value != null && !Float.isNaN(value.floatValue())) {
//            	Color color = colourScheme.getColor(value);
//        		int[] xPoints = {i - glyphSize / 2, i + glyphSize / 2 - 1, i + glyphSize / 2 - 1};
//        		int[] yPoints = {j + glyphSize / 2 - 1, j + glyphSize / 2 - 1, j - glyphSize / 2};
//        		g.setColor(color);
//            	g.fillPolygon(xPoints, yPoints, 3);	
//            }
//        }
//        
//        // Plot upper confidence interval triangles
//        List<PlottingDatum> upperData = dataReader.getDataForLayerName(upperFieldName);
//        for(PlottingDatum datum : upperData) {
//        	Number value = datum.getValue();
//        	
//            int i = datum.getGridCoords().getX();
//            int j = datum.getGridCoords().getY();
//            
//            if (value != null && !Float.isNaN(value.floatValue())) {
//            	Color color = colourScheme.getColor(value);
//            	int[] xPoints = {i - glyphSize / 2, i - glyphSize / 2, i + glyphSize / 2 - 1};
//        		int[] yPoints = {j + glyphSize / 2 - 1, j - glyphSize / 2, j - glyphSize / 2};
//        		g.setColor(color);
//            	g.fillPolygon(xPoints, yPoints, 3);	
//            }
//        }
//	}
//
//	@Override
//	protected Set<NameAndRange> getFieldsWithScales() {
//        Set<NameAndRange> ret = new HashSet<Drawable.NameAndRange>();
//        ret.add(new NameAndRange(lowerFieldName, Extents.newExtent(
//                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
//        ret.add(new NameAndRange(upperFieldName, Extents.newExtent(
//                colourScheme.getScaleMin(), colourScheme.getScaleMax())));
//        return ret;
//	}
//
//}
