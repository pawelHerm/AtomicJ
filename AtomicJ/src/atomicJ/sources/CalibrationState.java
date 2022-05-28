package atomicJ.sources;

import atomicJ.data.Channel1D;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.gui.UserCommunicableException;

public enum CalibrationState
{
    CURRENT(Quantities.DEFLECTION_NANO_AMPERES)
    {
        @Override
        public double getPhotodiodeValueConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) throws UserCommunicableException
        {
            return 1;
        }

        @Override
        public double getDeflectionConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) 
        {
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double deflectionConversionConstant = requestedSensitivityKnown ? sensitivityRequested : sensitivityReadIn;

            return deflectionConversionConstant;
        }

        @Override
        public double getForceConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested,double springConstantRequested) 
        {
            boolean requestedSpringConstantKnown = !Double.isNaN(springConstantRequested);            
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double springConstant = requestedSpringConstantKnown ? springConstantRequested : 1000*springConstantReadIn;
            double sensitivity = requestedSensitivityKnown ? sensitivityRequested : sensitivityReadIn;

            double forceConversionConstant = sensitivity*springConstant;

            return forceConversionConstant;
        }

        @Override
        public boolean canBeUsedForCalibration(double sensitivityReadIn, double springConstantReadIn) 
        {
            return true;
        }

        @Override
        public boolean isUnitCompatible(PrefixedUnit unit) 
        {
            return StandardQuantityTypes.CURRENT.isCompatible(unit);
        }
    }, 

    VOLTAGE(Quantities.DEFLECTION_VOLTS)
    {
        @Override
        public double getPhotodiodeValueConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) throws UserCommunicableException
        {
            return 1;
        }

        @Override
        public double getDeflectionConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) 
        {
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double deflectionConversionConstant = requestedSensitivityKnown ? sensitivityRequested : sensitivityReadIn;

            return deflectionConversionConstant;
        }

        @Override
        public double getForceConversionFactor(double sensitivityReadIn,double springConstantReadIn, double sensitivityRequested,double springConstantRequested) 
        {
            boolean requestedSpringConstantKnown = !Double.isNaN(springConstantRequested);            
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double springConstant = requestedSpringConstantKnown ? springConstantRequested : 1000*springConstantReadIn;
            double sensitivity = requestedSensitivityKnown ? sensitivityRequested : sensitivityReadIn;

            double forceConversionConstant = sensitivity*springConstant;

            return forceConversionConstant;
        }

        @Override
        public boolean canBeUsedForCalibration(double sensitivityReadIn, double springConstantReadIn) 
        {
            return true;
        }

        @Override
        public boolean isUnitCompatible(PrefixedUnit unit) 
        {
            return StandardQuantityTypes.VOLTAGE.isCompatible(unit);
        }
    }, 

    DEFLECTION_SENSITIVITY_CALIBRATED(Quantities.DEFLECTION_MICRONS)
    {
        @Override
        public double getPhotodiodeValueConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) throws UserCommunicableException
        {
            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            boolean necessaryDataKnown = (readInSensitivityKnown || requestedSensitivityKnown);

            if(!necessaryDataKnown)
            {
                throw new UserCommunicableException("The curve data cannot be converted to raw voltage");
            }

            double voltageConversionConstant = (readInSensitivityKnown)? 1/sensitivityReadIn : 1/sensitivityRequested;

            return voltageConversionConstant;
        }

        @Override
        public double getDeflectionConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested)
        {
            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double deflectionConversionConstant = (readInSensitivityKnown && requestedSensitivityKnown) ? sensitivityRequested/sensitivityReadIn : 1;

            return deflectionConversionConstant;
        }

        @Override
        public double getForceConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested)
        {
            boolean requestedSpringConstantKnown = !Double.isNaN(springConstantRequested);

            double springConstantUsedInConversion = requestedSpringConstantKnown 
                    ? springConstantRequested : (1000*springConstantReadIn);         

            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double forceConversionConstant = (readInSensitivityKnown && requestedSensitivityKnown) ? springConstantUsedInConversion*sensitivityRequested/(sensitivityReadIn) 
                    : springConstantUsedInConversion;

            return forceConversionConstant;
        }

        @Override
        public boolean canBeUsedForCalibration(double sensitivityReadIn, double springConstantReadIn)
        {
            boolean canBeUsed = !Double.isNaN(sensitivityReadIn);
            return canBeUsed;
        }

        @Override
        public boolean isUnitCompatible(PrefixedUnit unit)
        {
            return  StandardQuantityTypes.LENGTH.isCompatible(unit);
        }
    }, 

    FORCE_CALIBRATED(Quantities.FORCE_NANONEWTONS)
    {
        @Override
        public double getPhotodiodeValueConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) throws UserCommunicableException
        {
            //we have to use the spring constant that was used to convert raw deflection to force values
            //so we will use the one read-in from the file, if it is known

            boolean readInSpringConstantKnown = !Double.isNaN(springConstantReadIn);
            boolean requestedSpringConstantKnown = !Double.isNaN(springConstantRequested);

            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            boolean necessaryDataKnown = (readInSpringConstantKnown || requestedSpringConstantKnown)
                    &&(readInSensitivityKnown || requestedSensitivityKnown);

            if(!necessaryDataKnown)
            {
                throw new UserCommunicableException("The curve data cannot be converted to raw voltage");
            }

            double springConstantUsedInConversion = readInSpringConstantKnown ? (1000*springConstantReadIn): springConstantRequested;         

            double voltageConversionConstant = (readInSensitivityKnown) ? 1/(sensitivityReadIn*springConstantUsedInConversion) : 1/(sensitivityRequested*springConstantUsedInConversion);

            return voltageConversionConstant;
        }

        @Override
        public double getDeflectionConversionFactor(double sensitivityReadIn, double springConstantReadIn,double sensitivityRequested, double springConstantRequested) 
        {
            boolean readInSpringConstantKnown = !Double.isNaN(springConstantReadIn);

            double springConstantUsedInConversion = readInSpringConstantKnown ? (1000*springConstantReadIn): springConstantRequested;         

            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            double deflectionConversionConstant = (readInSensitivityKnown && requestedSensitivityKnown) ? sensitivityRequested/(sensitivityReadIn*springConstantUsedInConversion) : 1/springConstantUsedInConversion;

            return deflectionConversionConstant;
        }

        @Override
        public double getForceConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) 
        {
            boolean readInSpringConstantKnown = !Double.isNaN(springConstantReadIn);
            boolean requestedSpringConstantKnown = !Double.isNaN(springConstantRequested);

            double forceConversionConstant = (readInSpringConstantKnown && requestedSpringConstantKnown) ? springConstantRequested/(1000*springConstantReadIn) : 1;

            boolean readInSensitivityKnown = !Double.isNaN(sensitivityReadIn);
            boolean requestedSensitivityKnown = !Double.isNaN(sensitivityRequested);

            forceConversionConstant = (readInSensitivityKnown && requestedSensitivityKnown) ? forceConversionConstant * (sensitivityRequested/sensitivityReadIn): forceConversionConstant;

            return forceConversionConstant;
        }

        @Override
        public boolean canBeUsedForCalibration(double sensitivityReadIn, double springConstantReadIn) 
        {
            boolean canBeUsed = !Double.isNaN(springConstantReadIn) && !Double.isNaN(sensitivityReadIn);

            return canBeUsed;
        }

        @Override
        public boolean isUnitCompatible(PrefixedUnit unit) 
        {
            return StandardQuantityTypes.FORCE.isCompatible(unit);
        }
    };

    private final Quantity defaultQuantity;

    CalibrationState(Quantity defaultQuantity)
    {
        this.defaultQuantity = defaultQuantity;
    }

    public Quantity getDefaultYQuantity()
    {
        return defaultQuantity;
    }

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value, and sensitivity in
    //micrometers per volt (10^6 times more then the SI value) or micrometers per ampere
    public abstract double getPhotodiodeValueConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested) throws UserCommunicableException;

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value
    public abstract double getDeflectionConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested);

    //spring constant should be in nN/micron, i.e. 1000 times more then the SI value
    public abstract double getForceConversionFactor(double sensitivityReadIn, double springConstantReadIn, double sensitivityRequested, double springConstantRequested);

    public abstract boolean canBeUsedForCalibration(double sensitivityReadIn, double springConstantReadIn);
    public abstract boolean isUnitCompatible(PrefixedUnit unit);

    public static Quantity getDefaultYQuantity(PrefixedUnit ySignalUnit)
    {
        Quantity yQuantity = null;

        for(CalibrationState state : CalibrationState.values())
        {
            if(state.isUnitCompatible(ySignalUnit))
            {
                return state.getDefaultYQuantity();
            }
        }

        return yQuantity;
    }

    public static boolean isCompatibleWithAnyCalibrationState(PrefixedUnit ySignalUnit)
    {
        boolean compatible = false;

        for(CalibrationState state : CalibrationState.values())
        {
            if(state.isUnitCompatible(ySignalUnit))
            {
                compatible = true;
                break;
            }
        }

        return compatible;
    }

    public static CalibrationState getCalibrationState(Channel1D approachChannel, Channel1D withdrawChannel)
    {
        Quantity yQuantityApproach = approachChannel.getYQuantity();
        Quantity yQuantityWithdraw = withdrawChannel.getYQuantity();

        for(CalibrationState state : CalibrationState.values())
        {
            if(state.isUnitCompatible(yQuantityApproach.getUnit()) && state.isUnitCompatible(yQuantityWithdraw.getUnit()))
            {
                return state;
            }
        }

        throw new IllegalArgumentException("No calibration state corresponds to units " + yQuantityApproach.getUnit().toString() + " and " + yQuantityWithdraw.getUnit().toString());
    }
}