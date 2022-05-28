package atomicJ.gui;

public interface NumberFormatReceiver 
{
    public boolean isTickLabelTrailingZeroes();
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes);
    public boolean isTickLabelGroupingUsed();
    public void setTickLabelGroupingUsed(boolean used);
    public char getTickLabelGroupingSeparator();
    public void setTickLabelGroupingSeparator(char separatorNew);
    public char getTickLabelDecimalSeparator();
    public void setTickLabelDecimalSeparator(char separator);
}
