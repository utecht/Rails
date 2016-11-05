/**
 * 
 */
package net.sf.rails.game.specific._1800;

import net.sf.rails.game.GameManager;
import net.sf.rails.game.PublicCompany;
import net.sf.rails.game.financial.StockRound;

/**
 * @author brummm
 *
 */
public class Stockround_1800 extends StockRound {

    /**
     * @param parent
     * @param id
     */
    public Stockround_1800(GameManager parent, String id) {
        super(parent, id);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void adjustSharePrice(PublicCompany company, int numberSold,
            boolean soldBefore) {
        // No more changes if it has already dropped
        if (!soldBefore) {
            super.adjustSharePrice (company, 1, soldBefore);
        }
    }

}
