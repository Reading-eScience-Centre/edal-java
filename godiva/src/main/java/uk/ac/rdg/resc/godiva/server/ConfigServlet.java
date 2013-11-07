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

package uk.ac.rdg.resc.godiva.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Map<String, String> outputMap;
    private long lastModified = 0L;
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    outputMap = loadProperties(getServletContext());
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
	    super.init(config);
	    outputMap = loadProperties(getServletContext());
	}
	

	protected Map<String, String> loadProperties(ServletContext context) {
	    Map<String, String> returnMap = new HashMap<String, String>();
	    Properties props = new Properties();
        try {
            File configFile = new File(context.getRealPath("config.properties"));
            if(configFile.exists()){
                lastModified = configFile.lastModified();
                props.load(new FileInputStream(configFile));
                for(Object key : props.keySet()){
                    if(key instanceof String){
                        returnMap.put((String)key, props.getProperty((String)key));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No config.properties file found.  Defaults will be used.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnMap;
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*
         * If the config file has changed since it was last accessed, reload it.
         */
        File configFile = new File(getServletContext().getRealPath("config.properties"));
        if(configFile.exists() && configFile.lastModified() != lastModified){
            outputMap = loadProperties(getServletContext());
        }
        
        /*
         * Now output the properties as simple JSON properties
         */
        StringBuilder ret = new StringBuilder("{");
        for(String key : outputMap.keySet()){
            ret.append("\""+key+"\":\""+outputMap.get(key)+"\",");
        }
        ret.append("}");
        response.setContentType("application/json");
        response.getWriter().write(ret.toString());
	}
}
