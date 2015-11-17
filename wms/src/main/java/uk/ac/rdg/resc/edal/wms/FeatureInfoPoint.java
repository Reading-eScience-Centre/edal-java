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

package uk.ac.rdg.resc.edal.wms;

import java.util.Properties;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;

/**
 * Class which encapsulates information pertinent to individual points in a
 * GetFeatureInfo request
 * 
 * @author Guy Griffiths
 */
public class FeatureInfoPoint {
	private String layerName;
	private HorizontalPosition position;
	private Object value;
	private String featureId;
	private String timeStr;
	private Properties properties;

	/**
	 * Constructs a {@link FeatureInfoPoint}
	 * 
	 * @param layerName
	 *            The layer name to be displayed. If no layer name is required
	 *            (e.g. all features belong to the same layer), this can be
	 *            <code>null</code>
	 * @param featureId
	 *            The feature ID/name to be displayed. If this is not required
	 *            (e.g. there is a single feature) this can be <code>null</code>
	 * @param position
	 *            The position at which the feature is located. This is required
	 *            to sort multiple feature info points and filter by distance to
	 *            the clicked point
	 * @param timeStr
	 *            The time associated with the feature. This can be
	 *            <code>null</code>
	 * @param value
	 *            The value of the feature at the clicked point
	 * @param properties
	 *            Any additional properties associated with the feature
	 */
	public FeatureInfoPoint(String layerName, String featureId,
			HorizontalPosition position, String timeStr, Object value,
			Properties properties) {
		this.layerName = layerName;
		this.featureId = featureId;
		this.position = position;
		this.timeStr = timeStr;
		this.value = value;
		this.properties = properties;
	}

	/**
	 * @return The layer name to be displayed. If no layer name is required
	 *         (e.g. all features belong to the same layer), this can be
	 *         <code>null</code>
	 */
	public String getLayerName() {
		return layerName;
	}

	/**
	 * @return The feature ID/name to be displayed. If this is not required
	 *         (e.g. there is a single feature) this can be <code>null</code>
	 */
	public String getFeatureId() {
		return featureId;
	}

	/**
	 * @return The position at which the feature is located. This is required to
	 *         sort multiple feature info points and filter by distance to the
	 *         clicked point
	 */
	public HorizontalPosition getPosition() {
		return position;
	}

	/**
	 * @return The time associated with the feature. This can be
	 *         <code>null</code>
	 */
	public String getTime() {
		return timeStr;
	}

	/**
	 * @return The value of the feature at the clicked point
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return A set of additional {@link Properties} associated with the
	 *         feature at the clicked point
	 */
	public Properties getFeatureProperties() {
		return properties;
	}
}
