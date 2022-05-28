package atomicJ.gui.curveProcessing;

public enum TipShapeParameter 
{
    RADIUS("Radius"), HALF_ANGLE("Half angle"), TRANSITION_RADIUS("Transitio radius"), EXPONENT("Exp"), FACTOR("Factor");

    private final String prettyName;

    TipShapeParameter(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public static TipShapeParameter getValue(String identifier)
    {
        TipShapeParameter value = null;

        if(identifier != null)
        {
            for(TipShapeParameter val : TipShapeParameter.values())
            {
                String estIdentifier =  val.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    value = val;
                    break;
                }
            }
        }


        return value;
    }


    @Override
    public String toString()
    {
        return prettyName;
    }

    public String getIdentifier()
    {
        return super.toString();
    }
}
