package atomicJ.gui.imageProcessing;

public enum ImageMathOperation
{
    ADD("Add", "+"), SUBTRACT("Subtract", "-"), AVERAGE("Average", "average"), MULTIPLY("Multiply", "*"), DIVIDE("Divide", "/");

    private final String prettyName;
    private final String symbol;

    ImageMathOperation(String prettyName, String symbol)
    {
        this.prettyName = prettyName;
        this.symbol = symbol;
    }

    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }
}
