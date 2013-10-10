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

import uk.ac.rdg.resc.edal.exceptions.EdalException;

public class GetFeatureInfoParameters extends GetMapParameters {

    private String[] layers;
    private String infoFormat;
    private int i;
    private int j;
    
    private int featureCount;
    private String exceptionType;

    public GetFeatureInfoParameters(RequestParams params) throws EdalException {
        super(params);
        layers = params.getMandatoryString("query_layers").split(",");
        infoFormat = params.getMandatoryString("info_format");
        i = params.getMandatoryPositiveInt("i");
        j = params.getMandatoryPositiveInt("j");
        
        featureCount = params.getPositiveInt("feature_count", 1);
        exceptionType = params.getString("exceptions", "XML");
    }
    
    public String[] getLayerNames() {
        return layers;
    }
    
    public String getInfoFormat() {
        return infoFormat;
    }
    
    public int getI() {
        return i;
    }
    
    public int getJ() {
        return j;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public String getExceptionType() {
        return exceptionType;
    }
}
