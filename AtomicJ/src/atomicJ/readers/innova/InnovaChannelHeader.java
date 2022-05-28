package atomicJ.readers.innova;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;

public class InnovaChannelHeader
{
    private final String name;
    private final PrefixedUnit unit;
    private final InnovaBranchTag branchTag;

    private final int columnIndex;

    public InnovaChannelHeader(String columnHeader, int columnIndex)
    {
        String[] tokens = columnHeader.split(",");
        this.name = tokens[0].trim();
        this.unit = (tokens.length > 1) ? UnitUtilities.getSIUnit(tokens[1]) : SimplePrefixedUnit.getNullInstance();
        this.branchTag = InnovaBranchTag.getBranch(this);
        this.columnIndex = columnIndex;
    }

    public String getName()
    {
        return name;
    }

    public int getColumnIndex()
    {
        return columnIndex;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public InnovaBranchTag getBranchTag()
    {
        return branchTag;
    }

    @Override
    public String toString()
    {
        String string = name + " (" + unit.getFullName() + ")";
        return string;
    }

    public static enum InnovaBranchTag
    {
        TRACE("Trace", ForceCurveBranch.APPROACH), RETRACE("Retrace", ForceCurveBranch.WITHDRAW);

        private final String name;
        private final ForceCurveBranch branch;

        InnovaBranchTag(String name, ForceCurveBranch branch)
        {
            this.name = name;
            this.branch = branch;
        }

        public ForceCurveBranch getBranch()
        {
            return branch;
        }

        public static InnovaBranchTag getBranch(InnovaChannelHeader channelHeader)
        {
            for(InnovaBranchTag branch : InnovaBranchTag.values())
            {
                if(channelHeader.name.startsWith(branch.name))
                {
                    return branch;
                }
            }

            return null;
        }
    }
}
