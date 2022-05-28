package atomicJ.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.utilities.ArrayUtilities;

public class Coordinate4D
{
    private final UnitExpression tCoordinate;
    private final UnitExpression xCoordinate;
    private final UnitExpression yCoordinate;
    private final UnitExpression zCoordinate;

    public Coordinate4D(UnitExpression tCoordinate, UnitExpression xCoordinate, UnitExpression yCoordinate, UnitExpression zCoordinate)
    {
        this.tCoordinate = tCoordinate;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.zCoordinate = zCoordinate;
    }

    public UnitExpression getTCoordinate()
    {
        return tCoordinate;
    }

    public UnitExpression getXCoordinate()
    {
        return xCoordinate;
    }

    public UnitExpression getYCoordinate()
    {
        return yCoordinate;
    } 

    public UnitExpression getZCoordinate()
    {
        return zCoordinate;
    } 

    public boolean isEqualUpToPrefices(Coordinate4D other)
    {
        if(!UnitExpression.equalUpToPrefices(this.tCoordinate, other.tCoordinate))
        {
            return false;
        }

        if(!UnitExpression.equalUpToPrefices(this.xCoordinate, other.xCoordinate))
        {
            return false;
        }

        if(!UnitExpression.equalUpToPrefices(this.yCoordinate, other.yCoordinate))
        {
            return false;
        }

        if(!UnitExpression.equalUpToPrefices(this.zCoordinate, other.zCoordinate))
        {
            return false;
        }
        return true;
    }

    public static Coordinate4D getCommonCoordinates(List<Coordinate4D> coordinates)
    {
        if(coordinates.isEmpty())
        {
            return null;
        }

        Coordinate4D firstPlaneCoordinates = coordinates.get(0);

        UnitExpression t = (isTConstant(coordinates)) ? firstPlaneCoordinates.getTCoordinate() : null;
        UnitExpression x = (isXConstant(coordinates)) ? firstPlaneCoordinates.getXCoordinate() : null;
        UnitExpression y = (isYConstant(coordinates)) ? firstPlaneCoordinates.getYCoordinate() : null;
        UnitExpression z = (isZConstant(coordinates)) ? firstPlaneCoordinates.getZCoordinate() : null;

        return new Coordinate4D(t, x, y, z);
    }

    public static boolean isZConstant(List<Coordinate4D> coordinates)
    {
        return isConstant(coordinates, CoordinateType.Z_POSITION);
    }

    public static boolean isYConstant(List<Coordinate4D> coordinates)
    {
        return isConstant(coordinates, CoordinateType.Y_POSITION);
    }

    public static boolean isXConstant(List<Coordinate4D> coordinates)
    {
        return isConstant(coordinates, CoordinateType.X_POSITION);
    }

    public static boolean isTConstant(List<Coordinate4D> coordinates)
    {   
        return isConstant(coordinates, CoordinateType.TIME);
    }

    private static boolean isConstant(List<Coordinate4D> coordinates, CoordinateType type)
    {
        if(coordinates.isEmpty())
        {
            return true;
        }

        UnitExpression firstCoordinate = type.getCoordinate(coordinates.get(0));

        boolean constant = true;

        for(int i = 0; i<coordinates.size();i++)
        {
            constant = constant && UnitExpression.equalUpToPrefices(firstCoordinate, type.getCoordinate(coordinates.get(i)));

            if(!constant)
            {
                break;
            }
        }

        return constant;
    }

    public static List<Coordinate4D> getCoordinates(List<Channel2D> channels)
    {
        List<Coordinate4D> coordinates = new ArrayList<>();

        for(Channel2D channel : channels)
        {
            coordinates.add(channel.getMetadata().getCoordinates());
        }

        return coordinates;
    }

    public static List<DataAxis1D> getAxesWithoutTies(List<Coordinate4D> coordinates)
    {
        List<DataAxis1D> axes = new ArrayList<>();

        for(CoordinateType type : CoordinateType.values())
        {
            DataAxis1D axis = getAxisIfNoTiedValues(coordinates, type);
            if(axis != null)
            {
                axes.add(axis);
            }
        }

        return axes;
    }

    public static DataAxis1D getAxisIfNoTiedValues(List<Coordinate4D> coordinates, CoordinateType type)
    {
        if(coordinates.isEmpty())
        {
            return null;
        }


        Coordinate4D coordinate4D = coordinates.get(0);
        if(coordinate4D== null)
        {
            return null;
        }

        UnitExpression firstCoordExpression = type.getCoordinate(coordinate4D);

        if(firstCoordExpression == null)
        {
            return null;
        }

        PrefixedUnit commonUnit = firstCoordExpression.getUnit();
        Set<Double> valueSet = new LinkedHashSet<>();

        for(int i = 0; i<coordinates.size();i++)
        {
            UnitExpression currrentCoordinateExpression = type.getCoordinate(coordinates.get(i));

            if(currrentCoordinateExpression == null)
            {
                return null;
            }

            UnitExpression currentCoordinateCommonUnit = currrentCoordinateExpression.derive(commonUnit);
            Double value = currentCoordinateCommonUnit.getValue();
            if(valueSet.contains(value))
            {
                return null;
            }

            valueSet.add(value);
        }

        double[] val = ArrayUtilities.getDoubleArray(valueSet);

        Grid1D gridRepresentation = Grid1D.getGrid(val, Quantities.TIME_SECONDS, 1e-16);
        if(gridRepresentation != null)
        {
            return gridRepresentation;
        }

        Arrays.sort(val);

        DataAxis1D axis = new IncreasingDataAxis1D(val, new UnitQuantity(type.getQuantityName(), commonUnit));

        return axis;
    }

    private static enum CoordinateType
    {
        TIME("Time") {
            @Override
            public UnitExpression getCoordinate(Coordinate4D coordinates) {
                return coordinates.getTCoordinate();
            }
        }, X_POSITION("X position") {
            @Override
            public UnitExpression getCoordinate(Coordinate4D coordinates) {
                return coordinates.getXCoordinate();
            }
        }, Y_POSITION("Y position") {
            @Override
            public UnitExpression getCoordinate(Coordinate4D coordinates) {
                return coordinates.getYCoordinate();
            }
        }, Z_POSITION("Z position") 
        {
            @Override
            public UnitExpression getCoordinate(Coordinate4D coordinates) {
                return coordinates.getZCoordinate();
            }
        };

        private final String quantityName;
        CoordinateType(String quantityName)
        {
            this.quantityName = quantityName;
        }

        public String getQuantityName()
        {
            return quantityName;
        }

        public abstract UnitExpression getCoordinate(Coordinate4D coordinate);
    }
}