package atomicJ.readers.regularImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;
import atomicJ.gui.AbstractModel;

public class ImageInterpretationModel extends AbstractModel
{
    private static final String TASK_NAME = "Image interpretation";
    private static final String TASK_DESCRIPTION = "<html>Specify how to interpret image as AFM recording</html>";

    public static final String IMAGE_WIDTH = "ImageWidth";
    public static final String IMAGE_HEIGHT = "ImageHeight";

    public static final String IMAGE_WIDTH_UNIT = "ImageWidthUnit";
    public static final String IMAGE_HEIGHT_UNIT = "ImageHeightUnit";

    public static final String ASPECT_RATIO_CONSTANT = "AspectRatioConstant";

    public static final String COMBINE_CHANNELS = "CombineChannels";
    public static final String CHANNEL_COMBINATION_FRACTION = "ChannelCombinationFraction";

    public static final String CHANNEL_Z_QUANTITY_NAME = "ChannelZQuantityName";
    public static final String CHANNEL_Z_QUANTITY_PREFIX = "ChannelZQuantityPrefix";
    public static final String CHANNEL_Z_QUANTITY_UNIT_NAME = "ChannelZQuantityUnitName";

    public static final String COMBINED_Z_QUANTITY_NAME = "CombinedZQuantityName";
    public static final String COMBINED_Z_QUANTITY_PREFIX = "CombinedZQuantityPrefix";
    public static final String COMBINED_Z_QUANTITY_UNIT_NAME = "CombinedZQuantityUnitName";

    public static final String READ_IN_ROIS = "ReadInROIs";
    public static final String USE_READIN_COLOR_GRADIENTS = "UseReadInColorGradients";

    public static final String FINISH_ENABLED = "FinishEnabled";

    private final int imageRowCount;
    private final int imageColumnCount;
    private final int frameCount;
    private final String colorSpace;
    private final List<String> channels = new ArrayList<>();

    private Double imageWidth = Double.NaN;
    private Double imageHeight = Double.NaN;

    private PrefixedUnit imageWidthUnit = Units.MICRO_METER_UNIT;
    private PrefixedUnit imageHeightUnit = Units.MICRO_METER_UNIT;

    private boolean combineChannels;
    private final Map<String, Double> combinationCoefficients = new LinkedHashMap<>();

    private final Map<String, SIPrefix> zQuantityPrefices = new LinkedHashMap<>();
    private final Map<String, String> zQuantityUnitNames = new LinkedHashMap<>();
    private final Map<String, String> zQuantityNames = new LinkedHashMap<>();

    private boolean readInROIs = true;
    private boolean useReadInColorGradients = true;
    private final boolean readInROIsAvailable;
    private final boolean readInColorGradientsAvailable;

    private String combinedZQuantityName = "";
    private SIPrefix combinedZQuantityPrefix = SIPrefix.u;
    private String combinedZQuatityUnitName = "m";

    private boolean finishEnabled;

    private static final PrefixedUnit[] units = new PrefixedUnit[] {Units.METER_UNIT, Units.CENTI_METER_UNIT, Units.MILI_METER_UNIT, Units.MICRO_METER_UNIT,
            Units.NANO_METER_UNIT, Units.PICO_METER_UNIT};

    private static final String[] unitBareNames = new String[] {"", "m","V","N","Pa","g","s","A","Hz","J","eV",
            "W","C","F","deg","rad","Arb"};

    private boolean aspectRatioConstant = true;
    private double aspectRatio;

    private boolean approved;

