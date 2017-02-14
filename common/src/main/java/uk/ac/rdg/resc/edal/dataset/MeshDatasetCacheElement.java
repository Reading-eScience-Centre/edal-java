package uk.ac.rdg.resc.edal.dataset;

import uk.ac.rdg.resc.edal.util.GridCoordinates2D;

import java.io.Serializable;
import java.util.List;

/*
 *
 */
class MeshDatasetCacheElement implements Serializable {
    List<GridCoordinates2D> outputCoords;
    List<HZTDataSource.MeshCoordinates3D> coordsToRead;

    MeshDatasetCacheElement(List<GridCoordinates2D> outputCoords, List<HZTDataSource.MeshCoordinates3D> coordsToRead) {
        this.outputCoords = outputCoords;
        this.coordsToRead = coordsToRead;
    }

    List<GridCoordinates2D> getOutputCoords() {
        return outputCoords;
    }

    List<HZTDataSource.MeshCoordinates3D> getCoordsToRead() {
        return coordsToRead;
    }
}
