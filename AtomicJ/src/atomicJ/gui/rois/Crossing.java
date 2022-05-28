package atomicJ.gui.rois;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Collection;
import java.util.Iterator;

public class Crossing implements Comparable<Crossing>
{
    private final double val;
    private final boolean upwards;

    public Crossing(double val, boolean upwards)
    {
        this.val = val;
        this.upwards = upwards;
    }

    public boolean isUpwards()
    {
        return upwards;
    }

    public double getValue()
    {
        return val;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof Crossing))
        {
            return false;
        }

        Crossing that = (Crossing)other;
        if(Double.compare(this.val, that.val) != 0)
        {
            return false;
        }
        if(this.upwards != that.upwards)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int code = 17;

        long valBits = Double.doubleToLongBits(val);
        code= 31*code + (int)(valBits ^ (valBits >>> 32)); 
        code = 31 + (upwards ? 1 : 0);

        return code;
    }

    @Override
    public int compareTo(Crossing other) 
    {
        return Double.compare(this.val, other.val);
    }

    public static TDoubleList resolveInsidness(Collection<Crossing> crossings)
    {
        int windingNumber = 0;

        TDoubleList insidnessLimits = new TDoubleArrayList();

        for(Iterator<Crossing> it = crossings.iterator();it.hasNext();)
        {
            Crossing crossing = it.next();
            int w = crossing.upwards ? 1 : -1;
            int windingNumberNew = windingNumber + w;

            if((windingNumberNew == 0) != (windingNumber == 0))
            {
                insidnessLimits.add(crossing.val);
            }

            windingNumber = windingNumberNew;
        }

        return insidnessLimits;
    }
}