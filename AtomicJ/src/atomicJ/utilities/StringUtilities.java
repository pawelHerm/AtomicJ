package atomicJ.utilities;

public class StringUtilities
{
    //https://en.wiktionary.org/wiki/Appendix:Unicode/Superscripts_and_Subscripts
    private static final String[] UNICODE_SUBSCRIPTS_INTEGERS = {"\u2080"/*zero*/,"\u2081"/*one*/,"\u2082"/*two*/,"\u2083"/*three*/,
            "\u2084"/*four*/,"\u2085"/*five*/,"\u2086"/*six*/,"\u2087"/*seven*/,"\u2088"/*eight*/,"\u2089"/*nine*/};

    private static final String SUBSCRIPT_PLUS = "\u208A";
    private static final String SUBSCRIPT_MINUS = "\u208B";

    private static final String[] UNICODE_SUPERSCRIPTS_INTEGERS = {"\u2070"/*zero*/,"\u2071"/*one*/,"\u2072"/*two*/,"\u2073"/*three*/,
            "\u2074"/*four*/,"\u2075"/*five*/,"\u2076"/*six*/,"\u2077"/*seven*/,"\u2078"/*eight*/,"\u2079"/*nine*/};

    private static final String SUPERSCRIPT_PLUS = "\\u207A";
    private static final String SUPERSCRIPT_MINUS = "\u207B";

    public static final String toSuperScriptString(int number)
    {
        boolean negative = number < 0;
        StringBuffer buffer = new StringBuffer(negative ? SUPERSCRIPT_MINUS :"");
        buffer.reverse();

        int positiveNumber = negative ? -number : number;

        while (positiveNumber > 0) 
        {
            String currentDigit = UNICODE_SUPERSCRIPTS_INTEGERS[positiveNumber % 10];
            buffer.append(currentDigit);
            positiveNumber = positiveNumber / 10;
        }

        return buffer.toString();
    }

    public static final String toSubScriptString(int number)
    {
        boolean negative = number < 0;
        StringBuffer buffer = new StringBuffer(negative ? SUBSCRIPT_MINUS :"");
        buffer.reverse();

        int positiveNumber = negative ? -number : number;

        while (positiveNumber > 0) 
        {
            String currentDigit = UNICODE_SUBSCRIPTS_INTEGERS[positiveNumber % 10];
            buffer.append(currentDigit);
            positiveNumber = positiveNumber / 10;
        }

        return buffer.toString();
    }

    public static boolean isNullOrEmpty(String s)
    {
        boolean nullOrEmpty = (s == null) || s.length() == 0;      
        return nullOrEmpty;
    }

    public static String convertToEmptyIfNull(String s)
    {
        String string = (s == null) ? "" : s;

        return string;
    }
}
