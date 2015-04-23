/**
 * 
 */
package net.sf.rails.ui.swing.gamespecific._1880;

import java.awt.BorderLayout;

import net.sf.rails.ui.swing.GameUIManager;
import net.sf.rails.ui.swing.StatusWindow;

public class StatusWindow_1880 extends StatusWindow {

    private static final long serialVersionUID = 1L;
    private ParSlotsPanel parSlotsPanel;
    
    public StatusWindow_1880(GameUIManager gameUIManager) {
       super(gameUIManager);
    }
    
    @Override
    public void init() {
        super.init();
        
        parSlotsPanel = new ParSlotsPanel();
        parSlotsPanel.init(gameUIManager);
        
        pane.add(parSlotsPanel, BorderLayout.EAST);
        
    }

}
