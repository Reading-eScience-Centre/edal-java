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
        int[] sizes = new int[mins.length];
        final GridAxis[] axes = new GridAxis[mins.length];
        for (int i = 0; i < mins.length; i++) {
            sizes[i] = maxes[i] - mins[i] + 1;
            if (getAxis(i) == null)
                axes[i] = null;
            else
                axes[i] = new GridAxisImpl(this.getAxis(i).getName(), sizes[i]);
        }

        // This GridValuesMatrix wraps the parent one, without allocating new
        // storage
        return new InMemoryGridValuesMatrix<Object>() {

            @Override
            @SuppressWarnings("unchecked")
            public Class<Object> getValueType() {
                return (Class<Object>) plugin.generateValueType(memberName, new AbstractList<Class<?>>() {
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
            protected Object doReadPoint(int[] coords) {
                for (int i = 0; i < coords.length; i++) {
                    coords[i] += mins[i];
                }
                return PluginWrappedGridValuesMatrix.this.readPoint(coords);
            }
        };
    }


}
