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
