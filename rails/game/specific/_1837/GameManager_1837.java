/**
 * 
 */
package rails.game.specific._1837;

import rails.game.GameManager;
import rails.game.RoundI;
import rails.game.StartPacket;
import rails.game.StartRound;
import rails.game.specific._1880.ParSlotManager_1880;

/**
 * @author martin
 *
 */
public class GameManager_1837 extends GameManager {

    private ParSlotManager_1837 parSlotManager;
    /**
     * 
     */
    public GameManager_1837() {
        super(); 
        parSlotManager = new ParSlotManager_1837(this);
    }
    
    public void nextRound(RoundI round) {
        if (round instanceof StartRound) { 
            //if (((StartRound) round).getStartPacket().areAllSold()) { // This start round was "completed"
                StartPacket nextStartPacket = companyManager.getNextUnfinishedStartPacket();
                if (nextStartPacket == null) {
                    startStockRound(); // All start rounds complete - start stock rounds
                } else {
                    startStartRound(nextStartPacket); // Start next start round
                }
          /*  } else {
                startOperatingRound(runIfStartPacketIsNotCompletelySold());
            }*/
        }
        else {
            super.nextRound(round);
        }
    }
    
    public ParSlotManager_1837 getParSlotManager() {
        return parSlotManager;
    }

}
