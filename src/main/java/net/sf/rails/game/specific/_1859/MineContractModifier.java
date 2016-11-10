/**
 * 
 */
package net.sf.rails.game.specific._1859;

import java.util.Set;

/**
 * @author Martin
 *
 */

import net.sf.rails.algorithms.NetworkVertex;
import net.sf.rails.algorithms.RevenueAdapter;
import net.sf.rails.algorithms.RevenueBonus;
import net.sf.rails.algorithms.RevenueStaticModifier;
import net.sf.rails.game.PrivateCompany;


public class MineContractModifier implements RevenueStaticModifier {

    public boolean modifyCalculator(RevenueAdapter revenueAdapter) {
        Set<PrivateCompany> privateCompanies =
                revenueAdapter.getCompany().getPresident().getPortfolioModel().getPrivateCompanies();
        for (PrivateCompany company : privateCompanies) {
            if (company.getId().equals("KDMC")) {
                revenueAdapter.addRevenueBonus(createKimberleyBonus(revenueAdapter));
            }
            if (company.getId().equals("JGMC")) {
                revenueAdapter.addRevenueBonus(createJohannesbergBonus(revenueAdapter));
            }
        }
        return false; // no pretty print
    }
    
    private RevenueBonus createKimberleyBonus(RevenueAdapter revenueAdapter) {
        NetworkVertex kimberley = NetworkVertex.getVertexByIdentifier(revenueAdapter.getVertices(), "N16.-1");
        RevenueBonus bonus = new RevenueBonus(20, "1859KimberleyDiamondBonus");
        bonus.addVertex(kimberley);
        return bonus;
    }

    private RevenueBonus createJohannesbergBonus(RevenueAdapter revenueAdapter) {
        NetworkVertex johannesberg = NetworkVertex.getVertexByIdentifier(revenueAdapter.getVertices(), "N16.-1");
        RevenueBonus bonus = new RevenueBonus(20, "1859JohannesbergGoldBonus");
        bonus.addVertex(johannesberg);
        return bonus;
    }


    public String prettyPrint(RevenueAdapter revenueAdapter) {
        return null;
    }

}