    public ImageInterpretationModel(int frameCount, int imageRowCount, int imageColumnCount, UnitExpression xLength, UnitExpression yLength,
            String colorSpaceName, List<String> channels, boolean readInROIsAvailable, boolean readInColorGradientsAvailable)
    {
        this.frameCount = frameCount;
        this.imageRowCount = imageRowCount;
        this.imageColumnCount = imageColumnCount;
        this.colorSpace = colorSpaceName;
        this.channels.addAll(channels);

        boolean xLengthOK = (xLength != null) && xLength.getValue() > 0;
        boolean yLengthOK = (yLength != null) && yLength.getValue() > 0;

        UnitExpression xLengthFinalForm = (xLengthOK) ? xLength.deriveSimpleForm() : null;
        UnitExpression yLengthFinalForm = (yLengthOK) ? yLength.derive(xLengthFinalForm.getUnit()) : null;

        this.aspectRatio = (xLengthOK && yLengthOK) ? xLengthFinalForm.getValue()/yLengthFinalForm.getValue() : (double)imageColumnCount/(double)imageRowCount;
        this.imageWidth = xLengthOK ? xLengthFinalForm.getValue() : (yLengthOK ? aspectRatio*yLengthFinalForm.getValue(): 1.);
        this.imageHeight = yLengthOK ? yLengthFinalForm.getValue() : imageWidth/aspectRatio;

        this.imageWidthUnit = xLengthOK ? xLengthFinalForm.getUnit() : Units.MICRO_METER_UNIT;
        this.imageHeightUnit = yLengthOK ? yLengthFinalForm.getUnit() : Units.MICRO_METER_UNIT;

        this.readInROIsAvailable = readInROIsAvailable;
        this.readInColorGradientsAvailable = readInColorGradientsAvailable;

        initMultipleChannelProperties(channels);

        this.finishEnabled = checkIfFinishEnabled();
    }

    public static ImageInterpretationModel getInstance(ChannelProvider channelProvider, int frameCount, boolean readInROIsAvailable, boolean readInColorGradientsAvailable)
    {        
        return new ImageInterpretationModel(frameCount, channelProvider.getRowCount(), channelProvider.getColumnCount(), channelProvider.getXLength(), channelProvider.getYLength(),
                channelProvider.getColorSpaceName(), channelProvider.getChannelNames(), readInROIsAvailable, readInColorGradientsAvailable);       
    }

    private void updateAspectRatio()
    {
        this.aspectRatio = imageWidth/imageHeight;
    }

    public String getTaskName()
    {
        return TASK_NAME;
    }

