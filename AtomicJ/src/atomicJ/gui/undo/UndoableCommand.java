package atomicJ.gui.undo;


public interface UndoableCommand
{
    public boolean isExecuted();
    public void execute();

    public void undo();
    public void redo();

    public boolean canBeUndone();
    public boolean canBeRedone();

    public CommandIdentifier getCompundCommandIdentifier();
}