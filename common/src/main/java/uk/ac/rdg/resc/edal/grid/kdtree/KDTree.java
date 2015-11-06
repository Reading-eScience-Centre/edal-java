package uk.ac.rdg.resc.edal.grid.kdtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Implementation of a 2-dimensional KDTree. Taken from old ncWMS codebase
 * (where it was uncredited) and modified to meet coding standards etc.
 *
 * @author Guy Griffiths
 */
public class KDTree {
    private final static double SQRT2 = Math.sqrt(2.0);
    private static final Logger log = LoggerFactory.getLogger(KDTree.class);

    private List<HorizontalPosition> points;
    private CoordinateReferenceSystem crs = null;

    private TreeNode[] tree = null;
    private double nominalMinResolution;
    private double expansionFactor = 3.5;
    private int approxQueries = 0, approxResults = 0, approxIterations = 0;

    public KDTree(List<HorizontalPosition> points) {
        this.points = points;
    }

    public void setQueryParameters(double expansionFactor, double nominalMinimumResolution) {
        this.expansionFactor = expansionFactor;
        this.nominalMinResolution = nominalMinimumResolution;
    }

    public void buildTree() {
        /* Load data from files into sourceData, and keep track of min/max */
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.NEGATIVE_INFINITY;
        double maxX = Double.POSITIVE_INFINITY;
        Point[] sourceData = new Point[points.size()];
        for(int i=0;i < points.size();i++) {
            HorizontalPosition pos = points.get(i);
            /*
             * Set the CRS or convert the
             */
            if (crs == null) {
                crs = pos.getCoordinateReferenceSystem();
            } else {
                if (!GISUtils.crsMatch(pos.getCoordinateReferenceSystem(), crs)) {
                    pos = GISUtils.transformPosition(pos, crs);
                }
            }
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            sourceData[i] = new Point(pos.getX(), pos.getY(), i);
        }
        /* Compute the nominal resolution */
        nominalMinResolution = 0.25f;
        // Math.sqrt(Math.min(max_lon - min_lon, max_lat - min_lat)) / 20.0f;

        /*
         * Perform an initial sort of the source data by longitude (likely to be
         * bigger for world data)
         */
        Comparator<Point> xComp = new PointComparator(false);
        Arrays.sort(sourceData, xComp);

        /* Calculate the number of elements needed in the tree */
        int nLeafElements = (int) Math.pow(2.0, Math.ceil(Math.log(points.size()) / Math.log(2.0)));
        int nTreeElements = 2 * nLeafElements - 1;
        /* Create the uninitialised tree with this number of elements */
        tree = new TreeNode[nTreeElements];

        /* Recursively build this into a tree */
        recursiveBuildTree(0, points.size() - 1, 0, false, sourceData);
    }

    /**
     * Verify the correctness of the tree
     */
    public void verifyChildren() {
        verifyChildren(0);
    }

    private void verifyChildren(int currentIndex) {
        /* Reached a leaf node, no more checks can be made */
        if (tree[currentIndex] instanceof Point) {
            return;
        }

        int leftChildIndex = 2 * currentIndex + 1;
        int rightChildIndex = 2 * currentIndex + 2;

        NonTerminalTreeNode myself = (NonTerminalTreeNode) tree[currentIndex];

        /*
         * If child nodes are leaf nodes, check that the fall on the correct
         * side of the current discriminator If the child nodes are non terminal
         * tree nodes, then they can only be checked if the discriminators are
         * of the same type
         */
        if (tree[leftChildIndex] instanceof Point) {
            Point leftChild = (Point) tree[leftChildIndex];
            if (myself.isY()) {
                if (leftChild.getY() > myself.getDiscriminator()) {
                    log.error("Left child latitude greater than self");
                }
            } else {
                if (leftChild.getX() > myself.getDiscriminator()) {
                    log.error("Left child longitude greater than self");
                }
            }
        } else {
            NonTerminalTreeNode leftChild = (NonTerminalTreeNode) tree[leftChildIndex];
            if (!(myself.isY() ^ leftChild.isY())) {
                if (leftChild.getDiscriminator() > myself.getDiscriminator()) {
                    log.error("Compatible left child discriminator greater than self");
                }
            }
        }

        if (tree[rightChildIndex] instanceof Point) {
            Point rightChild = (Point) tree[rightChildIndex];
            if (myself.isY()) {
                if (rightChild.getY() < myself.getDiscriminator()) {
                    log.error("Right child latitude lesser than self");
                }
            } else {
                if (rightChild.getX() < myself.getDiscriminator()) {
                    log.error("Right child longitude lesser than self");
                }
            }
        } else {
            NonTerminalTreeNode rightChild = (NonTerminalTreeNode) tree[rightChildIndex];
            if (!(myself.isY() ^ rightChild.isY())) {
                if (rightChild.getDiscriminator() < myself.getDiscriminator()) {
                    log.error("Compatible right child discriminator lesser than self");
                }
            }
        }

        /*
         * Call recursively for both children
         */
        verifyChildren(leftChildIndex);
        verifyChildren((rightChildIndex));
    }

