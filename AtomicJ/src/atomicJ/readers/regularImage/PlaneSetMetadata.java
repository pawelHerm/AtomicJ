package atomicJ.readers.regularImage;

import java.util.Collections;
import java.util.List;

import atomicJ.data.Coordinate4D;

public class PlaneSetMetadata
{
    private static final PlaneSetMetadata NULL_INSTANCE = new PlaneSetMetadata(Collections.<Coordinate4D>emptyList());

    private final List<Coordinate4D> planeCoordinates;

    public PlaneSetMetadata(List<Coordinate4D> planeCoordinates)
    {
        this.planeCoordinates = planeCoordinates;
    }

    public static PlaneSetMetadata getNullInstance()
    {
        return NULL_INSTANCE;
    }

    public Coordinate4D getCombinedCoordinate()
    {
        return Coordinate4D.getCommonCoordinates(planeCoordinates);
    }

    public Coordinate4D getCoordinate(int planeIndex)
    {
        Coordinate4D coordinates = (planeCoordinates.size() > planeIndex) ? planeCoordinates.get(planeIndex) : null;

        return coordinates;
    }
}