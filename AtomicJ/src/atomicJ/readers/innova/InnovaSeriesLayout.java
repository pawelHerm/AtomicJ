package atomicJ.readers.innova;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.readers.innova.InnovaChannelHeader.InnovaBranchTag;
import atomicJ.utilities.OrderedPair;

public class InnovaSeriesLayout
{
    public static final String NAME = "Series layout";

    private static final String POINTS_NUMBER = "Points Number";
    private static final String MAX_POINTS_NUMBER = "Max Points Number";
    private static final String SERIES_NUMBER = "Series Number";

    private final int seriesCount;
    private final int pointsCount;
    private final int maxPointsCount;
    private final List<InnovaChannelHeader> channelHeaders = new ArrayList<>();

    public InnovaSeriesLayout(InnovaINISection seriesLayoutSection)
    {        
        Map<String, String> keyValuePairs = seriesLayoutSection.getKeyValuePairs();

        this.seriesCount = keyValuePairs.containsKey(SERIES_NUMBER) ? Integer.parseInt(keyValuePairs.get(SERIES_NUMBER)): 1;
        this.pointsCount = keyValuePairs.containsKey(POINTS_NUMBER) ? Integer.parseInt(keyValuePairs.get(POINTS_NUMBER)): -1;
        this.maxPointsCount = keyValuePairs.containsKey(MAX_POINTS_NUMBER) ? Integer.parseInt(keyValuePairs.get(MAX_POINTS_NUMBER)): -1;

        String allHeadersText = seriesLayoutSection.getText().trim();        
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

    public int getSeriesCount()
    {
        return seriesCount;
    }

    public int getMaximalPointCount()
    {
        int maxPointCount = maxPointsCount > -1 ? maxPointsCount : pointsCount;

        return maxPointCount;
    }

    public int getPointsCount()
    {
        return pointsCount;
    }

    public int getChannelCount()
    {
        return channelHeaders.size();
    }

    public List<InnovaChannelHeader> getChannelHeaders()
    {
        return Collections.unmodifiableList(channelHeaders);
    }

    public String getName()
    {
        return NAME;
    }
}
