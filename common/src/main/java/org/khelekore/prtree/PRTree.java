package org.khelekore.prtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Priority R-Tree, a spatial index, for N dimensions. This tree only supports
 * bulk loading.
 *
 * @param <T>
 *            the data type stored in the PRTree
 */
public class PRTree<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private MBRConverter<T> converter;
    private int branchFactor;

    private Node<T> root;
    private int numLeafs;
    private int height;

    /**
     * Create a new PRTree using the specified branch factor.
     * 
     * @param converter
     *            the MBRConverter to use for this tree
     * @param branchFactor
     *            the number of child nodes for each internal node.
     */
    public PRTree(MBRConverter<T> converter, int branchFactor) {
        this.converter = converter;
        this.branchFactor = branchFactor;
    }

    /**
     * Bulk load data into this tree.
     *
     * Create the leaf nodes that each hold (up to) branchFactor data entries.
     * Then use the leaf nodes as data until we can fit all nodes into the root
     * node.
     *
     * @param data
     *            the collection of data to store in the tree.
     * @throws IllegalStateException
     *             if the tree is already loaded
     */
    public void load(Collection<? extends T> data) {
        if (root != null)
            throw new IllegalStateException("Tree is already loaded");
        numLeafs = data.size();
        LeafBuilder lb = new LeafBuilder(converter.getDimensions(), branchFactor);

        List<LeafNode<T>> leafNodes = new ArrayList<>(estimateSize(numLeafs));
        lb.buildLeafs(data, new DataComparators<>(converter), new LeafNodeFactory(), leafNodes);

        height = 1;
        List<? extends Node<T>> nodes = leafNodes;
        while (nodes.size() > branchFactor) {
            height++;
            List<InternalNode<T>> internalNodes = new ArrayList<>(estimateSize(nodes.size()));
            lb.buildLeafs(nodes, new InternalNodeComparators<>(converter),
                    new InternalNodeFactory(), internalNodes);
            nodes = internalNodes;
        }
        setRoot(nodes);
    }

    private int estimateSize(int dataSize) {
        return (int) (1.0 / (branchFactor - 1) * dataSize);
    }

    private <N extends Node<T>> void setRoot(List<N> nodes) {
        if (nodes.size() == 0)
            root = new InternalNode<>(new Object[0]);
        else if (nodes.size() == 1) {
            root = nodes.get(0);
        } else {
            height++;
            root = new InternalNode<>(nodes.toArray());
        }
    }

    private class LeafNodeFactory implements NodeFactory<LeafNode<T>> {
        public LeafNode<T> create(Object[] data) {
            return new LeafNode<>(data);
        }
    }

    private class InternalNodeFactory implements NodeFactory<InternalNode<T>> {
        public InternalNode<T> create(Object[] data) {
            return new InternalNode<>(data);
        }
    }

    /**
     * Get a 2 dimensional minimum bounding rectangle of the data stored in this
     * tree.
     * 
     * @return the MBR of the whole PRTree
     */
    public MBR2D getMBR2D() {
        MBR mbr = getMBR();
        if (mbr == null)
            return null;
        return new SimpleMBR2D(mbr.getMin(0), mbr.getMin(1), mbr.getMax(0), mbr.getMax(1));
    }

    /**
     * Get an N dimensional minimum bounding box of the data stored in this
     * tree.
     * 
     * @return the MBR of the whole PRTree
     */
    public MBR getMBR() {
        return root.getMBR(converter);
    }

    /**
     * Get the number of data leafs in this tree.
     * 
     * @return the total number of leafs in this tree
     */
    public int getNumberOfLeaves() {
        return numLeafs;
    }

    /**
     * Check if this tree is empty
     * 
     * @return true if the number of leafs is 0, false otherwise
     */
    public boolean isEmpty() {
        return numLeafs == 0;
    }

    /**
     * Get the height of this tree.
     * 
     * @return the total height of this tree
     */
    public int getHeight() {
        return height;
    }

    /**
     * Finds all objects that intersect the given rectangle and stores the found
     * node in the given list. Note, this find method will only use two
     * dimensions, no matter how many dimensions the PRTree actually has.
     * 
     * @param xmin
     *            the minimum value of the x coordinate when searching
     * @param ymin
     *            the minimum value of the y coordinate when searching
     * @param xmax
     *            the maximum value of the x coordinate when searching
     * @param ymax
     *            the maximum value of the y coordinate when searching
     * @param resultNodes
     *            the list that will be filled with the result
     */
    public void find(double xmin, double ymin, double xmax, double ymax, List<T> resultNodes) {
        find(new SimpleMBR(xmin, xmax, ymin, ymax), resultNodes, new AcceptAll<T>());
    }

    /**
     * Finds all objects that intersect the given rectangle and stores the found
     * node in the given list. Note, this find method will only use two
     * dimensions, no matter how many dimensions the PRTree actually has.
     * 
     * @param xmin
     *            the minimum value of the x coordinate when searching
     * @param ymin
     *            the minimum value of the y coordinate when searching
     * @param xmax
     *            the maximum value of the x coordinate when searching
     * @param ymax
     *            the maximum value of the y coordinate when searching
     * @param resultNodes
     *            the list that will be filled with the result
     * @param filter
     *            a secondary filter to apply
     */
    public void find(double xmin, double ymin, double xmax, double ymax, List<T> resultNodes,
            NodeFilter<T> filter) {
        find(new SimpleMBR(xmin, xmax, ymin, ymax), resultNodes, filter);
    }

    /**
     * Finds all objects that intersect the given rectangle and stores the found
     * node in the given list.
     * 
     * @param query
     *            the bounds of the query
     * @param resultNodes
     *            the list that will be filled with the result
     */
    public void find(MBR query, List<T> resultNodes) {
        find(query, resultNodes, new AcceptAll<T>());
    }

    /**
     * Finds all objects that intersect the given rectangle and stores the found
     * node in the given list.
     * 
     * @param query
     *            the bounds of the query
     * @param resultNodes
     *            the list that will be filled with the result
     * @param filter
     *            a secondary filter to apply to the found nodes
     */
    public void find(MBR query, List<T> resultNodes, NodeFilter<T> filter) {
        validateRect(query);
        if (filter == null)
            throw new NullPointerException("Filter may not be null");
        root.find(query, converter, resultNodes, filter);
    }

    /**
     * Find all objects that intersect the given rectangle. Note, this find
     * method will only use two dimensions, no matter how many dimensions the
     * PRTree actually has.
     * 
     * @param xmin
     *            the minimum value of the x coordinate when searching
     * @param ymin
     *            the minimum value of the y coordinate when searching
     * @param xmax
     *            the maximum value of the x coordinate when searching
     * @param ymax
     *            the maximum value of the y coordinate when searching
     * @return an iterable of the elements inside the query rectangle
     * @throws IllegalArgumentException
     *             if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find(double xmin, double ymin, double xmax, double ymax) {
        return find(xmin, ymin, xmax, ymax, new AcceptAll<T>());
    }

    /**
     * Find all objects that intersect the given rectangle. Note, this find
     * method will only use two dimensions, no matter how many dimensions the
     * PRTree actually has.
     * 
     * @param xmin
     *            the minimum value of the x coordinate when searching
     * @param ymin
     *            the minimum value of the y coordinate when searching
     * @param xmax
     *            the maximum value of the x coordinate when searching
     * @param ymax
     *            the maximum value of the y coordinate when searching
     * @param filter
     *            a secondary filter to apply to the found nodes
     * @return an iterable of the elements inside the query rectangle
     * @throws IllegalArgumentException
     *             if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find(double xmin, double ymin, double xmax, double ymax, NodeFilter<T> filter) {
        return find(new SimpleMBR(xmin, xmax, ymin, ymax), filter);
    }

    /**
     * Find all objects that intersect the given rectangle.
     * 
     * @param query
     *            the bounds of the query
     * @throws IllegalArgumentException
     *             if xmin &gt; xmax or ymin &gt; ymax
     * @return an iterable of the elements inside the query rectangle
     */
    public Iterable<T> find(final MBR query) {
        return find(query, new AcceptAll<T>());
    }

    /**
     * Find all objects that intersect the given rectangle.
     * 
     * @param query
     *            the bounds of the query
     * @param filter
     *            a secondary filter to apply to the found nodes
     * @throws IllegalArgumentException
     *             if xmin &gt; xmax or ymin &gt; ymax
     * @return an iterable of the elements inside the query rectangle
     */
    public Iterable<T> find(final MBR query, final NodeFilter<T> filter) {
        validateRect(query);
        if (filter == null)
            throw new NullPointerException("Filter may not be null");
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                return new Finder(query, filter);
            }
        };
    }

    private void validateRect(MBR query) {
        for (int i = 0; i < converter.getDimensions(); i++) {
            double max = query.getMax(i);
            double min = query.getMin(i);
            if (max < min)
                throw new IllegalArgumentException("max: " + max + " < min: " + min + ", axis: "
                        + i + ", query: " + query);
        }
    }

    private class Finder implements Iterator<T> {
        private final MBR mbr;
        private final NodeFilter<T> filter;

        private List<T> ts = new ArrayList<>();
        private List<Node<T>> toVisit = new ArrayList<>();
        private T next;

        private int visitedNodes = 0;
        private int dataNodesVisited = 0;

        public Finder(MBR mbr, NodeFilter<T> filter) {
            this.mbr = mbr;
            this.filter = filter;
            toVisit.add(root);
            findNext();
        }

        public boolean hasNext() {
            return next != null;
        }

        public T next() {
            T toReturn = next;
            findNext();
            return toReturn;
        }

        private void findNext() {
            while (ts.isEmpty() && !toVisit.isEmpty()) {
                Node<T> n = toVisit.remove(toVisit.size() - 1);
                visitedNodes++;
                n.expand(mbr, filter, converter, ts, toVisit);
            }
            if (ts.isEmpty()) {
                next = null;
            } else {
                next = ts.remove(ts.size() - 1);
                dataNodesVisited++;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /**
     * Get the nearest neighbour of the given point
     * 
     * @param dc
     *            the DistanceCalculator to use.
     * @param filter
     *            a NodeFilter that can be used to ignore some leaf nodes.
     * @param maxHits
     *            the maximum number of entries to find.
     * @param p
     *            the point to find the nearest neighbour to.
     * @return A List of DistanceResult with up to maxHits results. Will return
     *         an empty list if this tree is empty.
     */
    public List<DistanceResult<T>> nearestNeighbour(DistanceCalculator<T> dc, NodeFilter<T> filter,
            int maxHits, PointND p) {
        if (isEmpty())
            return Collections.emptyList();
        NearestNeighbour<T> nn = new NearestNeighbour<>(converter, filter, maxHits, root, dc, p);
        return nn.find();
    }
}
