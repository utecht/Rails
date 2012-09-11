/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/Stop.java,v 1.12 2010/04/18 15:08:57 evos Exp $ */
package rails.game;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rails.game.Access.Loop;
import rails.game.Access.RunThrough;
import rails.game.Access.RunTo;
import rails.game.Access.ScoreType;
import rails.game.Access.StopType;
import rails.game.move.Moveable;
import rails.game.state.GenericState;
import rails.util.Util;

/**
 * A Stop object represents any junction on the map that is relevant for
 * establishing train run length and revenue calculation. A Stop object is bound
 * to (1) a MapHex, (2) to a Station object on the current Tile laid on that
 * MapHex, and (3) any tokens laid on that tile and station. <p> Each Stop has a
 * unique ID, that is derived from the MapHex name and the Stop number. The
 * initial Stop numbers are derived from the Station numbers of the preprinted
 * tile of that hex. <p> Please note, that during upgrades the Stop numbers
 * related to a city on a multiple-city hex may change: city 1 on one tile may
 * be numbered 2 on its upgrade, depending on the rotation of the upgrading
 * tile. However, the Stop numbers will not change, unless cities are merged
 * during upgrades; but even then it is attempted to retain the old Stop numbers
 * as much as possible.
 *
 * @author Erik Vos
 */
public class Stop implements TokenHolder {
    private int number;
    private String uniqueId;
    //private Station relatedStation;
    private GenericState<Station> relatedStation;
    private int slots;
    private ArrayList<TokenI> tokens;
    private MapHex mapHex;
    private String trackEdges;



    private StopType stopType = null;

    private Access accessInfo = new Access();

    protected static Logger log =
        Logger.getLogger(Stop.class.getPackage().getName());

    public Stop(MapHex mapHex, int number, Station station) {
        this.mapHex = mapHex;
        this.number = number;

        uniqueId = mapHex.getName() + "_" + number;
        relatedStation = new GenericState<Station>("City_"+uniqueId+"_station", station);
        setRelatedStation(station);

        tokens = new ArrayList<TokenI>(4);

        initStopProperties();
    }

    private void initStopProperties () {

        boolean logdetail = false;
        String loghex = "J1";

        Station station = relatedStation.get();
        int stationNumber = station.getNumber();
        TileI tile = station.getTile();
        MapManager mapManager = mapHex.getMapManager();
        TileManager tileManager = tile.getTileManager();

        /* Merge the configured stop property layers,
         * using the upper four precedence levels (see class Access).
         */
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+0+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(mapHex.getAccessInfo(stationNumber));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+1+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(tile.getAccessInfo(stationNumber));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+2+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(mapHex.getAccessInfo(0));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+3+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(tile.getAccessInfo(0));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+4a Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }

        /* Check the stop type.
         * If still null at this stage, determine it from the Station properties.
         */
        stopType = accessInfo.getStopType();
        if (stopType == null) {
            String stationType = relatedStation.get().getType();
            if (stationType.equals(Station.CITY)) {
                stopType = StopType.CITY;
            } else if (stationType.equals(Station.TOWN)) {
                stopType = StopType.TOWN;
            } else if (stationType.equals(Station.OFF_MAP_AREA)) {
                stopType = StopType.OFFMAP;
            } else if (stationType.equals(Station.PASS)) {
                stopType = StopType.CITY;
            } else {
                // The above four types seem to be all that can be assigned in ConvertTileXML.
                // If all else fails, assume City.
                stopType = StopType.CITY;
            }
        }

        /* Now merge the default stop property layers,
         * using the lower five precedence levels (see class Access).
         */
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+4b Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(mapManager.getAccessInfoDefaults(stopType));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+5+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(tileManager.getAccessInfoDefaults(stopType));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+6+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(mapManager.getAccessInfoDefaults(null));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+7+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(tileManager.getAccessInfoDefaults(null));
        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+8+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
        accessInfo.merge(stopType.getAccessInfoDefaults());

