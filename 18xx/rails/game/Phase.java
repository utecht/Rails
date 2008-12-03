/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/Phase.java,v 1.10 2008/12/03 20:15:14 evos Exp $ */
package rails.game;

import java.util.*;

import org.apache.log4j.Logger;

import rails.util.Tag;

public class Phase implements PhaseI {

    protected int index;

    protected String name;

    protected String colourList = "";

    protected HashMap<String, Integer> tileColours;

    protected boolean privateSellingAllowed = false;

    protected boolean privatesClose = false;

    protected int numberOfOperatingRounds = 1;

    protected int offBoardRevenueStep = 1;

    protected boolean trainTradingAllowed = false;

    /** May company buy more than one Train from the Bank per turn? */
    protected boolean oneTrainPerTurn = false;

    /** May company buy more than one Train of each type from the Bank per turn? */
    protected boolean oneTrainPerTypePerTurn = false;

    /** Previous phase, defining the current one's defaults */
    protected Phase defaults = null;

    /** Items to close if a phase gets activated */
    protected List<Closeable> closedObjects = null;

    /** A HashMap to contain phase-dependent parameters
     * by name and value.
     */
    protected Map<String, String> parameters = null;

    protected static Logger log =
            Logger.getLogger(Phase.class.getPackage().getName());

    public Phase(int index, String name, Phase previousPhase) {
        this.index = index;
        this.name = name;
        this.defaults = previousPhase;
    }

    public void configureFromXML(Tag tag) throws ConfigurationException {
        if (defaults != null) {
            colourList = defaults.colourList;
            privateSellingAllowed = defaults.privateSellingAllowed;
            numberOfOperatingRounds = defaults.numberOfOperatingRounds;
            offBoardRevenueStep = defaults.offBoardRevenueStep;
            trainTradingAllowed = defaults.trainTradingAllowed;
            oneTrainPerTurn = defaults.oneTrainPerTurn;
            oneTrainPerTypePerTurn = defaults.oneTrainPerTypePerTurn;
            if (defaults.parameters != null) {
                this.parameters = new HashMap<String, String>();
                for (String key : defaults.parameters.keySet()) {
                    parameters.put(key, defaults.parameters.get(key));
                }
            }
        }

        // String colourList;
        String[] colourArray = new String[0];
        tileColours = new HashMap<String, Integer>();

        // Allowed tile colours
        Tag tilesTag = tag.getChild("Tiles");
        if (tilesTag != null) {
            colourList = tilesTag.getAttributeAsString("colour", colourList);
        }
        if (colourList != null) colourArray = colourList.split(",");
        for (int i = 0; i < colourArray.length; i++) {
            tileColours.put(colourArray[i], null);
        }

        // Private-related properties
        Tag privatesTag = tag.getChild("Privates");
        if (privatesTag != null) {
            privateSellingAllowed =
                    privatesTag.getAttributeAsBoolean("sellingAllowed",
                            privateSellingAllowed);
            privatesClose = privatesTag.getAttributeAsBoolean("close", false);
        }

        // Operating rounds
        Tag orTag = tag.getChild("OperatingRounds");
        if (orTag != null) {
            numberOfOperatingRounds =
                    orTag.getAttributeAsInteger("number",
                            numberOfOperatingRounds);
        }

        // Off-board revenue steps
        Tag offBoardTag = tag.getChild("OffBoardRevenue");
        if (offBoardTag != null) {
            offBoardRevenueStep =
                    offBoardTag.getAttributeAsInteger("step",
                            offBoardRevenueStep);
        }

        Tag trainsTag = tag.getChild("Trains");
        if (trainsTag != null) {
            trainTradingAllowed =
                    trainsTag.getAttributeAsBoolean("tradingAllowed",
                            trainTradingAllowed);
            oneTrainPerTurn =
                    trainsTag.getAttributeAsBoolean("onePerTurn",
                            oneTrainPerTurn);
            oneTrainPerTypePerTurn =
                    trainsTag.getAttributeAsBoolean("onePerTypePerTurn",
                            oneTrainPerTypePerTurn);
        }

        Tag parameterTag = tag.getChild("Parameters");
        if (parameterTag != null) {
            if (parameters == null) parameters = new HashMap<String, String>();
            Map<String,String> attributes = parameterTag.getAttributes();
            for (String key : attributes.keySet()) {
                parameters.put (key, attributes.get(key));
            }
        }
    }

    /** Called when a phase gets activated */
    public void activate() {
        log.debug("Phase " + name + " activated");
        if (closedObjects != null && !closedObjects.isEmpty()) {
            for (Closeable object : closedObjects) {
                log.debug("Closing object " + object.toString());
                object.close();
            }
        }
    }

    public boolean isTileColourAllowed(String tileColour) {
        return tileColours.containsKey(tileColour);
    }

    public Map<String, Integer> getTileColours() {
        return tileColours;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    /**
     * @return Returns the privatesClose.
     */
    public boolean doPrivatesClose() {
        return privatesClose;
    }

    /**
     * @return Returns the privateSellingAllowed.
     */
    public boolean isPrivateSellingAllowed() {
        return privateSellingAllowed;
    }

    public boolean isTrainTradingAllowed() {
        return trainTradingAllowed;
    }

    public boolean canBuyMoreTrainsPerTurn() {
        return !oneTrainPerTurn;
    }

    public boolean canBuyMoreTrainsPerTypePerTurn() {
        return !oneTrainPerTypePerTurn;
    }

    public int getNumberOfOperatingRounds() {
        return numberOfOperatingRounds;
    }

    /**
     * @return Returns the offBoardRevenueStep.
     */
    public int getOffBoardRevenueStep() {
        return offBoardRevenueStep;
    }

    public void addObjectToClose(Closeable object) {
        if (closedObjects == null) {
            closedObjects = new ArrayList<Closeable>();
        }
        closedObjects.add(object);
    }

    public String getParameterAsString (String key) {
        if (parameters != null) {
            return parameters.get(key);
        } else {
            return null;
        }
    }

    public int getParameterAsInteger (String key) {
        String stringValue = getParameterAsString(key);
        if (stringValue == null) {
            return 0;
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (Exception e) {
            log.error ("Error while parsing parameter "+key+" in phase "+name, e);
            return 0;
        }

    }

    @Override
    public String toString() {
        return name;
    }
}
