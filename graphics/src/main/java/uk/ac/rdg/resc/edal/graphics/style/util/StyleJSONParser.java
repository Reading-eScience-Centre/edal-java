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

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

public class StyleJSONParser {

    /*
     * Convert a JSON string to an XML string for use by JAXB.
     */
    public static String JSONtoXMLString(String jsonString) throws JSONException {
    	XMLSerializer serializer = new XMLSerializer();
		
    	JSON json = JSONSerializer.toJSON(jsonString);
		
		// Do not add type hints for conversion from xml back to JSON
		serializer.setTypeHintsEnabled(false);
		// Set namespace property for Image element
		serializer.setNamespace("resc", "http://www.resc.reading.ac.uk", "Image");
		
		String xmlString = serializer.write(json);
		
		// Add resc namespace prefix to image element
		xmlString = xmlString.replaceAll("Image", "resc:Image");
		// Strip out all superfluous object names and array element names
		// added by the XML serializer
		xmlString = xmlString.replaceAll("<o>", "");
		xmlString = xmlString.replaceAll("</o>", "");
		xmlString = xmlString.replaceAll("<e>", "");
		xmlString = xmlString.replaceAll("</e>", "");
				
		return xmlString;
    }
    
}
