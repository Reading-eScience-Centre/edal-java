package uk.ac.rdg.resc.edal.graphics.style.datamodel.impl;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlType;

import uk.ac.rdg.resc.edal.graphics.style.DataReadingTypes.PlotType;

@XmlType(namespace = Image.NAMESPACE, name = "GlyphLayerType")
public abstract class GlyphLayer extends ImageLayer {

	public GlyphLayer() {
		super(PlotType.GLYPH);
	}

	@Override
	protected abstract void drawIntoImage(BufferedImage image, DataReader dataReader);

}
