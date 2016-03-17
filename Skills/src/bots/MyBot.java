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
    public int actions;
    public ArrayList<Integer> moves;
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
        for (BoardStatusAttack bs : boardStatusAttack) {
            piratesTactics.add(moveToAttack(game, bs));
        }
        //Check colisions
        PirateTactics t1;
        PirateTactics t2;
        Location l;
        boolean found;
        for (int i = 0; i < piratesTactics.size(); i++) {
            for (int j = i; j < piratesTactics.size(); j++) {
                if(j!=i) {
                    t1 = piratesTactics.get(i);
                    t2 = piratesTactics.get(j);
                    if(game.distance(t1.tempDestination, t2.tempDestination) < 3) {
                        List<Location> loc = game.getSailOptions(t2.pirate, t2.finalDestination, game.distance(t2.pirate, t2.tempDestination));
                        found = false;
                        for (int k = 0; k < loc.size() && !found; k++) {
                            l = loc.get(i);
                            d.print("temp-> col:"+t2.tempDestination.col+", row:"+t2.tempDestination.row);
                            if(!(l.col==t2.tempDestination.col && l.row==t2.tempDestination.row)) {
                                d.print("found");
                                found=true;
                                t2.tempDestination = l;
                            }
                        }
                        loc = game.getSailOptions(t1.pirate, t1.finalDestination, game.distance(t1.pirate, t1.tempDestination));
                        for (int k = 0; k < loc.size() && !found; k++) {
                            l = loc.get(i);
                            if(!(l.col==t1.tempDestination.col && l.row==t1.tempDestination.row)) {
                                d.print("found");
                                found=true;
                                t1.tempDestination = l;
                            }
                        }
//                        if(!found) {
//                            around(game, t1);
//                        }
                    }
                }
            }
        }
        //Take actions
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
            moves = status.pirate.getCarryTreasureSpeed();
            tactics.finalDestination = status.pirate.getInitialLocation();
        }
        List<Location> possibleLocations = game.getSailOptions(tactics.pirate, tactics.finalDestination, moves);
        tactics.tempDestination = possibleLocations.get(0);
        return tactics ;
    }
    private PirateTactics moveToAttack(PirateGame game, BoardStatusAttack status) {
        PirateTactics tactics = new PirateTactics();
        tactics.pirate = status.pirate;
        tactics.finalDestination = status.enemy.getLocation();
        tactics.tempDestination = game.getSailOptions(status.pirate, status.enemy.getLocation(), movesAttacking.get(0)).get(0);
        movesAttacking.remove(0);
        return tactics;
    }
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
        actions = game.getActionsPerTurn();
        piratesTactics = new ArrayList<>();
        attackingPiratesMoving = new ArrayList<>();
        boardStatusTreasure = new ArrayList<>();
        boardStatusAttack = new ArrayList<>();
        treasurePirates = copyP(game.myPiratesWithTreasures());
        availableTreasures = copyT(game.treasures());
        availablePirates = copyP(game.mySoberPirates());
        availablePirates.removeAll(treasurePirates);
        //for (Pirate p : availablePirates) {
        for (int i = 0; i < availablePirates.size();) {
            Pirate p = availablePirates.get(i);
            if(p.isLost())
                availablePirates.remove(p);
            else
                i++;
        }
        //Set Pirates Lists && attack if in range
//        boolean remove;
//        for (Pirate enemyPirate : game.enemyPiratesWithTreasures()) { //set attacking pirates
//            remove=true;
//            Pirate p = getClosestAvailablePirate(game, enemyPirate, availablePirates);
//            if(game.InRange(p.getLocation(), enemyPirate.getLocation())) {
//                if(p.getReloadTurns()==0) {
//                    game.attack(p, enemyPirate); //attack if can
//                    actions--;
//                }
//                else {
//                    if(p.getReloadTurns() < game.distance(enemyPirate.getLocation(), enemyPirate.getInitialLocation()))
//                        attackingPiratesMoving.add(p);
//                    else
//                        remove=false;
//                }
//            }
//            else {
//                attackingPiratesMoving.add(p);
//                boardStatusAttack.add(getBoardStatusAttack(game, p, enemyPirate));
//            }
//            if(remove)
//                availablePirates.remove(p);
//        }
        setMoves(game);
        for (Pirate p : availablePirates) {
            boardStatusTreasure.add(getBoardStatusTreasure(game, p));
        }
    } //PirateTactics, Moves, PiratesLists
    private void firstTurn (PirateGame game) {
        d = new Debug(game, showLog);
    }
    private void setMoves(PirateGame game) {
        moves = new ArrayList<>();
        movesAttacking = new ArrayList<>();
        movesToTreasures = new ArrayList<>();
        for (int i = 0; i < attackingPiratesMoving.size(); i++) {
            movesAttacking.add(1);
        }
        for (int i = 0; i < availablePirates.size(); i++) {
            movesToTreasures.add(1);
        }
//        int pointer = 0;
//        while(actions%(attackingPiratesMoving.size()+availablePirates.size())!=0) {
//            moves.add(pointer, 1);
//            pointer++;
//            actions--;
//        }
//        for (int i = 0; i < attackingPiratesMoving.size()+availablePirates.size(); i++) {
//            if(i < moves.size())
//                moves.set(i, (actions/(attackingPiratesMoving.size()+availablePirates.size()))+moves.get(i));
//            else
//                moves.add(i, actions/(attackingPiratesMoving.size()+availablePirates.size()));
//        }
//        for (int i = 0; i < attackingPiratesMoving.size(); i++) {
//            movesAttacking.add(moves.get(i));
//        }
//        for (int i = attackingPiratesMoving.size(); i < moves.size(); i++) {
//            movesToTreasures.add(moves.get(i));
//        }
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
    private ArrayList<Pirate> copyP(List<Pirate> list) {
        ArrayList<Pirate> arr = new ArrayList<>();
        for (Pirate p : list) {
            arr.add(p);
        }
        return arr;
    }
    private ArrayList<Treasure> copyT(List<Treasure> list) {
        ArrayList<Treasure> arr = new ArrayList<>();
        for (Treasure t : list) {
            arr.add(t);
        }
        return arr;
    }
    private void around(PirateGame game, PirateTactics t1) {
        List<Location> possibleLocations;
        Location loc;
        for (Pirate enemy : game.enemyDrunkPirates()) {
            if(game.distance(t1.pirate, enemy) < 2 && game.distance(enemy, t1.tempDestination) < 2){
                if(t1.tempDestination.col != enemy.getLocation().col) {
                    if(t1.pirate.getLocation().row > 3) {
                        loc = new Location(t1.pirate.getLocation().row-3, t1.pirate.getLocation().col);
                    } else {
                        loc = new Location(t1.pirate.getLocation().row+3, t1.pirate.getLocation().col);
                    }
                }
                else {
                    if(t1.pirate.getLocation().row > 3) {
                        loc = new Location(t1.pirate.getLocation().row, t1.pirate.getLocation().col-3);
                    } else {
                        loc = new Location(t1.pirate.getLocation().row, t1.pirate.getLocation().col+3);
                    }
                }
                t1.finalDestination = loc;
            }
        }
        possibleLocations = game.getSailOptions(t1.pirate, t1.finalDestination, 1);
        t1.tempDestination = possibleLocations.get(0);
    }//try
}