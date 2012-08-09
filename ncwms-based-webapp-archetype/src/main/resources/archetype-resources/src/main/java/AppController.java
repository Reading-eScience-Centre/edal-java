package $groupId;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import uk.ac.rdg.resc.ncwms.config.Config;

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
        writer.write("AppController is working correctly");
        writer.close();
        outputStream.close();
        return null;
    }
}
