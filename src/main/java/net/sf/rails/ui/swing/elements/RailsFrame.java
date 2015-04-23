package net.sf.rails.ui.swing.elements;

import javax.swing.JFrame;

import net.sf.rails.ui.swing.GameUIManager;

/**
 * RailsFrame provides common mechanisms for all frames of Rails
 */
public abstract class RailsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    
    protected final GameUIManager gameUIManager;
    
    protected RailsFrame(GameUIManager gameUIManager) {
        this.gameUIManager = gameUIManager;
    }
    
    public GameUIManager getGameUIManager() {
        return gameUIManager;
    }

}