    private void recursiveBuildTree(int sourceIndexFirst, int sourceIndexLast,
            int treeIndexCurrent, boolean sortedByY, Point[] sourceData) {

        if (sourceIndexFirst == sourceIndexLast) {
            /*
             * If the recursion has bottomed out and there is only one point of
             * source data left, store it in the tree at the current index
             */
            tree[treeIndexCurrent] = sourceData[sourceIndexFirst];
            return;
        }
        /*
         * Determine whether latitude or longitude has the biggest range across
         * our current set of source data A discriminator node will then be
         * inserted that splits this data into 2 (high and low)
         * 
         * Determine whether we should be sorting by latitude or longitude
         */
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        for (int i = sourceIndexFirst; i <= sourceIndexLast; i++) {
            minY = Math.min(minY, sourceData[i].getY());
            maxY = Math.max(maxY, sourceData[i].getY());
            minX = Math.min(minX, sourceData[i].getX());
            maxX = Math.max(maxX, sourceData[i].getX());
        }
        boolean discriminateOnY = (Math.abs(maxY - minY) >= Math.abs(maxX - minX));

        /*
         * Determine if we need to sort - if discriminateOnY and sortedByYhave
         * different values
         */
        if (discriminateOnY ^ sortedByY) {
            /* Sort by x or y as appropriate */
            Comparator<Point> comp = new PointComparator(discriminateOnY);
            Arrays.sort(sourceData, sourceIndexFirst, sourceIndexLast + 1, comp);
        }

        /*
         * Work out the median, and the indices of the values surrounding the
         * median
         */
        int endLeft;
        int startRight;
        double discriminator;
        if (((sourceIndexLast - sourceIndexFirst) % 2) != 0) {
            /* Even number of elements */
            endLeft = sourceIndexFirst + ((sourceIndexLast - sourceIndexFirst - 1) / 2);
            startRight = endLeft + 1;
            if (discriminateOnY) {
                discriminator = (sourceData[endLeft].getY() + sourceData[startRight].getY()) / 2.0;
            } else {
                discriminator = (sourceData[endLeft].getX() + sourceData[startRight].getX()) / 2.0;
            }
        } else {
            /* Odd number of elements */
            endLeft = ((sourceIndexLast - sourceIndexFirst) / 2) + sourceIndexFirst;
            startRight = endLeft + 1;
            if (discriminateOnY) {
                discriminator = sourceData[endLeft].getY();
            } else {
                discriminator = sourceData[endLeft].getX();
            }
        }

        /*
         * Store this information back into the tree to create the discriminator
         * node
         */
        tree[treeIndexCurrent] = new NonTerminalTreeNode(discriminator, discriminateOnY);

        /* Call recursively for both sides */
        recursiveBuildTree(sourceIndexFirst, endLeft, 2 * treeIndexCurrent + 1, discriminateOnY,
                sourceData);
        recursiveBuildTree(startRight, sourceIndexLast, 2 * treeIndexCurrent + 2, discriminateOnY,
                sourceData);
    }