    public String getTaskDescription()
    {
        return TASK_DESCRIPTION;
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public boolean isApproved()
    {
        return approved;
    }

    public void finish()
    {
        this.approved = true;
    }

    public void cancel()
    {
        this.approved = false;
    }

    private void initMultipleChannelProperties(List<String> channels)
    {
        for(String channel : channels)
        {
            combinationCoefficients.put(channel, Double.valueOf(1));
            zQuantityPrefices.put(channel, SIPrefix.Empty);
            zQuantityUnitNames.put(channel, "");
            zQuantityNames.put(channel, channel);
        }
    }

    public int getImageRowCount()
    {
        return imageRowCount;
    }

    public int getImageColumnCount()
    {
        return imageColumnCount;
    }

    public String getColorSpace()
    {
        return colorSpace;
    }

    public List<String> getChannelNames()
    {
        return new ArrayList<>(channels);
    }

    public boolean isCombineChannels()
    {
        return combineChannels;
    }

    public boolean isROIsAvailableToRead()
    {
        return readInROIsAvailable;
    }

    public boolean isReadInROIs()
    {
        return readInROIs;
    }

    public void setReadInROIs(boolean readInROIsNew)
    {
        boolean readInROIsOld = this.readInROIs;
        this.readInROIs = readInROIsNew;

        firePropertyChange(READ_IN_ROIS, readInROIsOld, readInROIsNew);
    }

    public boolean isReadInColorGradientsAvailable()
    {
        return readInColorGradientsAvailable;
    }

    public boolean isUseReadInColorGradients()
    {
        return useReadInColorGradients;
    }

    public void setUseReadInColorGradients(boolean useReadInColorGradientsNew)
    {
        boolean useReadInColorGradientsOld = this.useReadInColorGradients;
        this.useReadInColorGradients = useReadInColorGradientsNew;

        firePropertyChange(USE_READIN_COLOR_GRADIENTS, useReadInColorGradientsOld, useReadInColorGradientsNew);
    }

    public boolean isAspectRatioConstant()
    {
        return aspectRatioConstant;
    }

    public void setAspectRatioConstant(boolean aspectRatioConstantNew)
    {
        boolean aspectRatioConstantOld = this.aspectRatioConstant;
        this.aspectRatioConstant = aspectRatioConstantNew;

        firePropertyChange(ASPECT_RATIO_CONSTANT, aspectRatioConstantOld, aspectRatioConstantNew);
    }

    public void setCombineChannels(boolean combineChannelsNew)
    {
        boolean combineChannelsOld = this.combineChannels;
        this.combineChannels = combineChannelsNew;

        firePropertyChange(COMBINE_CHANNELS, combineChannelsOld, combineChannelsNew);

        checkIfFinishEnabled();
    }

    public double getCombinationCoefficient(String channel)
    {
        if(!combinationCoefficients.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        return combinationCoefficients.get(channel);
    }

    public void setCombinationCoefficient(String channel, double fractionNew)
    {
        if(!combinationCoefficients.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        double fractionOld = combinationCoefficients.get(channel);
        combinationCoefficients.put(channel, fractionNew);

        String propertyName = CHANNEL_COMBINATION_FRACTION + channel;
        firePropertyChange(propertyName, fractionOld, fractionNew);

        checkIfFinishEnabled();
    }

    public String getChannelQuantityName(String channel)
    {
        if(!zQuantityNames.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        return zQuantityNames.get(channel);
    }

    public void setChannelQuantityName(String channel, String quantityNameNew)
    {
        if(!zQuantityNames.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        String quantityNameOld = zQuantityNames.get(channel);
        zQuantityNames.put(channel, quantityNameNew);

        String propertyName = CHANNEL_Z_QUANTITY_NAME + channel;
        firePropertyChange(propertyName, quantityNameOld, quantityNameNew);

        checkIfFinishEnabled();
    }

    public String getChannelQuantityUnitName(String channel)
    {
        if(!zQuantityUnitNames.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        return zQuantityUnitNames.get(channel);
    }

    public void setChannelQuantityUnitName(String channel, String unitNameNew)
    {
        if(!zQuantityUnitNames.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        String unitNameOld = zQuantityUnitNames.get(channel);
        zQuantityUnitNames.put(channel, unitNameNew);

        String propertyName = CHANNEL_Z_QUANTITY_UNIT_NAME + channel;
        firePropertyChange(propertyName, unitNameOld, unitNameNew);

        checkIfFinishEnabled();
    }

    public SIPrefix getChannelQuantityPrefix(String channel)
    {
        if(!zQuantityPrefices.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        return zQuantityPrefices.get(channel);
    }

    public void setChannelQuantityPrefix(String channel, SIPrefix prefixNew)
    {
        if(!zQuantityPrefices.containsKey(channel))
        {
            throw new IllegalArgumentException("'Channel' cannot be found in the color space");
        }

        SIPrefix prefixOld = zQuantityPrefices.get(channel);
        zQuantityPrefices.put(channel, prefixNew);

        String propertyName = CHANNEL_Z_QUANTITY_PREFIX + channel;
        firePropertyChange(propertyName, prefixOld, prefixNew);

        checkIfFinishEnabled();
    }

    public double[] getCombinationCoefficients()
    {
        int n = channels.size();

        double[] coefficients = new double[n];

        for(int i = 0; i<n; i++)
        {
            String channel = channels.get(i);
            coefficients[i] = combinationCoefficients.get(channel);
        }

        return coefficients;
    }

    public String getCombinedZQuantityName()
    {
        return combinedZQuantityName;
    }

    public void setCombinedZQuantityName(String combinedZQuantityNameNew)
    {
        String combinedZQuantityNameOld = this.combinedZQuantityName;
        this.combinedZQuantityName = combinedZQuantityNameNew;

        firePropertyChange(COMBINED_Z_QUANTITY_NAME, combinedZQuantityNameOld, combinedZQuantityNameNew);

        checkIfFinishEnabled();
    }

    public SIPrefix getCombinedZQuantityPrefix()
    {
        return combinedZQuantityPrefix;
    }

    public void setCombinedZQuantityPrefix(SIPrefix combinedZQuantityPrefixNew)
    {
        SIPrefix combinedZQuantityPrefixOld = this.combinedZQuantityPrefix;
        this.combinedZQuantityPrefix = combinedZQuantityPrefixNew;

        firePropertyChange(COMBINED_Z_QUANTITY_PREFIX, combinedZQuantityPrefixOld, combinedZQuantityPrefixNew); 
    }

    public String getCombinedZQuatityUnitName()
    {
        return combinedZQuatityUnitName;
    }

    public void setCombinedZQuatityUnitName(String combinedZQuantityUnitNameNew)
    {
        String combinedZQuantityUnitNameOld = this.combinedZQuatityUnitName;
        this.combinedZQuatityUnitName = combinedZQuantityUnitNameNew;

        firePropertyChange(COMBINED_Z_QUANTITY_UNIT_NAME, combinedZQuantityUnitNameOld, combinedZQuantityUnitNameNew);
    }

    public Quantity getCombinedZQuantity()
    {
        Quantity combinedZQuantity = combinedZQuatityUnitName.isEmpty() ?
                new DimensionlessQuantity(combinedZQuantityName,  new SimplePrefixedUnit("", combinedZQuantityPrefix)):
                    new UnitQuantity(combinedZQuantityName, new SimplePrefixedUnit(combinedZQuatityUnitName, combinedZQuantityPrefix));

                return combinedZQuantity;
    }

    public List<Quantity> getSeparateZQuantities()
    {
        List<Quantity> quantities = new ArrayList<>();

        for(String channel : channels)
        {
            String quantityName = zQuantityNames.get(channel);
            SIPrefix prefix = zQuantityPrefices.get(channel);
            String unitName = zQuantityUnitNames.get(channel);

            Quantity quantity = unitName.isEmpty() ? new DimensionlessQuantity(quantityName, new SimplePrefixedUnit("", prefix)) : new UnitQuantity(quantityName, new SimplePrefixedUnit(unitName, prefix));

            quantities.add(quantity);
        }

        return quantities;
    }

    public List<Quantity> getZQuatities()
    {  
        List<Quantity> zQuantities = combineChannels ? Collections.singletonList(getCombinedZQuantity()) :getSeparateZQuantities();        
        return zQuantities;
    }

    public Double getImageWidth()
    {
        return imageWidth;
    }

    public void setImageWidth(Double imageWidthNew)
    {
        if(imageWidthNew == null)
        {
            return;
        }

        double imageWidthOld = this.imageWidth;
        this.imageWidth = imageWidthNew;

        firePropertyChange(IMAGE_WIDTH, imageWidthOld, imageWidthNew);

        checkIfFinishEnabled();
    }

    public void specifyImageWidth(Double imageWidthNew)
    {
        setImageWidth(imageWidthNew);

        if(aspectRatioConstant)
        {
            double heightNew = this.imageWidth/aspectRatio;
            setImageHeight(heightNew);
        }
        else
        {
            updateAspectRatio();
        }
    }

    public Double getImageHeight()
    {
        return imageHeight;
    }

    private void setImageHeight(Double imageHeigtNew)
    {
        if(imageHeigtNew == null)
        {
            return;
        }

        double imageHeightOld = this.imageHeight;
        this.imageHeight = imageHeigtNew;

        firePropertyChange(IMAGE_HEIGHT, imageHeightOld, imageHeigtNew);

        checkIfFinishEnabled();
    }

    public void specifyImageHeight(Double imageHeigtNew)
    {
        setImageHeight(imageHeigtNew);

        if(aspectRatioConstant)
        {
            double widthNew = this.imageHeight*aspectRatio;
            setImageWidth(widthNew);
        }
        else
        {
            updateAspectRatio();
        }
    }


    public PrefixedUnit getImageWidthUnit()
    {
        return imageWidthUnit;
    }

    public void specifyImageWidthUnit(PrefixedUnit imageWidthUnitNew)
    {
        if(imageWidthUnitNew == null)
        {
            return;
        }

        PrefixedUnit imageWidthUnitOld = this.imageWidthUnit;
        this.imageWidthUnit = imageWidthUnitNew;

        firePropertyChange(IMAGE_WIDTH_UNIT, imageWidthUnitOld, imageWidthUnitNew);

        checkIfFinishEnabled();
    }

    public PrefixedUnit getImageHeightUnit()
    {
        return imageHeightUnit;
    }

    private void setImageHeightUnit(PrefixedUnit imageHeigtUnitNew)
    {
        if(imageHeigtUnitNew == null)
        {
            return;
        }

        PrefixedUnit imageHeightUnitOld = this.imageHeightUnit;
        this.imageHeightUnit = imageHeigtUnitNew;

        firePropertyChange(IMAGE_HEIGHT_UNIT, imageHeightUnitOld, imageHeigtUnitNew);

        checkIfFinishEnabled();      
    } 

    public void specifyImageHeightUnit(PrefixedUnit imageHeigtUnitNew)
    {
        setImageHeightUnit(imageHeigtUnitNew);
    }

    public static PrefixedUnit[] getSIUnits()
    {
        return units;
    }

    public static String[] getZQuantityUnitNames()
    {
        return unitBareNames;
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private boolean checkIfFinishEnabled()
    {
        boolean finishEnabledNew = true;

        finishEnabledNew = finishEnabledNew && !Double.isNaN(imageWidth) && !Double.isNaN(imageHeight);
        finishEnabledNew = finishEnabledNew && (imageWidthUnit != null) && (imageHeightUnit != null);

        if(combineChannels)
        {
            finishEnabledNew = finishEnabledNew && checkIfCombinationFactorsSpecified()
                    && combinedZQuantityName != null && !combinedZQuantityName.isEmpty();
        }
        else
        {
            finishEnabledNew = finishEnabledNew && checkIfChannelQuantityNamesSpecified();
        }

        boolean finishEnabledOld = this.finishEnabled;
        this.finishEnabled = finishEnabledNew;

        firePropertyChange(FINISH_ENABLED, finishEnabledOld, finishEnabledNew);

        return finishEnabledNew;
    }

    private boolean checkIfCombinationFactorsSpecified()
    {
        boolean specified = true;

        for(String channel : channels)
        {
            Double factor = combinationCoefficients.get(channel);

            specified = specified && (factor != null && !factor.isNaN() && !factor.isInfinite());

            if(!specified)
            {
                break;
            }
        }

        return specified;
    }

    private boolean checkIfChannelQuantityNamesSpecified()
    {
        boolean specified = true;

        for(String channel : channels)
        {
            String name = zQuantityNames.get(channel);

            specified = specified && (name != null && !name.isEmpty());

            if(!specified)
            {
                break;
            }
        }

        return specified;
    }

    public static boolean areCompatible(List<ChannelProvider> channelProviders)
    {
        if(channelProviders.size() < 2)
        {
            return true;
        }

        ChannelProvider firstChannelProvider = channelProviders.get(0);

        int firstColumnCount = firstChannelProvider.getColumnCount();
        int firstRowCount = firstChannelProvider.getRowCount();

        List<String> firstChannelNames = firstChannelProvider.getChannelNames();

        for(ChannelProvider channelProvider : channelProviders)
        {            
            if(firstColumnCount != channelProvider.getColumnCount())
            {
                return false;
            }
            if(firstRowCount != channelProvider.getRowCount())
            {
                return false;
            }            
            if(!firstChannelNames.equals(channelProvider.getChannelNames()))
            {
                return false;
            }
        }

        return true;
    }
}
