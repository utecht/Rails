package rails.game.state;

/**
 * Change associated with BooleanState
 * @author freystef
 */

final class BooleanChange extends Change {

    private final BooleanState state;
    private final boolean newValue;
    private final boolean oldValue;
    
    BooleanChange(BooleanState state,  boolean newValue) {
        super(state);
        this.state = state;
        this.newValue = newValue;
        this.oldValue = state.booleanValue();
    }

    @Override
    public void execute() {
        state.change(newValue);
    }

    @Override
    public void undo() {
        state.change(oldValue);
    }

    @Override
    public BooleanState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Change BooleanState " + state.getId() + " from " + oldValue + " to " + newValue; 
    }
        
}
