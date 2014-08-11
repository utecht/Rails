package rails.game.action;

import java.io.IOException;
import java.io.ObjectInputStream;

import net.sf.rails.game.special.SpecialProperty;


/**
 * This class can only be used to offer a Special Property to the UI that does
 * NOT need any return parameters. Example: the M&H/NYC swap in 1830.
 * @author Erik Vos
 */
public class UseSpecialProperty extends PossibleORAction {

    /*--- Preconditions ---*/

    /** The special property that could be used */
    transient protected SpecialProperty specialProperty = null;
    private int specialPropertyId;

    /*--- Postconditions ---*/

    public UseSpecialProperty(SpecialProperty specialProperty) {
        super();
        this.specialProperty = specialProperty;
        if (specialProperty != null)
            this.specialPropertyId = specialProperty.getUniqueId();
    }

    public static final long serialVersionUID = 1L;

    /**
     * @return Returns the specialProperty.
     */
    public SpecialProperty getSpecialProperty() {
        return specialProperty;
    }

    public boolean equalsAsOption(PossibleAction action) {
        if (!(action instanceof UseSpecialProperty)) return false;
        UseSpecialProperty a = (UseSpecialProperty) action;
        return a.specialProperty == specialProperty;
    }

    public boolean equalsAsAction(PossibleAction action) {
        return action.equalsAsOption(this);
    }

    public String toString() {
        StringBuffer b = new StringBuffer("UseSpecialProperty: ");
        if (specialProperty != null) b.append(specialProperty);
        return b.toString();
    }

    public String toMenu() {
        return specialProperty.toMenu();
    }

    /** Deserialize */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        in.defaultReadObject();

        if (specialPropertyId > 0) {
            specialProperty = SpecialProperty.getByUniqueId(specialPropertyId);
        }
    }

}