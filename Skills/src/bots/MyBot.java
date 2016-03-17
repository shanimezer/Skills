package bots;

import java.util.ArrayList;
import java.util.List;
import pirates.game.Direction;
import pirates.game.Location;
import pirates.game.Treasure;
import pirates.game.Pirate;
import pirates.game.PirateBot;
import pirates.game.PirateGame;

public class MyBot implements PirateBot{
    //General Variables
    private static Debug d;
    public static boolean firstTurn = true;
    public static final boolean showLog = true;
    
    //My Lists
    public List<Treasure> availableTreasures;
    public List<Pirate> availablePirates; //after all set this pirates to the treasure
    public ArrayList<Pirate> attackingPiratesMoving; //moving to the enemy
    public List<Pirate> treasurePirates; //on the way to the base
    public ArrayList<Integer> movesAttacking;
    public ArrayList<Integer> movesToTreasures;
    public ArrayList<BoardStatusTreasure> boardStatusTreasure;
    public ArrayList<BoardStatusAttack> boardStatusAttack;
    public ArrayList<PirateTactics> piratesTactics;
    
    //Classes
    private class BoardStatus {
        public Pirate pirate;
    }
    private class BoardStatusTreasure extends BoardStatus{
        public Treasure treasure;
    }
    private class BoardStatusAttack extends BoardStatus{
        public Pirate enemy;
    }
    private class PirateTactics {
        public Pirate pirate;
        public Location finalDestination;
        public Location tempDestination;
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
        for (BoardStatusTreasure bs : boardStatusTreasure) {
            piratesTactics.add(toTreasure(game, bs));
        }
//        for (BoardStatusAttack bs : boardStatusAttack) {
//            piratesTactics.add(moveToAttack(game, bs));
//        }
        //Check Collision HERE!
        for (PirateTactics pt : piratesTactics ) {
            takeAction(game, pt);
        }
    }
    
    //Not finished! ------------------
    private BoardStatusTreasure getBoardStatusTreasure(PirateGame game, Pirate pirate) {
        BoardStatusTreasure status = new BoardStatusTreasure();
        status.pirate = pirate;
        status.treasure = getClosestTreasure(game, status.pirate, game.treasures());
        availableTreasures.remove(status.treasure);
        return status;
    }
    private BoardStatusAttack getBoardStatusAttack(PirateGame game, Pirate pirate, Pirate enemy) {
        BoardStatusAttack status = new BoardStatusAttack();
        status.enemy = enemy;
        status.pirate = pirate;
        return status;
    }
    private PirateTactics toTreasure(PirateGame game, BoardStatusTreasure status) {
        PirateTactics tactics = new PirateTactics();
        tactics.pirate = status.pirate;
        int moves = movesToTreasures.get(0);
        movesToTreasures.remove(0);
        if (!tactics.pirate.hasTreasure()) {
            tactics.finalDestination = status.treasure.getLocation();
        }
        else {
            moves = 1;
            tactics.finalDestination = status.pirate.getInitialLocation();
        }
        List<Location> possibleLocations = game.getSailOptions(tactics.pirate, tactics.finalDestination, moves);
        tactics.tempDestination = possibleLocations.get(0);
        List<Pirate> inRangePirates = inRangePirates(game, tactics.pirate);
        int minDistanceMove = 0;
        //better move
        return tactics ;
    }
//    private PirateTactics moveToAttack(PirateGame game, BoardStatusAttack status) {
//        
//    }
    private void takeAction(PirateGame game, PirateTactics tactics) {
        game.setSail(tactics.pirate, tactics.tempDestination);
    }
    // -------------------
    
    //Set Function
    private void setAll(PirateGame game) {
        if(firstTurn) {
            firstTurn(game);
            firstTurn = false;
        }
        //Set Lists
        piratesTactics = new ArrayList<>();
        attackingPiratesMoving = new ArrayList<>();
        boardStatusTreasure = new ArrayList<>();
        boardStatusAttack = new ArrayList<>();
        treasurePirates = game.myPiratesWithTreasures();
        availableTreasures = game.treasures();
        availablePirates = game.mySoberPirates();
        availablePirates.removeAll(treasurePirates);
        for (Pirate p : availablePirates) {
            if(p.isLost())
                availablePirates.remove(p);
        }
        //Set Pirates Lists && attack if in range
        for (Pirate enemyPirate : game.enemyPiratesWithTreasures()) { //set attacking pirates
            Pirate p = getClosestAvailablePirate(game, enemyPirate, availablePirates);
            if(game.InRange(p.getLocation(), enemyPirate.getLocation()))
                game.attack(p, enemyPirate); //attack if in range
            else
                attackingPiratesMoving.add(p);
            boardStatusAttack.add(getBoardStatusAttack(game, p, enemyPirate));
            availablePirates.remove(p);
        }
        setMoves(game);
        for (Pirate p : availablePirates) {
            boardStatusTreasure.add(getBoardStatusTreasure(game, p));
        }
    } //PirateTactics, Moves, PiratesLists
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