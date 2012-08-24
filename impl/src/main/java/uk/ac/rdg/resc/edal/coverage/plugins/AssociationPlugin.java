/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.rdg.resc.edal.coverage.plugins;

import java.util.Arrays;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.RangeMetadataImpl;
import uk.ac.rdg.resc.edal.coverage.metadata.impl.ScalarMetadataImpl;

/**
 *
 * @author Kevin
 */
public class AssociationPlugin extends Plugin
{
    private final String description;
    private final String commonStandardName;
    //private final String xName;
    //private final String yName;
    
    private RangeMetadataImpl metadata;
   
   

    /**
     * Instantiate a new {@link VectorPlugin}
     * 
     * @param xCompId
     *            the textual identifier of the x-component
     * @param yCompId
     *            the textual identifier of the y-component
     * @param commonStandardName
     *            the common part of their standard name:
     * 
     *            e.g. for components with standard names
     *            eastward_sea_water_velocity and northward_sea_water_velocity,
     *            the common part of the standard name is sea_water_velocity
     * 
     * @param description
     *            a description of the new {@link RangeMetadata}
     */
    public AssociationPlugin(List<RangeMetadata> rangeMetadataList, String commonStandardName,String description) 
    {
        
        //super(Arrays.asList((RangeMetadata) xCompMetadata, (RangeMetadata) yCompMetadata));
       
        super(rangeMetadataList);
        
        this.description = description;
        this.commonStandardName = commonStandardName;
        
      /*  ScalarMetadata sMetadata = (ScalarMetadata) rangeMetadataList.get(0);
        this.xName =  sMetadata.getName()+ "_Mean";
        
        this.yName = getParentName() + "_Variance";
        */
    }
    
     
    @Override
    protected Object generateValue(String component, List<Object> values) {
     
        //System.out.println(" component is "+ component );
        if (component.contains("_Mean"))
        {
            return values.get(0);
        } 
        else if (component.contains("_Variance")) 
        {
            return values.get(1);
        }
        else 
        {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }

    @Override
    protected Class<?> generateValueType(String component, List<Class<?>> classes) {
        if (component.contains("_Mean"))
        {
            return classes.get(0);
        } 
        else if (component.contains("_Variance"))
        {
            return classes.get(1);
        }
        else
        {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + component);
        }
    }
    
    //private VectorComponent xMetadata = null;
    //private VectorComponent yMetadata = null;
    
    
    
    
    @Override
    protected RangeMetadata generateRangeMetadata(List<RangeMetadata> metadataList) {
        /*
         * The casts to ScalarMetadata are fine, because
         */
        metadata = new RangeMetadataImpl(metadataList.get(metadataList.size()-1).getName(), description);
        
        ScalarMetadata sMetadata = (ScalarMetadata) metadataList.get(0);
      
        ScalarMetadata scalarMetadata0 = new ScalarMetadataImpl(sMetadata.getName()+"_Mean",sMetadata.getDescription(),sMetadata.getParameter(),sMetadata.getUnits(),sMetadata.getValueType()); 
        
        sMetadata = (ScalarMetadata) metadataList.get(1);
        
        ScalarMetadata scalarMetadata1 = new ScalarMetadataImpl(sMetadata.getName()+"_Variance",sMetadata.getDescription(),sMetadata.getParameter(),sMetadata.getUnits(),sMetadata.getValueType()); 
        
        metadata.addMember(scalarMetadata0);
        metadata.addMember(scalarMetadata1);
        
        
       /* if (xMetadata == null) 
        {
            ScalarMetadata sMetadata = (ScalarMetadata) metadataList.get(0);
            xMetadata = new VectorComponentImpl(xName, sMetadata.getDescription(),
                    sMetadata.getParameter(), sMetadata.getUnits(), sMetadata.getValueType(),
                    VectorComponentType.X);
        }
        
        
        if (yMetadata == null)
        {
            ScalarMetadata sMetadata = (ScalarMetadata) metadataList.get(1);
            yMetadata = new VectorComponentImpl(yName, sMetadata.getDescription(),
                    sMetadata.getParameter(), sMetadata.getUnits(), sMetadata.getValueType(),
                    VectorComponentType.Y);
        }
        
        
        metadata.addMember(xMetadata);
        metadata.addMember(yMetadata);
        */
        
        metadata.setChildrenToPlot(Arrays.asList(scalarMetadata0.getName(), scalarMetadata1.getName()));
       
        return metadata;
    }

    @Override
    protected ScalarMetadata getScalarMetadata(String memberName) {
         
    ScalarMetadata scalarMetadata = (ScalarMetadata)metadata.getMemberMetadata(memberName);
    
    return scalarMetadata;  
        /* if (memberName.contains("Mean"))
      {
          return (ScalarMetadata)metadata;
      }    
      else if (memberName.contains("Variance"))
      {
          
          return (ScalarMetadata)metadata.getRepresentativeChildren().get(1);
          
      }  
      else
      {
            throw new IllegalArgumentException(memberName + " is not provided by this plugin");
      }
       */
        
    /*    if (xName.equals(memberName)) 
        {
            return xMetadata;
        } 
        else if (yName.equals(memberName))
        {
            return yMetadata;
        } 
        else {
            throw new IllegalArgumentException(memberName + " is not provided by this plugin");
        }
         
     */
        
     
    }
    
}
