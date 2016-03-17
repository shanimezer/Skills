package bots;

import java.util.ArrayList;
import java.util.List;
import pirates.game.Direction;
import pirates.game.Location;
import pirates.game.Treasure;
import pirates.game.Pirate;
import pirates.game.PirateBot;
import pirates.game.PirateGame;
/**
 * פוקציות שליחה של פיראטים ליעדים
 * פעולה שמגדירה צעדים
 */
public class MyBot implements PirateBot{
    //General Variables
    private static Debug d;
    public static boolean firstTurn = true;
    public static final boolean showLog = true;
    
    //My Lists
    public List<Treasure> availableTreasures;
    public List<Pirate> availablePirates; //after all set this pirates to the treasure
    public List<Pirate> attackingPiratesMoving; //moving to the enemy
    public List<Pirate> attackingPirates; //FIRE
    public List<Pirate> treasurePirates; //on the way to the base
    public ArrayList<Integer> movesAttacking;
    public ArrayList<Integer> movesToTreasures;
    
    //Classes
    private class BoardStatus {
        public Pirate Pirate;
        public Treasure Treasure;
    }
    private class PirateTactics {
        public Pirate Pirate;
        public Location FinalDestination;
        public Location TempDestination;
        public int Moves;
    }
    private class Debug {
        private final PirateGame game;
        private final boolean print;
        
        Debug (PirateGame game, boolean showLog) {
            this.game = game;
            this.print = showLog;
        }
        public void print(String log) {
            if(print)
                game.debug(log);
        }
    }
    
    @Override
    public void doTurn(PirateGame game) 
    {
        setAll(game);
        BoardStatus status = getBoardStatus(game);
        PirateTactics tactics = toTreasure(game, status);
        takeAction(game, tactics);
    }
    
    //Not finished! ------------------
    private BoardStatus getBoardStatus(PirateGame game) {
        BoardStatus status = new BoardStatus();
//        status.Pirate = game.myPirates().get(0);
//        status.Treasure = getClosestTreasure(game, status.Pirate, game.treasures());
        return status;
    }
    private PirateTactics toTreasure(PirateGame game, BoardStatus status) {
        PirateTactics tactics = new PirateTactics();
        tactics.Pirate = status.Pirate;
        tactics.Moves = game.getActionsPerTurn();
        if (!tactics.Pirate.hasTreasure()) {
            tactics.FinalDestination = status.Treasure.getLocation();
        }
        else {
            tactics.Moves = 1;
            tactics .FinalDestination = status .Pirate.getInitialLocation();
        }
        List<Location> possibleLocations = game.getSailOptions( tactics .Pirate,
        tactics.FinalDestination, tactics.Moves);
        tactics.TempDestination = possibleLocations.get(0);
        List<Pirate> inRangePirates = inRangePirates(game, tactics.Pirate);
        int minDistanceMove = 0;
        //better move
        return tactics ;
    }
    private void takeAction(PirateGame game, PirateTactics tactics) {
        game.setSail(tactics.Pirate, tactics.TempDestination);
    }
    // -------------------
    
    //Set Function
    private void setAll(PirateGame game) {
        if(firstTurn) {
            firstTurn(game);
            firstTurn = false;
        }
        //Set Lists
        attackingPirates = game.allMyPirates(); attackingPirates.clear(); //
        attackingPiratesMoving = game.allMyPirates(); attackingPiratesMoving.clear(); //
        treasurePirates = game.myPiratesWithTreasures();
        availableTreasures = game.treasures();
        availablePirates = game.mySoberPirates();
        availablePirates.removeAll(treasurePirates);
        for (Pirate p : availablePirates) {
            if(p.isLost())
                availablePirates.remove(p);
        }
        //Set Pirates Lists
        for (Pirate enemyPirate : game.enemyPiratesWithTreasures()) { //set attacking pirates
            Pirate p = getClosestAvailablePirate(game, enemyPirate, availablePirates);
            if(game.InRange(p.getLocation(), enemyPirate.getLocation()))
                attackingPirates.add(p);
            else
                attackingPiratesMoving.add(p);
            availablePirates.remove(p);
        }
        setMoves(game);
    }
    private void firstTurn (PirateGame game) {
        d = new Debug(game, showLog);
    }
    private void setMoves(PirateGame game) {
        int moves = game.getActionsPerTurn();
        moves-=treasurePirates.size();
        //Not finished! ------------------
    }
    
    //Playing Function
    private Treasure getClosestTreasure(PirateGame game, Pirate pirate, List<Treasure> treasures){
        if(treasures.isEmpty()) {
            d.print("return 'null' in getClosestTreasure");
            return null;
        }
        Treasure closest = treasures.get(0);
        for (Treasure treasure : treasures) {
            if(game.distance(pirate, closest) > game.distance(pirate, treasure))
                closest = treasure;
        }
        return closest;
    }
    private Pirate getClosestEnemyPirateWithTreasure(PirateGame game, Pirate pirate, List<Pirate> enemyPiratesWithTreasure) {
        if(enemyPiratesWithTreasure.isEmpty()) {
            d.print("return 'null' in getClosestEnemyPirateWithTreasure");
            return null;
        }
        Pirate closest = enemyPiratesWithTreasure.get(0);
        for (Pirate enemyP : enemyPiratesWithTreasure) {
            if(game.distance(pirate, closest) > game.distance(pirate, enemyP))
                closest = enemyP;
        }
        return closest;
    }
    private Pirate getClosestAvailablePirate(PirateGame game, Pirate enemyPirate, List<Pirate> availablePirates) {
        if(availablePirates.isEmpty()) {
            d.print("return 'null' in getClosestAvailablePirate");
            return null;
        }
        Pirate closest = availablePirates.get(0);
        for (Pirate availableP : availablePirates) {
            if(game.distance(enemyPirate, closest) > game.distance(enemyPirate, availableP))
                closest = availableP;
        }
        return closest;
    }
    private List<Pirate> inRangePirates(PirateGame game, Pirate pirate) {
        List<Pirate> inRangePirates = game.enemyPirates();
        inRangePirates.addAll(game.myPirates());
        for (int i = 0; i < inRangePirates.size();) {
            if(!game.InRange(inRangePirates.get(i).getLocation(), pirate.getLocation()) || inRangePirates.get(i) == pirate)
                inRangePirates.remove(i);
            else
                i++;
        }
        return inRangePirates;
    }
}