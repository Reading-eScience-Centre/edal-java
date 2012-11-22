package uk.ac.rdg.resc.godiva.client.requests;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;

/**
 * Builds a request to get the timesteps for a particular day from an EDAL WMS
 * server
 * 
 * @author Guy Griffiths
 * 
 */
public class TimeRequestBuilder extends RequestBuilder {

    public TimeRequestBuilder(String layerId, String day, String baseUrl) {
        super(GET, URL.encode(baseUrl + "?request=GetMetadata&item=timesteps&layerName=" + layerId
                + "&day=" + day));
    }

    public void setCallback(TimeRequestCallback trc) {
        super.setCallback(trc);
    }
}
