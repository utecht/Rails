package rails.game.action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import net.sf.rails.game.MapHex;
import net.sf.rails.game.MapManager;
import net.sf.rails.game.Stop;
import net.sf.rails.game.special.SpecialProperty;
import net.sf.rails.game.special.SpecialBaseTokenLay;
import net.sf.rails.util.RailsObjects;
import net.sf.rails.util.Util;

/**
 * Rails 2.0: Updated equals and toString methods
 */
public class LayBaseToken extends LayToken {

    /* LayTile types */
    public final static int GENERIC = 0; // Stop-gap only
    public final static int LOCATION_SPECIFIC = 1; // Valid hex
    public final static int SPECIAL_PROPERTY = 2; // Directed by a special
    public final static int HOME_CITY = 3; // If city on home hex is undefined in 1st turn
    // property
    public final static int CORRECTION = 99; // Correction token lays

    protected int type = 0;

    /*--- Preconditions ---*/

    /*--- Postconditions ---*/

    /** The station (or city) on the hex where the token is laid */
    protected int chosenStation = 0; // Default

    public static final long serialVersionUID = 1L;

    public LayBaseToken(int type) {
        super();
        this.type = type;
    }
    
    /**
     * Lay a base token on one of a given list of locations.
     * <p>This constructor is only intended to be used for normal lays of non-home tokens
     * in the operating company LAY_TOKEN OR step.
     * 
     * @param locations A list of valid locations (hexes) where the acting company can lay a base token.<br>
     * <i>Note:</i> Currently, the game engine cannot yet provide such a list, as all knowledge about routes
     * is contained in the user interface code. As a consequence, this constructor is only called
     * with the value <b>null</b>, which allows laying a base token on <i>any</i> empty city slot.
     * In fact, the UI will now apply the restriction to valid locations only.
     * Over time, applying this restriction should be moved to the game engine.
     */
    public LayBaseToken(List<MapHex> locations) {
        super(locations);
        type = LOCATION_SPECIFIC;
    }

    /** Lay a base token as allowed via a Special Property.
     * <p>The valid locations (hexes) of such a token should be defined inside the special property.
     * Typically, such locations do not need to be connected to the existing network of a company.
     * 
     * @param specialProperty The special property that allows laying an extra or unconnected base token.
     */
    public LayBaseToken(SpecialBaseTokenLay specialProperty) {
        super(specialProperty);
        type = SPECIAL_PROPERTY;
    }

    /** Lay a base token on a given location.
     * <p> This constructor is specifically intended to allow the player to select a city for its <b>home</b> token
     * on a multi-city hex or tile (e.g. an OO tile, such as the Erie in 1830 or the THB in 1856).
     * 
     * @param hex The hex on which a city must be selected to lay a home token on.
     */
    public LayBaseToken (MapHex hex) {
        super (hex);
        setChosenHex (hex);
        type = HOME_CITY;
    }
    
    

    @Deprecated
    public int getChosenStation() {
        return chosenStation;
    }
    
    public Stop getChosenStop() {
        return chosenHex.getRelatedStop(chosenStation);
    }

    public void setChosenStation(int chosenStation) {
        this.chosenStation = chosenStation;
    }

    public int getType() {
        return type;
    }
    
    
    @Override
    public SpecialBaseTokenLay getSpecialProperty() {
        return (SpecialBaseTokenLay)specialProperty;
    }
    
    @Override
    public int getPotentialCost(MapHex hex) {
        if (hex == null) {
            return 0;
        } else {
            if (specialProperty != null && ((SpecialBaseTokenLay)specialProperty).isFree()) {
                return 0;
            } else {
                return company.getBaseTokenLayCost(hex);
            }
        }
              
    }

    @Override
    public int getCost() {
        return getPotentialCost(chosenHex);
    }

    @Override
    protected boolean equalsAs(PossibleAction pa, boolean asOption) {
        // identity always true
        if (pa == this) return true;
        //  super checks both class identity and super class attributes
        if (!super.equalsAs(pa, asOption)) return false; 

        // check asOption attributes
        LayBaseToken action = (LayBaseToken)pa; 
        boolean options = Objects.equal(this.type, action.type);
        
        // finish if asOptions check
        if (asOption) return options;
        
        // check asAction attributes
        return options
                && Objects.equal(this.chosenStation, action.chosenStation)
        ;
    }

    @Override
    public boolean isCorrection() {
        return (type == LayBaseToken.CORRECTION);
    }
    
    @Override
    public String toString() {
        return super.toString() + 
                RailsObjects.stringHelper(this)
                    .addToString("type", type)
                    .addToStringOnlyActed("chosenStation", chosenStation)
                    .toString()
        ;
    }

    /** Deserialize */
    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {

        in.defaultReadObject();

        MapManager mmgr = getRoot().getMapManager();
        locations = new ArrayList<MapHex>();
        if (Util.hasValue(locationNames)) {
            for (String hexName : locationNames.split(",")) {
                locations.add(mmgr.getHex(hexName));
            }
        }

        if (specialPropertyId > 0) {
            specialProperty =
                (SpecialBaseTokenLay) SpecialProperty.getByUniqueId(getRoot(), specialPropertyId);
        }
        if (chosenHexName != null && chosenHexName.length() > 0) {
            chosenHex = mmgr.getHex(chosenHexName);
        }
    }

}