        if (logdetail && mapHex.getName().equalsIgnoreCase(loghex)) {
            log.debug("+9+ Hex="+mapHex.getName()+" tile="+tile.getId()+" city="+number
                    +": stopType="+accessInfo.getStopType()
                    +" runTo="+accessInfo.getRunToAllowed()
                    +" runThrough="+accessInfo.getRunThroughAllowed()
                    +" loop="+accessInfo.getLoopAllowed()
                    +" scoreType="+accessInfo.getScoreType()
                    +" mutex="+accessInfo.getTrainMutexID());
        }
    }

    public String getName() {
        return mapHex.getName() + "/" + number;
    }

    /**
     * @return Returns the holder.
     */
    public MapHex getHolder() {
        return mapHex;
    }

    public int getNumber() {
        return number;
    }

    public Station getRelatedStation() {
        return relatedStation.get();
    }

    public void setRelatedStation(Station relatedStation) {
        this.relatedStation.set(relatedStation);
        slots = relatedStation.getBaseSlots();
        trackEdges =
            mapHex.getConnectionString(mapHex.getCurrentTile(),
                    mapHex.getCurrentTileRotation(),
                    relatedStation.getNumber());
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    /**
     * @return Returns the id.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    public boolean addToken(TokenI token, int position) {

        if (tokens.contains(token)) return false;

        boolean result = Util.addToList(tokens, token, position);
        if (result) token.setHolder(this);
        return result;
    }

    public boolean addObject(Moveable object, int[] position) {
        if (object instanceof TokenI) {
            return addToken((TokenI) object, position == null ? -1 : position[0]);
        } else {
            return false;
        }
    }

    public boolean removeObject(Moveable object) {
        if (object instanceof TokenI) {
            return removeToken((TokenI) object);
        } else {
            return false;
        }
    }

    public List<TokenI> getTokens() {
        return tokens;
    }

    public boolean hasTokens() {
        return tokens.size() > 0;
    }

    public int getSlots() {
        return slots;
    }

    public boolean hasTokenSlotsLeft() {
        return tokens.size() < slots;
    }

    public int getTokenSlotsLeft () {
        return slots - tokens.size();
    }

    public boolean removeToken(TokenI token) {

        boolean result = tokens.remove(token);
        return result;
    }

    /**
     * @param company
     * @return true if this Stop already contains an instance of the specified
     * company's token. Do this by calling the hasTokenOf with Company Name.
     * Using a tokens.contains(company) fails since the tokens are a ArrayList
     * of TokenI not a ArrayList of PublicCompanyI.
     */
    public boolean hasTokenOf(PublicCompanyI company) {
        return hasTokenOf (company.getName());
    }

    public boolean hasTokenOf (String companyName) {
        for (TokenI token : tokens) {
            if (token instanceof BaseToken
                    && ((BaseToken)token).getCompany().getName().equals(companyName)) {
                return true;
            }
        }
        return false;
    }

    public int[] getListIndex (Moveable object) {
        if (object instanceof BaseToken) {
            return new int[] {tokens.indexOf(object)};
        } else {
            return Moveable.AT_END;
        }
    }

    public void setTokens(ArrayList<TokenI> tokens) {
        this.tokens = tokens;
    }

    public String getTrackEdges() {
        return trackEdges;
    }

    public void setTrackEdges(String trackEdges) {
        this.trackEdges = trackEdges;
    }

    public StopType getType() {
        return stopType;
    }

    public ScoreType getScoreType () {
        return accessInfo.getScoreType();
    }

    public RunTo isRunToAllowed() {
        return accessInfo.getRunToAllowed();
    }

    public RunThrough isRunThroughAllowed() {
        return accessInfo.getRunThroughAllowed();
    }

    public Loop isLoopAllowed() {
        return accessInfo.getLoopAllowed();
    }

    public String getTrainMutexID() {
        return accessInfo.getTrainMutexID();
    }

    public boolean isRunToAllowedFor (PublicCompanyI company) {
        switch (accessInfo.getRunToAllowed()) {
        case YES:
            return true;
        case NO:
            return false;
        case TOKENONLY:
            return hasTokenOf (company);
        default:
            // Dead code, only to satisfy the compiler
            return true;
        }
    }

    public boolean isRunThroughAllowedFor (PublicCompanyI company) {
        switch (accessInfo.getRunThroughAllowed()) {
        case YES: // either it has no tokens at all, or it has a company tokens or empty token slots
            return !hasTokens() || hasTokenOf (company) || hasTokenSlotsLeft() ;
        case NO:
            return false;
        case TOKENONLY:
            return hasTokenOf (company);
        default:
            // Dead code, only to satisfy the compiler
            return true;
        }
    }

    public int getValueForPhase (PhaseI phase) {
        if (mapHex.hasValuesPerPhase()) {
            return mapHex.getCurrentValueForPhase(phase);
        } else {
            return relatedStation.get().getValue();
        }
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("Hex ").append(mapHex.getName());
        String cityName = mapHex.getCityName();
        b.append(" (");
        if (Util.hasValue(cityName)) {
            b.append(cityName);
        }
        if (mapHex.getStops().size() > 1) {
            b.append(" ").append(trackEdges);
        }
        b.append(")");
        return b.toString();
    }
}
