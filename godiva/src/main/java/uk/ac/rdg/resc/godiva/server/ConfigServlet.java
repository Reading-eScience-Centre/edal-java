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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ConfigServlet.class);
    private static final long serialVersionUID = 1L;
    private Map<String, Object> outputMap;
    private long lastModified = 0L;
    private File configFile;

    @Override
    public void init() throws ServletException {
        super.init();
        outputMap = loadProperties(getServletContext());
    }

    protected Map<String, Object> loadProperties(ServletContext context) {
        Map<String, Object> returnMap = new HashMap<>();
        Object configDir = context.getAttribute("configDir");
        if (configDir != null && configDir instanceof String) {
            Properties props = new Properties();
            try {
                configFile = new File(((String) configDir) + "/godiva3.properties");
                if (configFile.exists()) {
                    lastModified = configFile.lastModified();
                    props.load(new FileInputStream(configFile));
                    for (Object key : props.keySet()) {
                        Object value = props.getProperty((String) key);
                        if (value instanceof String) {
                            try {
                                value = Integer.parseInt((String) value);
                            } catch (Exception e) {
                            }
                        }
                        if (key instanceof String) {
                            returnMap.put((String) key, value);
                        }
                    }
                } else {
                    log.info("No godiva3.properties file found.  Defaults will be used.");
                    /*
                     * Now write out a default config file, for easier
                     * configuration in future.
                     * 
                     * If users completely ignore it, it's fine, because these
                     * are the defaults.
                     */
                    BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                    writer.write("mapHeight = 600\nmapWidth = 750\n#proxy = http://proxyUrlGoesHere/\n");
                    writer.close();
                }
            } catch (IOException e) {
                log.error("Problem reading config file for Godiva3 config.  Defaults will be used",
                        e);
            }
        } else {
            /*
             * No config directory. This is an unusual state of affairs. It
             * shouldn't really happen. Let's use defaults on the offchance it
             * does.
             */
        }
        return returnMap;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*
         * If the config file has changed since it was last accessed, reload it.
         */
        if (configFile.exists() && configFile.lastModified() != lastModified) {
            outputMap = loadProperties(getServletContext());
        }

        /*
         * Now output the properties as simple JSON properties
         */
        StringBuilder ret = new StringBuilder("{");
        for (String key : outputMap.keySet()) {
            Object value = outputMap.get(key);
            if (value instanceof Number) {
                ret.append("\"" + key + "\":" + value + ",");
            } else {
                ret.append("\"" + key + "\":\"" + value + "\",");
            }
        }
        /*
         * Delete the trailing comma
         */
        ret.deleteCharAt(ret.length() - 1);
        ret.append("}");
        response.setContentType("application/json");
        response.getWriter().write(ret.toString());
    }
}
