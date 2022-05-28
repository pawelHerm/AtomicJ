package atomicJ.gui.undo;

import java.util.ArrayList;
import java.util.List;

import atomicJ.utilities.Validation;


public class UndoStack
{
    private final List<UndoableCommand> stack;
    private int sizeLimit;
    private int top;

    public UndoStack(int sizeLimit)
    {
        Validation.requireValueGreaterOrEqualToParameterName(sizeLimit, 1, "sizeLimit");

        this.stack = new ArrayList<>(sizeLimit);
        this.top = -1;
        this.sizeLimit = sizeLimit;
    }

    public void push(UndoableCommand obj)
    {
        removeUndoneCommands();
        stack.add(obj);

        if(stack.size() > sizeLimit)
        {
            stack.remove(0);
        }
        else
        {
            this.top = top + 1;
        }       
    }

    private void removeUndoneCommands()
    {
        int n = stack.size();

        for(int i = n - 1; i > top; i--)
        {
            stack.remove(i);
        }
    }

    public boolean canBeRedone()
    {
        boolean canBeRedone = false;

        if(stack.size() > top + 1)
        {
            UndoableCommand command = stack.get(top + 1);
            canBeRedone = command.canBeRedone();
        }

        return canBeRedone;
    }

    public void redo()
    {
        if(stack.size() > top + 1)
        {
            UndoableCommand command = stack.get(++top);
            command.redo();
        }
    }

    public boolean canBeUndone()
    {
        boolean canBeUndone = false;
        if (top >= 0)
        {
            UndoableCommand command = stack.get(top);
            canBeUndone = command.canBeUndone();
        }

        return canBeUndone;
    }

    public void undo()
    {
        if (top >= 0)
        {
            UndoableCommand command = stack.get(top--);
            command.undo();
        }
    }

    public CommandIdentifier getCommandToUndoCompundIdentifier()
    {
        CommandIdentifier id = null;

        if (top >= 0)
        {
            UndoableCommand command = stack.get(top);
            id = command.canBeUndone() ? command.getCompundCommandIdentifier() : null;

        }

        return id;
    }

    public CommandIdentifier getCommandToRedoCompoundIdentifier()
    {
        CommandIdentifier id = null;
        if(stack.size() > top + 1)
        {
            UndoableCommand command = stack.get(top + 1);
            id = command.canBeRedone() ? command.getCompundCommandIdentifier() : null;
        }

        return id;
    }

    public int sizeLimit()
    {
        return sizeLimit;
    }

    public void setSizeLimit(int sizeLimit)
    {
        this.sizeLimit = sizeLimit;
    }

    public int elements()
    {
        return top + 1;
    }
}
