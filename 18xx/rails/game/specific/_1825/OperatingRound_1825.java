package rails.game.specific._1825;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rails.game.GameManagerI;
import rails.game.OperatingRound;
import rails.game.PublicCompanyI;

public class OperatingRound_1825 extends OperatingRound {

    public OperatingRound_1825(GameManagerI gameManager) {
        super(gameManager);
    }

    @Override
    public List<PublicCompanyI> setOperatingCompanies() {
        Map<Integer, PublicCompanyI> operatingCompanies = new TreeMap<Integer, PublicCompanyI>();
        int space;
        int key;
        for (PublicCompanyI company : companyManager.getAllPublicCompanies()) {
            if (!canCompanyOperateThisRound(company)) continue;    
            // Key must put companies in reverse operating order, because sort
            // is ascending.
            space = company.getIPOPrice();
            //Corps operate in descending IPO price
            //Corps with the same IPO price operate in the order they were floated
            //IPO price will inherently be in the right order
            //subtracting the formation order index will put it at the right point to operate
            //This wouldn't work if there are lots of corps at the same price
            //there are not too many corps in each banding for this to be an issue in 1825 even with all 3 units
            key = 1000000 - (space - company.getFormationOrderIndex());
            operatingCompanies.put(new Integer(key), company);
            }
        return new ArrayList<PublicCompanyI>(operatingCompanies.values());        
        }
    
}