/*******************************************************************************
 * Copyright (c) 2017 The University of Reading
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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.util.GISUtils;

public class WmsContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(WmsContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        /*
         * Shut down all cache threads
         */
        CacheManager.getInstance().shutdown();
        
        /*
         * TODO Close any DB connections
         */
        GISUtils.releaseEpsgDatabase();
        
        /*-
         * Taken from:
         * https://stackoverflow.com/questions/3320400/to-prevent-a-memory-leak-the-jdbc-driver-has-been-forcibly-unregistered
         */


        /*
         * Now deregister JDBC drivers in this context's ClassLoader: Get the
         * webapp's ClassLoader
         */
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        /*
         * Loop through all drivers
         */
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                /*
                 * This driver was registered by the webapp's ClassLoader, so
                 * deregister it:
                 */
                try {
                    log.info("Deregistering JDBC driver {}", driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    log.error("Error deregistering JDBC driver {}", driver, ex);
                }
            } else {
                /*
                 * Driver was not registered by the webapp's ClassLoader and may
                 * be in use elsewhere
                 */
                log.trace(
                        "Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader",
                        driver);
            }
        }
    }

}
