package uk.ac.rdg.resc.edal.coverage.plugins;

import java.util.AbstractList;
import java.util.List;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;

public class PluginWrappedGridValuesMatrix extends AbstractGridValuesMatrix<Object> {

    private List<? extends GridValuesMatrix<?>> gvmInputs;
    private Plugin plugin;
    private String memberName;
    private int nDim;

    public PluginWrappedGridValuesMatrix(Plugin plugin,
            List<? extends GridValuesMatrix<?>> gvmInputs, String memberName) {
        if (gvmInputs.size() == 0) {
            throw new IllegalArgumentException("Must provide at least one input field");
        }
        this.plugin = plugin;
        this.gvmInputs = gvmInputs;
        this.memberName = memberName;

        nDim = gvmInputs.get(0).getNDim();
    }

    @Override
    public Class<Object> getValueType() {
        return null;
    }

    @Override
    public void close() {
        for (GridValuesMatrix<?> gvm : gvmInputs) {
            gvm.close();
        }
    }

    @Override
    public int getNDim() {
        return nDim;
    }

    @Override
    protected GridAxis doGetAxis(int n) {
        return gvmInputs.get(0).getAxis(n);
    }

    @Override
    protected Object doReadPoint(final int[] coords) {
        List<Object> values = new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                return gvmInputs.get(index).readPoint(coords);
            }

            @Override
            public int size() {
                return gvmInputs.size();
            }
        };
        return plugin.getProcessedValue(memberName, values);
    }

    @Override
    protected GridValuesMatrix<Object> doReadBlock(final int[] mins, final int[] maxes) {
        /*
         * First we create some axis objects to populate our new GridValuesMatrix with
         */
        int[] sizes = new int[mins.length];
        final GridAxis[] axes = new GridAxis[mins.length];
        for (int i = 0; i < mins.length; i++) {
            sizes[i] = maxes[i] - mins[i] + 1;
            if (getAxis(i) == null)
                axes[i] = null;
            else
                axes[i] = new GridAxisImpl(this.getAxis(i).getName(), sizes[i]);
        }

        /*
         * Now we create an AbstractList which performs the readBlock method on
         * all of the plugin inputs.
         * 
         * Note that this could be done by simply populating a normal list, but
         * with this AbstractList we only read the blocks if and when needed,
         * caching them in an array
         */
        final List<GridValuesMatrix<?>> subMatrices = new AbstractList<GridValuesMatrix<?>>() {
            private GridValuesMatrix<?>[] accessedGVMs = new GridValuesMatrix<?>[gvmInputs.size()];
            
            @Override
            public GridValuesMatrix<?> get(int index) {
                if(accessedGVMs[index] == null){
                    accessedGVMs[index] = gvmInputs.get(index).readBlock(mins, maxes);
                }
                return accessedGVMs[index];
            }

            @Override
            public int size() {
                return accessedGVMs.length;
            }
        };
        
        /*
         * Now we return an InMemoryGridValuesMatrix which refers to the axes
         * and subMatrices we just defined
         */
        return new InMemoryGridValuesMatrix<Object>() {

            @Override
            @SuppressWarnings("unchecked")
            public Class<Object> getValueType() {
                return (Class<Object>) plugin.generateValueType(memberName,
                        new AbstractList<Class<?>>() {
                            @Override
                            public Class<?> get(int index) {
                                return gvmInputs.get(index).getValueType();
                            }

                            @Override
                            public int size() {
                                return gvmInputs.size();
                            }
                        });
            }

            @Override
            public int getNDim() {
                return axes.length;
            }

            @Override
            protected GridAxis doGetAxis(int n) {
                return axes[n];
            }

            @Override
            protected Object doReadPoint(final int[] coords) {
                return plugin.getProcessedValue(memberName, new AbstractList<Object>() {

                    @Override
                    public Object get(int index) {
                        return subMatrices.get(index).readPoint(coords);
                    }

                    @Override
                    public int size() {
                        return subMatrices.size();
                    }
                });
            }
        };
    }
}
