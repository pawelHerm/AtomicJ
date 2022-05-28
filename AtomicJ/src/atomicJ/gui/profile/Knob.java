package atomicJ.gui.profile;

import java.awt.Shape;

public class Knob
{        
    private final int key;
    private double position;

    private Shape knobHotSpot; 

    public Knob(double position, int key)
    {
        this.key = key;
        this.position = position;
    }

    public Knob(Knob that)
    {
        this.key = that.key;
        this.position = that.position;
    }

    public int getKey()
    {
        return key;
    }

    public void setKnobHotSpot(Shape knobHotSpot)
    {
        this.knobHotSpot = knobHotSpot;
    }

    public Shape getKnobHotSpot()
    {
        return knobHotSpot;
    }

    public double getPosition()
    {
        return position;
    }

    public void setPosition(double position)
    {
        this.position = position;
    }

    @Override
    public int hashCode()
    {
        int result = 17;

        long positionBits = Double.doubleToLongBits(position);

        result = 31*result + (int)(positionBits ^ (positionBits >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Knob)
        {
            Knob that = (Knob)o;
            boolean equal = (that.position == this.position);

            return equal;
        }
        return false;
    }
}