    public Point limitedNearestNeighbour(HorizontalPosition pos, double maxDistance) {
        /*
         * Transform position into correct CRS if necessary
         */
        if (!GISUtils.crsMatch(pos.getCoordinateReferenceSystem(), crs)) {
            pos = GISUtils.transformPosition(pos, crs);
        }
        Point nearestNeighbour = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Point candidate : approxNearestNeighbour(pos, maxDistance)) {
            double currentDistanceX = (pos.getX() - candidate.getX());
            double currentDistanceY = (pos.getY() - candidate.getY());
            double currentDistance = currentDistanceY * currentDistanceY + currentDistanceX
                    * currentDistanceX;
            if (currentDistance < bestDistance) {
                nearestNeighbour = candidate;
                bestDistance = currentDistance;
            }
        }
        /* Return best result */
        return nearestNeighbour;
    }

    public List<Point> approxNearestNeighbour(HorizontalPosition pos, double maxDistance) {
        /*
         * Transform position into correct CRS if necessary
         */
        if (!GISUtils.crsMatch(pos.getCoordinateReferenceSystem(), crs)) {
            pos = GISUtils.transformPosition(pos, crs);
        }
        approxQueries++;
        double currentDistance = nominalMinResolution;
        ArrayList<Point> results;
        boolean breakNext = false;
        while (currentDistance <= maxDistance) {
            approxIterations++;
            results = rangeQuery(pos.getX() - currentDistance, pos.getX() + currentDistance,
                    pos.getY() - currentDistance, pos.getY() + currentDistance);
            if (results.size() > 0) {
                /*
                 * Need to do one more check - if the point found is at the
                 * corner of the current box, there could be a closer point
                 * within that distance, so set the search distance to the
                 * distance to the current point
                 */
                currentDistance *= SQRT2;
                results = rangeQuery(pos.getX() - currentDistance, pos.getX() + currentDistance,
                        pos.getY() - currentDistance, pos.getY() + currentDistance);
                approxResults += results.size();
                return results;
            }
            if (breakNext) {
                break;
            }
            currentDistance *= expansionFactor;
            if (currentDistance > maxDistance) {
                breakNext = true;
                currentDistance = maxDistance;
            }
        }
        /* Reached max distance and no points found - return empty */
        return Collections.emptyList();
    }

    public void printApproxQueryStats() {
        System.out.println("Computed nominal resolution " + nominalMinResolution);
        if (approxQueries > 0) {
            System.out.println("Results per query " + (double) (approxResults / approxQueries));
            System.out.println("Iterations per query "
                    + (double) (approxIterations / approxQueries));
        } else {
            System.out.println("No approximate queries made");
        }
    }

    public void resetApproxQueryStats() {
        approxIterations = 0;
        approxResults = 0;
        approxQueries = 0;
    }

    private final static double squaredDistance(Point p, HorizontalPosition pos) {
        return Math.pow(p.getX() - pos.getX(), 2.0) + Math.pow(p.getY() - pos.getY(), 2.0);
    }

    public Point nearestNeighbour(HorizontalPosition pos) {
        /*
         * Transform position into correct CRS if necessary
         */
        if (!GISUtils.crsMatch(pos.getCoordinateReferenceSystem(), crs)) {
            pos = GISUtils.transformPosition(pos, crs);
        }
        return nearestNeighbourRecurse(pos, 0);
    }

    private final Point nearestNeighbourRecurse(HorizontalPosition pos, int currentIndex) {
        if (tree[currentIndex] instanceof Point) {
            /* Terminal node reached - return it */
            return (Point) tree[currentIndex];
        } else {
            /* Non-terminal */
            NonTerminalTreeNode node = (NonTerminalTreeNode) tree[currentIndex];
            double pivotTargetDistance;
            if (node.isY()) {
                pivotTargetDistance = node.getDiscriminator() - pos.getY();
            } else {
                pivotTargetDistance = node.getDiscriminator() - pos.getX();
            }

            /* Search the 'near' branch */
            Point best;
            if (pivotTargetDistance > 0) {
                best = nearestNeighbourRecurse(pos, 2 * currentIndex + 1);
            } else {
                best = nearestNeighbourRecurse(pos, 2 * currentIndex + 2);
            }
            /*
             * Only search the 'away' branch if the squared distance between the
             * current best and the target is greater Than the squared distance
             * between the target and the branch pivot
             */
            if (squaredDistance(best, pos) > Math.pow(pivotTargetDistance, 2.0)) {
                Point potentialBest;
                /* Search the 'away' branch */
                if (pivotTargetDistance > 0) {
                    potentialBest = nearestNeighbourRecurse(pos,
                            2 * (currentIndex + 1));
                } else {
                    potentialBest = nearestNeighbourRecurse(pos,
                            (2 * (currentIndex + 1)) - 1);
                }
                if (squaredDistance(potentialBest, pos) < squaredDistance(best,
                        pos)) {
                    return potentialBest;
                }
            }

            return best;
        }
    }

    public ArrayList<Point> rangeQuery(double minX, double maxX, double minY, double maxY) {
        ArrayList<Point> results = new ArrayList<Point>();
        rangeQueryRecurse(minX, maxX, minY, maxY, results, 0);
        return results;
    }

    private final void rangeQueryRecurse(double minX, double maxX, double minY, double maxY,
            ArrayList<Point> results, int treeCurrentIndex) {
        if (tree[treeCurrentIndex] instanceof Point) {
            /* Terminal - return this point if it's within bounds */
            Point terminalPoint = (Point) tree[treeCurrentIndex];
            if (terminalPoint.getX() >= minX && terminalPoint.getX() <= maxX
                    && terminalPoint.getY() >= minY && terminalPoint.getY() <= maxY) {
                results.add(terminalPoint);
            }
            return;
        } else {
            /*-
             * 3 cases - the discriminator in the non-terminal node can be less
             * than search range, within it, or greater than it
             * 
             * Less than: Search right of this node
             * Within: Search left and right of this node
             * Greater than: Search left of this node
             */
            boolean searchLeft, searchRight;
            NonTerminalTreeNode node = (NonTerminalTreeNode) tree[treeCurrentIndex];
            if (node.isY()) {
                searchLeft = (node.getDiscriminator() >= minY);
                searchRight = (node.getDiscriminator() <= maxY);
            } else {
                searchLeft = (node.getDiscriminator() >= minX);
                searchRight = (node.getDiscriminator() <= maxX);
            }

            if (searchLeft) {
                rangeQueryRecurse(minX, maxX, minY, maxY, results, (2 * (treeCurrentIndex + 1)) - 1);
            }
            if (searchRight) {
                rangeQueryRecurse(minX, maxX, minY, maxY, results, (2 * (treeCurrentIndex + 1)));
            }
        }
    }
}
