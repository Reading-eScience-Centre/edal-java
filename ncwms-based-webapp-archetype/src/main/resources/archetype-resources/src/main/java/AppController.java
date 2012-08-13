package $groupId;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import uk.ac.rdg.resc.edal.feature.Feature;
import uk.ac.rdg.resc.ncwms.config.Config;
import uk.ac.rdg.resc.ncwms.config.Dataset;

public class AppController extends MultiActionController {
    
    /*
     * This object gives access to the ncWMS config
     */
    protected Config config;

    public AppController(Config config) {
        this.config = config;
    }

    /**
     * A test method for this {@link AppController}
     * 
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public ModelAndView doTest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write("AppController is working correctly.  We have the data:\n");
        Map<String, Dataset> allDatasets = config.getAllDatasets();
        for(Entry<String, Dataset> dataset : allDatasets.entrySet()){
            writer.write(dataset.getKey()+":\n");
            for(Feature feature : dataset.getValue().getFeatureCollection()){
                writer.write("\t"+feature.getName()+":\n");
                for(String memberName : feature.getCoverage().getScalarMemberNames()){
                    writer.write("\t\t"+memberName+"\n");
                }
            }
        }
        
        writer.close();
        outputStream.close();
        return null;
    }
}
