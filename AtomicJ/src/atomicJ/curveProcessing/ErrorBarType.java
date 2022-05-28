package atomicJ.curveProcessing;

import atomicJ.utilities.Validation;

public enum ErrorBarType 
{
    STANDARD_DEVIATION("Standard deviation","SD") 
    {
        @Override
        public double getErrorValue(double sumOfsquares, int sampleSize) 
        {
            Validation.requireValueGreaterOrEqualToParameterName(sampleSize, 2, "sampleSize");

            double val = Math.sqrt(sumOfsquares/(sampleSize-1));
            return val;
        }

        @Override
        public double[] getErrorValues(double[] multipleSSValues, int sampleSize) 
        {
            Validation.requireValueGreaterOrEqualToParameterName(sampleSize, 2, "sampleSize");

            int variableCount = multipleSSValues.length;
            double[] sdValues = new double[variableCount];

            for(int i = 0 ; i < variableCount; i++)
            {
                sdValues[i] = Math.sqrt(multipleSSValues[i]/(sampleSize-1));
            }

            return sdValues;
        }
    },

    STANDARD_ERROR("Standard error","SE")
    {
        @Override
        public double getErrorValue(double sumOfsquares, int sampleSize)
        {
            Validation.requireValueGreaterOrEqualToParameterName(sampleSize, 2, "sampleSize");

            double sd = Math.sqrt(sumOfsquares/(sampleSize-1));
            double se = sd/Math.sqrt(sampleSize);
            return se;
        }

        @Override
        public double[] getErrorValues(double[] multipleSSValues, int sampleSize) 
        {
            Validation.requireValueGreaterOrEqualToParameterName(sampleSize, 2, "sampleSize");

            int variableCount = multipleSSValues.length;
            double[] seValues = new double[variableCount];

            for(int i = 0 ; i < variableCount; i++)
            {
                double sd = Math.sqrt(multipleSSValues[i]/(sampleSize-1));
                seValues[i] = sd/Math.sqrt(sampleSize);    
            }

            return seValues;
        }
    };

    private final String fullPrettyName;
    private final String abbreviatedName;

    private ErrorBarType(String fullPrettyName,String abbreviatedName)
    {
        this.fullPrettyName = fullPrettyName;
        this.abbreviatedName = abbreviatedName;
    }

    public static ErrorBarType getValue(String identifier, ErrorBarType fallBackValue)
    {
        ErrorBarType errorBarType = fallBackValue;

        if(identifier != null)
        {
            for(ErrorBarType ebt : ErrorBarType.values())
            {
                String currentIdentifier =  ebt.getIdentifier();
                if(currentIdentifier.equals(identifier))
                {
                    errorBarType = ebt;
                    break;
                }
            }
        }

        return errorBarType;
    }

    public abstract double getErrorValue(double ss, int n);
    public abstract double[] getErrorValues(double[] multipleSSValues, int n);

    public String getFullPrettyName()
    {
        return fullPrettyName;
    }

    public String getAbbreviatedName()
    {
        return abbreviatedName;
    }

    @Override
    public String toString()
    {
        return fullPrettyName;
    }

    public String getIdentifier()
    {
        return name();
    }
}
