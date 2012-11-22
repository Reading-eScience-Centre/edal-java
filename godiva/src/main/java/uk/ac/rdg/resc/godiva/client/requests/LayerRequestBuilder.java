package uk.ac.rdg.resc.godiva.client.requests;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;

/**
 * Builds a request to get the layer details from an EDAL WMS server
 * 
 * @author Guy Griffiths
 * 
 */
public class LayerRequestBuilder extends RequestBuilder {

    public LayerRequestBuilder(String layerId, String baseUrl, String time) {
        super(GET, URL.encode(baseUrl + "?request=GetMetadata&item=layerDetails&layerName="
                + layerId + ((time == null) ? "" : "&time=" + time)));
    }

    public void setCallback(LayerRequestCallback trc) {
        super.setCallback(trc);
    }
}
