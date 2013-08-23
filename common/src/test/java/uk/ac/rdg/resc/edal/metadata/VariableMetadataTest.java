package uk.ac.rdg.resc.edal.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VariableMetadataTest {

    private VariableMetadata metadata1;
    private VariableMetadata metadata2;
    private VariableMetadata metadata3;
    private VariableMetadata metadata4;

    @Before
    public void setUp() {
        /*
         * We are just testing parent-child relationships here, so we don't need
         * any real values. Parameters of a VariableMetadata object cannot be
         * null though (tested below)
         */
        metadata1 = new VariableMetadata("metadata1", new Parameter(null, null, null, null), null,
                null, null);
        metadata2 = new VariableMetadata("metadata2", new Parameter(null, null, null, null), null,
                null, null);
        metadata3 = new VariableMetadata("metadata3", new Parameter(null, null, null, null), null,
                null, null);
        metadata4 = new VariableMetadata("metadata4", new Parameter(null, null, null, null), null,
                null, null);
    }

    @Test
    public void testParentChildRelationships() {
        /*
         * Setup a linear tree
         */
        metadata4.setParent(metadata3);
        metadata3.setParent(metadata2);
        metadata2.setParent(metadata1);

        testExpectedChildren(Arrays.asList(metadata2), Arrays.asList(metadata3),
                Arrays.asList(metadata4), new ArrayList<VariableMetadata>());

        assertEquals(null, metadata1.getParent());
        assertEquals(metadata1, metadata2.getParent());
        assertEquals(metadata2, metadata3.getParent());
        assertEquals(metadata3, metadata4.getParent());

        /*
         * Linear tree tested, now move 3 to be a child of 1
         */
        metadata3.setParent(metadata1);
        testExpectedChildren(Arrays.asList(metadata2, metadata3),
                new ArrayList<VariableMetadata>(), Arrays.asList(metadata4),
                new ArrayList<VariableMetadata>());
        assertEquals(null, metadata1.getParent());
        assertEquals(metadata1, metadata2.getParent());
        assertEquals(metadata1, metadata3.getParent());
        assertEquals(metadata3, metadata4.getParent());
        
        /*
         * Now try and make 4 the tree root
         */
        try {
            metadata1.setParent(metadata4);
            fail("Circular tree created");
        } catch (IllegalArgumentException e) {
            /*
             * Good.  It should have failed
             */
        }
        /*
         * Detatch it first and try again
         */
        metadata4.setParent(null);
        metadata1.setParent(metadata4);
        
        testExpectedChildren(Arrays.asList(metadata2, metadata3),
                new ArrayList<VariableMetadata>(), new ArrayList<VariableMetadata>(), 
                Arrays.asList(metadata1));
        assertEquals(metadata4, metadata1.getParent());
        assertEquals(metadata1, metadata2.getParent());
        assertEquals(metadata1, metadata3.getParent());
        assertEquals(null, metadata4.getParent());
    }

    private void testExpectedChildren(List<VariableMetadata> expectedChildren1,
            List<VariableMetadata> expectedChildren2, List<VariableMetadata> expectedChildren3,
            List<VariableMetadata> expectedChildren4) {
        assertEquals(new HashSet<VariableMetadata>(expectedChildren1), metadata1.getChildren());
        assertEquals(new HashSet<VariableMetadata>(expectedChildren2), metadata2.getChildren());
        assertEquals(new HashSet<VariableMetadata>(expectedChildren3), metadata3.getChildren());
        assertEquals(new HashSet<VariableMetadata>(expectedChildren4), metadata4.getChildren());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonCircularTrees() {
        setUp();
        metadata4.setParent(metadata3);
        metadata3.setParent(metadata2);
        metadata2.setParent(metadata1);
        metadata1.setParent(metadata4);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter() {
        new VariableMetadata("", null, null, null, null);
    }
}
