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
//import javax.xml.bind.Unmarshaller;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlType;
//
//import uk.ac.rdg.resc.edal.graphics.style.util.DataReadingTypes.PlotType;
//
//@XmlType(namespace = Image.NAMESPACE, name = "SubsampledGlyphLayerType")
//public class SubsampledGlyphLayer extends GlyphLayer {
//
//    @XmlElement(name = "GlyphSpacing")
//	private Float glyphSpacing = 1.5f;
//    /*
//     * This gets called after being unmarshalled from XML. This reads in the
//     * icon for the glyph and sets the glyph spacing as a factor of the icon
//     * size.
//     */
//    void afterUnmarshal( Unmarshaller u, Object parent ) {
//    	icon = getIcon(this.glyphIconName);
//        
//    	if(glyphSpacing < 1.0 || glyphSpacing == null) {
//            throw new IllegalArgumentException("Glyph spacing must be non-null and => 1.0");
//        }        
//    	setXSampleSize((int) (icon.getWidth()*glyphSpacing));
//        setYSampleSize((int) (icon.getHeight()*glyphSpacing));
//    }
//
//   public SubsampledGlyphLayer() throws InstantiationException {
//		super(PlotType.SUBSAMPLE);
//	}
//	
//	public SubsampledGlyphLayer(String dataFieldName, String glyphIconName, float glyphSpacing,
//			ColourScheme colourScheme) throws InstantiationException {
//		super(PlotType.SUBSAMPLE);
//		
//		this.dataFieldName = dataFieldName;
//		this.glyphIconName = glyphIconName;
//		this.glyphSpacing = glyphSpacing;
//		this.colourScheme = colourScheme;	
//	}
//
//	public float getGlyphSpacing() {
//		return glyphSpacing;
//	}
//
//}
