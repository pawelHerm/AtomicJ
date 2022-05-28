package atomicJ.analysis.indentation;

public enum AdhesiveEnergyEstimationMethod 
{
    FROM_FIT("Calculate from fit"), FROM_ADHESION_FORCE("From adhesion force");

    private final String prettyName;

    AdhesiveEnergyEstimationMethod(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public static AdhesiveEnergyEstimationMethod getValue(String identifier, AdhesiveEnergyEstimationMethod fallBackValue)
    {
        AdhesiveEnergyEstimationMethod value = fallBackValue;

        if(identifier != null)
        {
            for(AdhesiveEnergyEstimationMethod val : AdhesiveEnergyEstimationMethod.values())
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
        return name();
    }
}
