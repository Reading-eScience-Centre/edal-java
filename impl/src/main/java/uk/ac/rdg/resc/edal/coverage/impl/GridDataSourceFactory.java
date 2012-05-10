/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.rdg.resc.edal.coverage.impl;

/**
 * An object that can create {@link GridDataSource}s.
 * @author Jon
 */
public interface GridDataSourceFactory {
    
    public GridDataSource<?> openDataSource(String memberName);
    
}
