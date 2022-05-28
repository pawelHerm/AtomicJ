package atomicJ.readers.innova;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;
import atomicJ.readers.innova.InnovaChannelHeader.InnovaBranchTag;
import atomicJ.utilities.OrderedPair;

public class InnovaImageDataParameters
{
    public static final String NAME = "Data Parameters";

    private static final String DATA_NAME = "DataName";
    private static final String DATA_OFFSET = "DataOffset";
    private static final String SCAN_RANGE_X = "ScanRangeX";
    private static final String SCAN_RANGE_Y = "ScanRangeY";
    private static final String OFFSET_X = "OffsetX";
    private static final String OFFSET_Y = "OffsetY";

    private static final String RESOLUTION_X = "ResolutionX";
    private static final String RESOLUTION_Y = "ResolutionY";

    private static final String Z_TRANSFER_COEFFICIENT = "ZTransferCoefficient";

    private final UnitExpression scanRangeX;
    private final UnitExpression scanRangeY;
    private final UnitExpression offsetX;
    private final UnitExpression offsetY;

    private final int resolutionX;
    private final int resolutionY;

    private final UnitExpression zTransferCoefficient;
    private final PrefixedUnit zUnit;

    private final String dataName;
    private final int dataOffset;
    private final List<InnovaChannelHeader> channelHeaders = new ArrayList<>();

    public InnovaImageDataParameters(InnovaINISection dataSection)
    {        
        Map<String, String> keyValuePairs = dataSection.getKeyValuePairs();

        this.offsetX = keyValuePairs.containsKey(OFFSET_X) ? UnitExpression.parse(keyValuePairs.get(OFFSET_X)): null;
        this.offsetY = keyValuePairs.containsKey(OFFSET_Y) ? UnitExpression.parse(keyValuePairs.get(OFFSET_Y)): null;

        this.scanRangeX = keyValuePairs.containsKey(SCAN_RANGE_X) ? UnitExpression.parse(keyValuePairs.get(SCAN_RANGE_X)): null;
        this.scanRangeY = keyValuePairs.containsKey(SCAN_RANGE_Y) ? UnitExpression.parse(keyValuePairs.get(SCAN_RANGE_Y)): null;

        this.resolutionX = keyValuePairs.containsKey(RESOLUTION_X) ? Integer.parseInt(keyValuePairs.get(RESOLUTION_X)): -1;
        this.resolutionY = keyValuePairs.containsKey(RESOLUTION_Y) ? Integer.parseInt(keyValuePairs.get(RESOLUTION_Y)): -1;

        this.zTransferCoefficient = keyValuePairs.containsKey(Z_TRANSFER_COEFFICIENT) ? UnitExpression.parse(keyValuePairs.get(Z_TRANSFER_COEFFICIENT)): null;
        this.zUnit = zTransferCoefficient.getUnit().multiply(Units.VOLT_UNIT).simplify();

        this.dataName = keyValuePairs.containsKey(DATA_NAME) ? keyValuePairs.get(DATA_NAME): "";
        this.dataOffset = keyValuePairs.containsKey(DATA_OFFSET) ? Integer.parseInt(keyValuePairs.get(DATA_OFFSET)): -1;

        String allHeadersText = dataSection.getText().trim();        
        String[] columnHeaders = allHeadersText.split("\\t");            
        for(int i = 0; i<columnHeaders.length; i++)
        {
            String columnHeader = columnHeaders[i];
            InnovaChannelHeader column = new InnovaChannelHeader(columnHeader, i);
            channelHeaders.add(column);
        }
    }

    public OrderedPair<InnovaChannelHeader> getBranchHeaders(ForceCurveBranch branch)
    {
        OrderedPair<InnovaChannelHeader> branchHeaders = null;

        for(int i = 0; i<channelHeaders.size() - 1; i++)
        {
            InnovaChannelHeader currentHeader = channelHeaders.get(i);
            InnovaBranchTag branchTag = currentHeader.getBranchTag();

            if(branchTag != null && Objects.equals(branch, branchTag.getBranch()))
            {
                branchHeaders = new OrderedPair<>(currentHeader, channelHeaders.get(++i));
            }
        }

        return branchHeaders;
    }

    public Quantity getZQuantity()
    {
        Quantity zQuantity = new UnitQuantity(dataName, zUnit);

        return zQuantity;
    }

    public Grid2D getGrid()
    {
        Quantity distanceQuantity = Quantities.DISTANCE_MICRONS;
        double incrementX = scanRangeX.derive(distanceQuantity.getUnit()).getValue()/(resolutionX -1);
        double incrementY = scanRangeX.derive(distanceQuantity.getUnit()).getValue()/(resolutionY -1);

        Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, resolutionY, resolutionX, distanceQuantity, distanceQuantity);

        return grid;
    }

    public double getZScale()
    {
        return zTransferCoefficient.getValue();
    }

    public UnitExpression getScanRangeX()
    {
        return scanRangeX;
    }

    public UnitExpression getScanRangeY()
    {
        return scanRangeY;
    }

    public int getResolutionX()
    {
        return resolutionX;
    }

    public int getResolutionY()
    {
        return resolutionY;
    }

    public int getDataOffset()
    {
        return dataOffset;
    }

    public String getDataName()
    {
        return dataName;
    }

    public String getName()
    {
        return NAME;
    }
}
