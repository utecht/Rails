package net.sf.rails.ui.swing.elements;

import java.awt.Rectangle;

import javax.swing.JFrame;

import com.google.gson.Gson;

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
    
    public void saveSettings(Gson gson) {
        
    }
    
    public void loadSettings(Gson gson) {
        
    }
    
    
    
    
    protected static class Settings {
        private Rectangle bounds;
    }
   
}
