package net.sf.rails.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.rails.game.StockSpace;
import net.sf.rails.ui.swing.elements.GUIStockSpace;
import net.sf.rails.ui.swing.elements.RailsFrame;


/**
 * This class displays the StockMarket Window.
 */

public class StockChart extends RailsFrame implements KeyListener {
    private static final long serialVersionUID = 1L;
    private JPanel stockPanel;
    private Box horLabels, verLabels;

    private GridLayout stockGrid;
    private GridBagConstraints gc;
    private StockSpace[][] market;

    public StockChart(GameUIManager gameUIManager) {
        super(gameUIManager);

        initialize();
        populateStockPanel();

        stockPanel.setBackground(Color.LIGHT_GRAY);

        final JFrame frame = this;
        final GameUIManager guiMgr = gameUIManager;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StatusWindow.uncheckMenuItemBox(StatusWindow.MARKET_CMD);
                frame.dispose();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                guiMgr.getWindowSettings().set(frame);
            }
            @Override
            public void componentResized(ComponentEvent e) {
                guiMgr.getWindowSettings().set(frame);
            }
        });
        addKeyListener(this);

        gameUIManager.packAndApplySizing(this);
    }

    private void initialize() {
        setTitle("Rails: Stock Chart");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        stockPanel = new JPanel();
        horLabels = Box.createHorizontalBox();
        verLabels = Box.createVerticalBox();

        stockGrid = new GridLayout();
        stockGrid.setHgap(0);
        stockGrid.setVgap(0);
        stockPanel.setLayout(stockGrid);

        gc = new GridBagConstraints();

        market = gameUIManager.getRoot().getStockMarket().getStockChart();

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.BOTH;
        getContentPane().add(stockPanel, BorderLayout.CENTER);
        getContentPane().add(horLabels, BorderLayout.NORTH);
        getContentPane().add(verLabels, BorderLayout.WEST);

    }

    private void populateStockPanel() {
        stockGrid.setColumns(market[0].length);
        stockGrid.setRows(market.length);
        JLabel l;

        for (int i = 0; i < market.length; i++) {
            l = new JLabel("" + (i + 1), JLabel.CENTER);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);

            verLabels.add(Box.createRigidArea(new Dimension(1, i == 0 ? 1 : 12)));
            verLabels.add(Box.createVerticalGlue());
            verLabels.add(l);
            for (int j = 0; j < market[0].length; j++) {
                if (i == 0) {
                    l =
                            new JLabel(Character.toString((char) ('A' + j)),
                                    JLabel.CENTER);
                    l.setAlignmentX(Component.CENTER_ALIGNMENT);

                    horLabels.add(Box.createRigidArea(new Dimension(j == 0 ? 12
                            : 12, 1)));
                    horLabels.add(Box.createHorizontalGlue());
                    horLabels.add(l);
                }
                stockPanel.add(new GUIStockSpace(i, j, market[i][j]));
            }
        }
        verLabels.add(Box.createVerticalGlue());
        horLabels.add(Box.createHorizontalGlue());
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F1) {
            HelpWindow.displayHelp(gameUIManager.getGameManager().getHelp());
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

}
