package uk.ac.rdg.resc.edal.wms;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Arrays;

import javax.naming.OperationNotSupportedException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.rdg.resc.edal.graphics.formats.SimpleFormat;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.util.GlobalPlottingParams;
import uk.ac.rdg.resc.edal.wms.exceptions.WmsException;

/**
 * Servlet implementation class WmsServlet
 */
public class WmsServlet extends HttpServlet {
    public static final int TEST=1;
    private static final long serialVersionUID = 1L;

    private WmsCatalogue catalogue;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WmsServlet() {
        super();
    }

    public void setCatalogue(WmsCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {

        /*
         * Create an object that allows request parameters to be retrieved in a
         * way that is not sensitive to the case of the parameter NAMES (but is
         * sensitive to the case of the parameter VALUES).
         */
        RequestParams params = new RequestParams(httpServletRequest.getParameterMap());

        try {
            /*
             * Check the REQUEST parameter to see if we're producing a
             * capabilities document, a map or a FeatureInfo
             */
            String request = params.getMandatoryString("request");
            dispatchWmsRequest(request, params, httpServletRequest, httpServletResponse);
        } catch (WmsException wmse) {
            handleWmsException(wmse, httpServletResponse);
        } catch (SocketException se) {
            /*
             * SocketExceptions usually happen when the client has aborted the
             * connection, so there's nothing we can do here
             */
        } catch (IOException ioe) {
            /*
             * Filter out Tomcat ClientAbortExceptions, which for some reason
             * don't inherit from SocketException. We check the class name to
             * avoid a compile-time dependency on the Tomcat libraries
             */
            if (ioe.getClass().getName()
                    .equals("org.apache.catalina.connector.ClientAbortException")) {
            }
            /*
             * Other types of IOException are potentially interesting and must
             * be rethrown to avoid hiding errors (maybe they represent internal
             * errors when reading data for instance).
             */
            throw ioe;
        } catch (Exception e) {
            e.printStackTrace();
            /* An unexpected (internal) error has occurred */
            throw new IOException(e);
        }
    }

    private void dispatchWmsRequest(String request, RequestParams params,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        if (request.equals("GetMap")) {
            getMap(params, httpServletResponse);
        } else if (request.equals("GetCapabilities")) {
            getCapabilities(params, httpServletResponse);
        } else if (request.equals("GetFeatureInfo")) {
            /* Look to see if we're requesting data from a remote server */
            String url = params.getString("url");
            if (url != null && !url.trim().equals("")) {
                /*
                 * TODO We need to proxy the request if it is on a different
                 * server
                 */
//                NcwmsMetadataController.proxyRequest(url, httpServletRequest, httpServletResponse);
//                return;
            }
            getFeatureInfo(params, httpServletResponse);
        }
        /*
         * The REQUESTs below are non-standard
         */
        else if (request.equals("GetMetadata")) {
            /*
             * This is a request for non-standard metadata.
             */
            getMetadata(params, httpServletResponse);
        } else if (request.equals("GetLegendGraphic")) {
            /*
             * This is a request for an image representing the legend for the
             * map parameters
             */
            getLegendGraphic(params, httpServletResponse);
        } else if (request.equals("GetTimeseries")) {
            getTimeseries(params, httpServletResponse);
        } else if (request.equals("GetTransect")) {
            getTransect(params, httpServletResponse);
        } else if (request.equals("GetVerticalProfile")) {
            getVerticalProfile(params, httpServletResponse);
        } else if (request.equals("GetVerticalSection")) {
            getVerticalSection(params, httpServletResponse);
        } else {
            throw new OperationNotSupportedException(request);
        }
    }

    private void getMap(RequestParams params, HttpServletResponse httpServletResponse) throws WmsException {
        /*
         * TODO GetMapParameters should take the catalogue to override default values?
         */
        GetMapParameters getMapParams = new GetMapParameters(params);

        GlobalPlottingParams plottingParameters = getMapParams.getPlottingParameters();
        GetMapStyleParams styleParameters = getMapParams.getStyleParameters();
        if (!(getMapParams.getImageFormat() instanceof SimpleFormat)) {
            throw new WmsException("Currently KML is not supported.");
        }
        SimpleFormat simpleFormat = (SimpleFormat) getMapParams.getImageFormat();

        /*
         * Do some checks on the style parameters.
         * 
         * These only apply to non-XML styles. XML ones are more complex to
         * handle.
         * 
         * TODO sort out some checks on XML styles.
         */
        if (!styleParameters.isXmlDefined()) {
            if (styleParameters.isTransparent()
                    && !getMapParams.getImageFormat().supportsFullyTransparentPixels()) {
                throw new WmsException("The image format "
                        + getMapParams.getImageFormat().getMimeType()
                        + " does not support fully-transparent pixels");
            }
            if (styleParameters.getOpacity() < 100
                    && !getMapParams.getImageFormat().supportsPartiallyTransparentPixels()) {
                throw new WmsException("The image format "
                        + getMapParams.getImageFormat().getMimeType()
                        + " does not support partially-transparent pixels");
            }
            if (styleParameters.getNumLayers() > catalogue.getMaxSimultaneousLayers()) {
                throw new WmsException("Only " + catalogue.getMaxSimultaneousLayers()
                        + " layer(s) can be plotted at once");
            }
        }

        /*
         * Check the dimensions of the image
         */
        if (plottingParameters.getHeight() > catalogue.getMaxImageHeight()
                || plottingParameters.getWidth() > catalogue.getMaxImageWidth()) {
            throw new WmsException("Requested image size exceeds the maximum of "
                    + catalogue.getMaxImageWidth() + "x" + catalogue.getMaxImageHeight());
        }

        MapImage imageGenerator = styleParameters.getImageGenerator();

        try {
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            simpleFormat.writeImage(
                    Arrays.asList(imageGenerator.drawImage(plottingParameters, catalogue)),
                    outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            /*
             * The client can quite often cancel requests when loading tiled
             * maps.
             * 
             * This gives Broken pipe errors which can be ignored.
             * 
             * TODO what about other exceptions?
             */
        }
    }

    private void getCapabilities(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getFeatureInfo(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getMetadata(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getLegendGraphic(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getTimeseries(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getTransect(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getVerticalProfile(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void getVerticalSection(RequestParams params, HttpServletResponse httpServletResponse) {
        // TODO Auto-generated method stub

    }

    private void handleWmsException(WmsException wmse, HttpServletResponse httpServletResponse) throws IOException {
        /*
         * TODO this should return the exception as XML or potentially an image
         */
        PrintWriter writer = httpServletResponse.getWriter();
        wmse.printStackTrace(writer);
    }
}
