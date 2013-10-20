package rails.game.specific._1837;

import java.util.ArrayList;
import java.util.List;

import rails.common.DisplayBuffer;
import rails.common.LocalText;
import rails.game.*;
import rails.game.action.*;
import rails.game.state.IntegerState;

/**
 * Implements an 1837-style startpacket sale.
 */
public class StartRound_1837 extends StartRound {

    protected IntegerState numRoundsPassed = new IntegerState("StartRoundRoundsPassed");
    
    /**
     * Constructor, only to be used in dynamic instantiation.
     */
    public StartRound_1837(GameManagerI gameManager) {
        super(gameManager);
        hasBidding = false;
    }

    /**
     * Start the 1835-style start round.
     *
     * @param startPacket The startpacket to be sold in this start round.
     */
    @Override
    public void start(StartPacket startPacket) {
        super.start(startPacket);

        if (!setPossibleActions()) {
            /*
             * If nobody can do anything, keep executing Operating and Start
             * rounds until someone has got enough money to buy one of the
             * remaining items. The game mechanism ensures that this will
             * ultimately be possible.
             */
            //gameManager.nextRound(this);
            finishRound();
        }

    }

    @Override
    public boolean setPossibleActions() {

        List<StartItem> startItems =  startPacket.getItems();
        List<StartItem> buyableItems = new ArrayList<StartItem>();
        int row;
        int column;
        boolean buyable;
        int minRow = 0;
        boolean[][] soldStartItems = new boolean [3][6];
                
        /*
         * First, mark which items are buyable. Once buyable, they always remain
         * so until bought, so there is no need to check if an item is still
         * buyable.
         */
        for (StartItem item : startItems) {
            buyable = false;

            item.setActualPrice(item.getBasePrice());
            column= item.getColumn();
            row = item.getRow();
            
            if (item.isSold()) {
                // Already sold: skip but set watermarks

                
                if (column ==1) {
                    soldStartItems[0][row-1] = true;
                } else {
                    if (column ==2) {
                        soldStartItems[1][row-1] = true;
                    } else {
                        soldStartItems[2][row-1] = true;
                    }
                }
                
           } else {
                if (minRow == 0) {
                    minRow = row;
                     }
                if (row == minRow) {
                    // Allow all items in the top row.
                    buyable = true;
                } else { 
                    // Allow the first item in the next row of a column where the items in higher 
                    //rows have been bought.
                    if (soldStartItems[column-1][row-2] == true) {                    
                    buyable = true;
                    }
                }
           }
            if (buyable) {
                item.setStatus(StartItem.BUYABLE);
                buyableItems.add(item);
            }
        }
        possibleActions.clear();

        /*
         * Repeat until we have found a player with enough money to buy some
         * item
         */
        while (possibleActions.isEmpty()) {

            Player currentPlayer = getCurrentPlayer();
            if (currentPlayer == startPlayer) ReportBuffer.add("");

            int cashToSpend = currentPlayer.getCash();

            for (StartItem item : buyableItems) {
                //if all players passed in a round, the itemprice gets reduced by 10 per round of completed passing
                int reducedPrice = (item.getBasePrice()- (10 * numRoundsPassed.intValue()));
                
                item.setActualPrice(reducedPrice);
                if (item.getActualPrice() <= cashToSpend) {
                    /* Player does have the cash */
                    possibleActions.add(new BuyStartItem(item,
                            item.getActualPrice(), false));
                }
            }

            // setNextPlayer();
      }

        /* Pass is always allowed */
        possibleActions.add(new NullAction(NullAction.PASS));

        return true;
    }

    /*----- moveStack methods -----*/

    @Override
    public boolean bid(String playerName, BidStartItem item) {

        DisplayBuffer.add(LocalText.getText("InvalidAction"));
        return false;
    }


    /**
     * Process a player's pass.
     *
     * @param playerName The name of the current player (for checking purposes).
     */
    @Override
    public boolean pass(String playerName) {

        String errMsg = null;
        Player player = getCurrentPlayer();

        while (true) {

            // Check player
            if (!playerName.equals(player.getName())) {
                errMsg = LocalText.getText("WrongPlayer", playerName, player.getName());
                break;
            }
            break;
        }

        if (errMsg != null) {
            DisplayBuffer.add(LocalText.getText("InvalidPass",
                    playerName,
                    errMsg ));
            return false;
        }

        ReportBuffer.add(LocalText.getText("PASSES", playerName));

        moveStack.start(false);

        numPasses.add(1);

        if (numPasses.intValue() >= numPlayers) {
            // All players have passed.
            // The next open top row papers in either column will be reduced by price 
            // TBD
            ReportBuffer.add(LocalText.getText("ALL_PASSED"));
            numPasses.set(0);
            numRoundsPassed.add(1);
            
            //finishRound(); This Startround cant be finished until all Items have sold.
            setNextPlayer();
        } else {
            setNextPlayer();
        }

        return true;
    }
    
    

    /* (non-Javadoc)
     * @see rails.game.StartRound#buy(java.lang.String, rails.game.action.BuyStartItem)
     */
    @Override
    protected boolean buy(String playerName, BuyStartItem boughtItem) {
        // If the player buys a price reduced paper the other price reduced papers need to be set back to the 
        // base price
        StartItem item = boughtItem.getStartItem();
        int lastBid = item.getBid();
        String errMsg = null;
        Player player = getCurrentPlayer();
        int price = 0;
        int sharePrice = 0;
        String shareCompName = "";

        while (true) {
            if (!boughtItem.setSharePriceOnly()) {
                if (item.getStatus() != StartItem.BUYABLE) {
                    errMsg = LocalText.getText("NotForSale");
                    break;
                }

                price = item.getBasePrice();
                if (item.getBid() > price) price = item.getBid();

                if (player.getFreeCash() < price) {
                    errMsg = LocalText.getText("NoMoney");
                    break;
                }
            } else {
                price = item.getBid();
            }

            if (boughtItem.hasSharePriceToSet()) {
                shareCompName = boughtItem.getCompanyToSetPriceFor();
                sharePrice = boughtItem.getAssociatedSharePrice();
                if (sharePrice == 0) {
                    errMsg =
                        LocalText.getText("NoSharePriceSet", shareCompName);
                    break;
                }
                if ((stockMarket.getStartSpace(sharePrice)) == null) {
                    errMsg =
                        LocalText.getText("InvalidStartPrice",
                                Bank.format(sharePrice),
                                shareCompName );
                    break;
                }
            }
            break;
        }

        if (errMsg != null) {
            DisplayBuffer.add(LocalText.getText("CantBuyItem",
                    playerName,
                    item.getName(),
                    errMsg ));
            return false;
        }

        moveStack.start(false);

        assignItem(player, item, price, sharePrice);

        // Set priority (only if the item was not auctioned)
        // ASSUMPTION: getting an item in auction mode never changes priority
        if (lastBid == 0) {
            gameManager.setPriorityPlayer();
        }
        setNextPlayer();

        auctionItemState.set(null);
        numPasses.set(0);

        return true;

    }

/*
    private boolean itemsBuyableOnRow(int i) {
        for (int j=0; j<3; j++) {
            if (soldStartItems[j][i]) return true;
        }
        return false;
    }
*/
    @Override
    public String getHelp() {
        return "1837 Start Round help text";
    }

}
