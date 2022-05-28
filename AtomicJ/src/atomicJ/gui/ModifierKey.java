package atomicJ.gui;

import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Set;

public enum ModifierKey
{
    ALT, ALT_GRAPH, SHIFT, CONTROL;

    public static Set<ModifierKey> getModifierKeys(MouseEvent event)
    {
        Set<ModifierKey> modifierKeys = EnumSet.noneOf(ModifierKey.class);

        if(event.isAltDown())
        {
            modifierKeys.add(ALT);
        }
        if(event.isAltGraphDown())
        {
            modifierKeys.add(ALT_GRAPH);
        }
        if(event.isShiftDown())
        {
            modifierKeys.add(SHIFT);
        }
        if(event.isControlDown())
        {
            modifierKeys.add(CONTROL);
        }
        return modifierKeys;
    }
}