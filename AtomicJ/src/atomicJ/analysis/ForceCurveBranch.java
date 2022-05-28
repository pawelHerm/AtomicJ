package atomicJ.analysis;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.sources.SimpleSpectroscopySource;

public enum ForceCurveBranch 
{
    //we use a look-behind regex construct to make sure 'trace' is not found in strings containing 'retrace'
    APPROACH(SimpleSpectroscopySource.APPROACH, Arrays.asList(Pattern.compile("extend"), Pattern.compile("approach"), Pattern.compile("(?<!(re))trace")), SortedArrayOrder.DESCENDING, SortedArrayOrder.ASCENDING),
    WITHDRAW(SimpleSpectroscopySource.WITHDRAW, Arrays.asList(Pattern.compile("retract"), Pattern.compile("withdraw"),Pattern.compile("retrace")), SortedArrayOrder.ASCENDING, SortedArrayOrder.DESCENDING);

    private final String prettyName;
    private final List<Pattern> lowerCaseKeywords;
    private final SortedArrayOrder defaultOrderLeft;
    private final SortedArrayOrder defaultOrderRight;

    private ForceCurveBranch(String prettyName, List<Pattern> lowerCaseKeywords, SortedArrayOrder defaultOrderLeft, SortedArrayOrder defaultOrderRight)
    {
        this.prettyName = prettyName;
        this.lowerCaseKeywords = lowerCaseKeywords;
        this.defaultOrderLeft = defaultOrderLeft;
        this.defaultOrderRight = defaultOrderRight;
    }

    public static ForceCurveBranch guessBranch(double[][] sortedBranch)
    {
        ForceCurveOrientation orientation = ForceCurveOrientation.resolveOrientation(sortedBranch);
        return orientation.guessBranch(sortedBranch);
    }

    public static ForceCurveBranch getValue(String identifier, ForceCurveBranch fallBackValue)
    {
        ForceCurveBranch branch = fallBackValue;

        if(identifier != null)
        {
            for(ForceCurveBranch br : ForceCurveBranch.values())
            {
                String estIdentifier =  br.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    branch = br;
                    break;
                }
            }
        }

        return branch;
    }

    public static ForceCurveBranch guessBranchFromTextualDescription(String description)
    {
        ForceCurveBranch branch = null;

        if(description != null)
        {
            String descriptionLowerCase = description.toLowerCase();

            for(ForceCurveBranch br : ForceCurveBranch.values())
            {
                List<Pattern> keywords =  br.lowerCaseKeywords;
                for(Pattern keyword : keywords)
                {
                    Matcher m = keyword.matcher(descriptionLowerCase);
                    //if we were to use the contains method of the String objects, we would have to deal with the fact that "retrace" contains "trace"
                    if(m.find())
                    {
                        branch = br;
                        break;
                    }
                }
            }
        }


        return branch;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public SortedArrayOrder getDefaultXOrderForLeftHandSideContactOrientation()
    {
        return defaultOrderLeft;
    }

    public SortedArrayOrder getDefaultXOrderForRightHandSideContactOrientation()
    {
        return defaultOrderRight;
    }

    public String getIdentifier()
    {
        return name();
    }
}
