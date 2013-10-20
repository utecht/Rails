/**
 * 
 */
package rails.ui.swing.gamespecific._1837;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import rails.common.LocalText;
import rails.game.StartItem;
import rails.game.StartRound;
import rails.game.action.StartItemAction;
import rails.ui.swing.GameUIManager;
import rails.ui.swing.StartRoundWindow;
import rails.ui.swing.elements.ActionButton;
import rails.ui.swing.elements.Caption;
import rails.ui.swing.elements.Cell;
import rails.ui.swing.elements.ClickField;
import rails.ui.swing.elements.Field;
import rails.ui.swing.elements.RailsIcon;
import rails.ui.swing.hexmap.HexHighlightMouseListener;

/**
 * @author martin
 *
 */
public class StartRoundWindow_1837 extends StartRoundWindow {

    protected StartItem[] items;
    /**
     * 
     */
    public StartRoundWindow_1837() {
        
    }
    
    
    public void init(StartRound round, GameUIManager parent) {
        this.round = round;
        includeBuying = round.hasBuying();
        includeBidding = round.hasBidding();
        showBasePrices = round.hasBasePrices();
        setGameUIManager(parent);
        setTitle(LocalText.getText("START_ROUND_TITLE"));
        getContentPane().setLayout(new BorderLayout());

        statusPanel = new JPanel();
        gb = new GridBagLayout();
        statusPanel.setLayout(gb);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setOpaque(true);

        buttonPanel = new JPanel();

        if (includeBuying) {
            buyButton = new ActionButton(RailsIcon.AUCTION_BUY);
            buyButton.setMnemonic(KeyEvent.VK_B);
            buyButton.addActionListener(this);
            buyButton.setEnabled(false);
            buttonPanel.add(buyButton);
        }

        if (includeBidding) {
            bidButton = new ActionButton(RailsIcon.BID);
            bidButton.setMnemonic(KeyEvent.VK_D);
            bidButton.addActionListener(this);
            bidButton.setEnabled(false);
            buttonPanel.add(bidButton);

            spinnerModel =
                new SpinnerNumberModel(new Integer(999), new Integer(0),
                        null, new Integer(1));
            bidAmount = new JSpinner(spinnerModel);
            bidAmount.setPreferredSize(new Dimension(50, 28));
            bidAmount.setEnabled(false);
            buttonPanel.add(bidAmount);
        }

        passButton = new ActionButton(RailsIcon.PASS);
        passButton.setMnemonic(KeyEvent.VK_P);
        passButton.addActionListener(this);
        passButton.setEnabled(false);
        buttonPanel.add(passButton);

        buttonPanel.setOpaque(true);

        gbc = new GridBagConstraints();

        players = gameUIManager.getPlayers();
        np = getGameUIManager().getGameManager().getNumberOfPlayers();
        packet = round.getStartPacket();
        crossIndex = new int[packet.getNumberOfItems()];

        items = round.getStartItems().toArray(new StartItem[0]);
        ni = items.length;
        StartItem item;
        for (int i = 0; i < ni; i++) {
            item = items[i];
            crossIndex[item.getIndex()] = i;
        }

        actionableItems = new StartItemAction[ni];

        infoIcon = createInfoIcon();

        initCells();

        getContentPane().add(statusPanel, BorderLayout.NORTH);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setTitle("Rails: Start Round");
        setLocation(300, 150);
        setSize(275, 325);
        gameUIManager.setMeVisible(this, true);
        requestFocus();

        addKeyListener(this);

        // set closing behavior and listener
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );
        final JFrame thisFrame = this;
        final GameUIManager guiMgr = getGameUIManager();
        addWindowListener(new WindowAdapter () {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(thisFrame, LocalText.getText("CLOSE_WINDOW"), LocalText.getText("Select"), JOptionPane.OK_CANCEL_OPTION)
                        == JOptionPane.OK_OPTION) {
                    thisFrame.dispose();
                    guiMgr.terminate();
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                guiMgr.getWindowSettings().set(thisFrame);
            }
            @Override
            public void componentResized(ComponentEvent e) {
                guiMgr.getWindowSettings().set(thisFrame);
            }
        });

        gameUIManager.packAndApplySizing(this);
    }

    private void initCells() {
        int lastX = -1;
        int lastY = 0;

        itemName = new Caption[ni];
        itemNameButton = new ClickField[ni];
        basePrice = new Field[ni];
        minBid = new Field[ni];
        bidPerPlayer = new Field[ni][np];
        info = new Field[ni];
        itemStatus = new Field[ni];
        upperPlayerCaption = new Cell[np];
        lowerPlayerCaption = new Cell[np];
        playerBids = new Field[np];
        playerFree = new Field[np];

        upperPlayerCaptionYOffset = ++lastY;

        itemNameXOffset = ++lastX;
        itemNameYOffset = ++lastY;
        if (showBasePrices) {
            basePriceXOffset = ++lastX;
            basePriceYOffset = lastY;
        }
        bidPerPlayerXOffset = playerCaptionXOffset = ++lastX;
        bidPerPlayerYOffset = lastY;

        infoXOffset = bidPerPlayerXOffset + np;
        infoYOffset = lastY;

        // Bottom rows
        lastY += (ni - 1);

        playerFreeCashXOffset = bidPerPlayerXOffset;
        playerFreeCashYOffset = ++lastY;

        lowerPlayerCaptionYOffset = ++lastY;

        fields = new JComponent[1+infoXOffset][2+lastY];

        addField(new Caption(LocalText.getText("ITEM")), 0, 0, 1, 2,
                WIDE_RIGHT + WIDE_BOTTOM);
        if (showBasePrices) {
            addField(new Caption(LocalText.getText("PRICE")), basePriceXOffset, 0, 1, 2,
                    WIDE_BOTTOM);
        }

        // Top player captions
        addField(new Caption(LocalText.getText("PLAYERS")),
                playerCaptionXOffset, 0, np, 1, 0);
        //boolean playerOrderCanVary = getGameUIManager().getGameParameterAsBoolean(GuiDef.Parm.PLAYER_ORDER_VARIES);
        for (int i = 0; i < np; i++) {
            //if (playerOrderCanVary) {
            //    f = upperPlayerCaption[i] = new Field(getGameUIManager().getGameManager().getPlayerNameModel(i));
            //    upperPlayerCaption[i].setNormalBgColour(Cell.NORMAL_CAPTION_BG_COLOUR);
            //} else {
            f = upperPlayerCaption[i] = new Caption(players.get(i).getNameAndPriority());
            //}
            addField(f, playerCaptionXOffset + i, upperPlayerCaptionYOffset, 1, 1, WIDE_BOTTOM);
        }

        for (int i = 0; i < ni; i++) {
            si = items[i];
            f = itemName[i] = new Caption(si.getName());
            HexHighlightMouseListener.addMouseListener(f,
                    gameUIManager.getORUIManager(),
                    si);
            addField(f, itemNameXOffset, itemNameYOffset + i, 1, 1, WIDE_RIGHT);
            f =
                itemNameButton[i] =
                    new ClickField(si.getName(), "", "", this,
                            itemGroup);
            HexHighlightMouseListener.addMouseListener(f,
                    gameUIManager.getORUIManager(),
                    si);
            addField(f, itemNameXOffset, itemNameYOffset + i, 1, 1, WIDE_RIGHT);
            // Prevent row height resizing after every buy action
            itemName[i].setPreferredSize(itemNameButton[i].getPreferredSize());

            if (showBasePrices) {
                f = basePrice[i] = new Field(si.getActualPriceModel());
                addField(f, basePriceXOffset, basePriceYOffset + i, 1, 1, 0);
            }

            for (int j = 0; j < np; j++) {
                f = bidPerPlayer[i][j] = new Field(round.getBidModel(i, j));
                addField(f, bidPerPlayerXOffset + j, bidPerPlayerYOffset + i,
                        1, 1, 0);
            }

            f = info[i] = new Field (infoIcon);
            f.setToolTipText(getStartItemDescription(si));
            HexHighlightMouseListener.addMouseListener(f,
                    gameUIManager.getORUIManager(),
                    si);
            addField (f, infoXOffset, infoYOffset + i, 1, 1, WIDE_LEFT);

            // Invisible field, only used to hold current item status.
            f = itemStatus[i] = new Field (si.getStatusModel());
        }

        // Player money
        boolean firstBelowTable = true;

        addField(new Caption(
                LocalText.getText("CASH")),
                playerFreeCashXOffset - 1, playerFreeCashYOffset, 1, 1,
                WIDE_RIGHT + (firstBelowTable ? WIDE_TOP : 0));
        for (int i = 0; i < np; i++) {
            f =
                playerFree[i] =
                    new Field(players.get(i).getCashModel());
            addField(f, playerFreeCashXOffset + i, playerFreeCashYOffset, 1, 1,
                    firstBelowTable ? WIDE_TOP : 0);
        }

        for (int i = 0; i < np; i++) {
            //if (playerOrderCanVary) {
            //    f = lowerPlayerCaption[i] = new Field(getGameUIManager().getGameManager().getPlayerNameModel(i));
            //    lowerPlayerCaption[i].setNormalBgColour(Cell.NORMAL_CAPTION_BG_COLOUR);
            //} else {
            f = lowerPlayerCaption[i] = new Caption(players.get(i).getNameAndPriority());
            //}
            addField(f, playerCaptionXOffset + i, lowerPlayerCaptionYOffset,
                    1, 1, WIDE_TOP);
        }

        dummyButton = new ClickField("", "", "", this, itemGroup);

    }
}
