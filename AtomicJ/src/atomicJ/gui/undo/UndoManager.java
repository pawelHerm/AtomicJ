package atomicJ.gui.undo;

import java.util.LinkedHashMap;
import java.util.Map;


public class UndoManager 
{
    private int sizeLimit;
    private final Map<String, UndoStack> stacks = new LinkedHashMap<>();

    public UndoManager(int sizeLimit)
    {
        this.sizeLimit = sizeLimit;
    }

    public int getSizeLimit()
    {
        return sizeLimit;
    }

    public void setSizeLimit(int sizeLimit)
    {
        this.sizeLimit = sizeLimit;

        for(UndoStack stack : stacks.values())
        {
            stack.setSizeLimit(sizeLimit);
        }
    }

    public void clear()
    {
        stacks.clear();
    }

    public void push(String type, UndoableCommand command)
    {
        UndoStack stack = stacks.get(type);
        if(stack == null)
        {
            stack = new UndoStack(sizeLimit);
            stacks.put(type, stack);
        }
        stack.push(command);
    }

    public boolean canBeRedone(String type)
    {
        boolean canBeRedone = false;

        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            canBeRedone = stack.canBeRedone();
        }

        return canBeRedone;
    }

    public void redo(String type)
    {
        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            stack.redo();
        }
    }

    public CommandIdentifier getCommandToRedoCompundIdentifier(String type)
    {
        CommandIdentifier id = null;

        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            id = stack.getCommandToRedoCompoundIdentifier();
        }

        return id;
    }


    public boolean canBeUndone(String type)
    {
        boolean canBeUndone = false;

        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            canBeUndone = stack.canBeUndone();
        }

        return canBeUndone;
    }


    public void undo(String type)
    {
        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            stack.undo();
        }
    }

    public CommandIdentifier getCommandToUndoCompundIdentifier(String type)
    {
        CommandIdentifier id = null;

        UndoStack stack = stacks.get(type);
        if(stack != null)
        {
            id = stack.getCommandToUndoCompundIdentifier();
        }

        return id;
    }

}
