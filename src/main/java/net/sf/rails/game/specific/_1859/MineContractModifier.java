/**
 * 
 */
package net.sf.rails.game.specific._1859;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Martin
 *
 */

import net.sf.rails.algorithms.NetworkVertex;
import net.sf.rails.algorithms.RevenueAdapter;
import net.sf.rails.algorithms.RevenueBonus;
import net.sf.rails.algorithms.RevenueDynamicModifier;
import net.sf.rails.algorithms.RevenueStaticModifier;
import net.sf.rails.algorithms.RevenueTrainRun;
import net.sf.rails.game.Phase;
import net.sf.rails.game.PrivateCompany;


public class MineContractModifier implements RevenueDynamicModifier {

    List<String> contractCompany = new ArrayList<String>();
    String targetVertex;
    List<String> visitedVertex = new ArrayList<String>();
    List<RevenueTrainRun> validRuns = new ArrayList<RevenueTrainRun>();
    Phase currentPhase;
    
    public boolean prepareModifier(RevenueAdapter revenueAdapter) {
        // only active for certain companies...
        Set<PrivateCompany> privateCompanies =
                revenueAdapter.getCompany().getPresident().getPortfolioModel().getPrivateCompanies();
        currentPhase = revenueAdapter.getPhase();
        for (PrivateCompany company : privateCompanies) {
            if (company.getId().equals("KDMC")) {
                contractCompany.add(company.getId()); 
                return true;
            }
              else if (company.getId().equals("JGMC")){
                  contractCompany.add(company.getId()); 
                  return true;    
                }
            }
        return false;
    }
    

    public int predictionValue(List<RevenueTrainRun> runs) {
        // cannot be predicted
        return 0;
    }

    private List<RevenueTrainRun> identifyValidRun(List<RevenueTrainRun> runs) {
        // check if runs hits one of the major contract stations..
       
        for (RevenueTrainRun run:runs) {
            if ((validRuns.isEmpty()) || (validRuns.size()<2)){
                if (!run.hasAValidRun()) continue;
                //make sure each vertex is only touched once
                if (currentPhase.getIndex() > 4) {
                    //in brown phase we need to check for both towns..
                    //first we check if both cities have been touched if both privates are in the 
                    //same players portfolio..
                    if ((run.getRunVertices().contains("G7.-1")) && ( contractCompany.contains("KDMC"))) {
                        validRuns.add(run);
                        visitedVertex.add("G7.-1");
                        if ((run.getRunVertices().contains("K3.-1")) &&( contractCompany.contains("JGMC"))) {
                        visitedVertex.add("K3.-1");
                        }
                        return validRuns;
                        //now we check if Johannesburg has been touched !!
                    } else if ((run.getRunVertices().contains("K3.-1")) && ( contractCompany.contains("JGMC"))) {
                        validRuns.add(run);
                        visitedVertex.add("K3.-1");
                        continue;
                    }
                }
                    //lets check if Kimberley and not Johannesburg has been touched...
                        if  ((run.getRunVertices().contains("G7.-1")) && (contractCompany.contains("KDMC"))){
                            validRuns.add(run);
                            visitedVertex.add("G7.-1");
                            continue;
                        }
            }
            return validRuns;
        }
        return null;
    }
    
    public int evaluationValue(List<RevenueTrainRun> runs, boolean optimalRuns) {
        // optimal runs is already adjusted
        if (optimalRuns) return 0;
        // otherwise check valid runs
        int changeRevenues = 0;
        for (RevenueTrainRun run:identifyValidRun(runs)) {
            if (visitedVertex.contains("G7.-1")) {
                if (currentPhase.getIndex() > 4)
                    changeRevenues += 20;
                if (visitedVertex.contains("K3.-1")) {
                    changeRevenues +=30;
                }
            } else if (currentPhase.getIndex()> 2) {
                changeRevenues += 10;
            }
        }
        return changeRevenues;
    }

    public void adjustOptimalRun(List<RevenueTrainRun> optimalRuns) {
        // set valid runs to be empty
        for (RevenueTrainRun run:identifyValidRun(optimalRuns)) {
            run.getRunVertices().clear();
        }
    }
    public boolean providesOwnCalculateRevenue() {
        // does not
        return false;
    }

    public int calculateRevenue(RevenueAdapter revenueAdpater) {
        // zero does no change
        return 0;
    }

    
    public String prettyPrint(RevenueAdapter revenueAdapter) {
        return null;
    }

}
