package atomicJ.gui;

import java.util.prefs.Preferences;

import javax.swing.table.TableCellRenderer;

public interface DecimalTableCellRenderer extends NumericalFormatStyle, TableCellRenderer
{
    public String getValue(Object entry, int modelColumn);
    public void setPreferences(Preferences pref);
}
