package uk.ac.rdg.resc.edal.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import uk.ac.rdg.resc.edal.util.CollectionUtils;

public class ColletionUtilsTest {
	private static int SIZE =100;
	private float[] f_array =new float[SIZE];
	private Float[] F_array =new Float[SIZE];
	private double [] d_array =new double [SIZE];
	private Double [] D_array =new Double [SIZE];
	private Set<Double> d_set =new HashSet<Double>();
	private Set<Float> f_set =new HashSet<Float>();
	@Before
	public void setUp() throws Exception {
		for (int i=0; i<SIZE; i++){
			float f_value =1.0f *i;
			double d_value =1.0 *i;
			f_array [i] =f_value;
			d_array [i] =d_value;
			d_set.add(d_value);
			f_set.add(f_value);
			F_array[i] =f_value;
			D_array[i] =d_value;
		}
	}

	@Test
	public void testListFromFloatArray() {
		List<Float> f_list =CollectionUtils.listFromFloatArray(f_array);		
		assertEquals(f_array.length, f_list.size());		
		for(int i=0; i<f_array.length; i++){
			assertEquals(f_list.get(i), f_array[i], 1e-6);
		}
	}
	
	@Test
	public void testListFromDoubleArray() {
		List<Double> d_list =CollectionUtils.listFromDoubleArray(d_array);
		assertEquals(d_array.length, d_list.size());
		for(int i=0; i<f_array.length; i++){
			assertEquals(d_list.get(i), d_array[i], 1e-6);
		}
	}
	
	@Test
	public void testSetOf() {
		Set<Float> expected_f_set =CollectionUtils.setOf(F_array);
		Set<Double> expected_d_set =CollectionUtils.setOf(D_array);
		assertEquals(expected_f_set, f_set);
		assertEquals(expected_d_set, d_set);
	}
	
	@Test(expected =NullPointerException.class)
	public void testNull(){
		CollectionUtils.listFromFloatArray(null);
		CollectionUtils.listFromDoubleArray(null);
	}
}
