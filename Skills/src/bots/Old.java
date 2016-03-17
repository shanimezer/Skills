package bots;

import java.util.ArrayList;
import java.util.List;
import pirates.game.Location;
import pirates.game.Treasure;
import pirates.game.Pirate;
import pirates.game.PirateBot;
import pirates.game.PirateGame;

/**
 *
 * @author yoavp13
 */
public class Old implements PirateBot{
    //for(Pirate p : game.myPirates())
    public ArrayList<Pirate> availablePirates;
    public ArrayList<Pirate> treasurePirates;
    //public ArrayList<Pirate> attackPirates;
    public ArrayList<Treasure> treasures;
    public ArrayList<Integer> moves;
    //public int movesPP; //moves per pirate
    
    @Override
    public void doTurn(PirateGame game) 
    {
        treasures = new ArrayList<>();
        availablePirates = new ArrayList<>();
        treasurePirates = new ArrayList<>();
        //attackPirates = new ArrayList<>();
        moves = new ArrayList<>();
        //Pirate List
        piratesSet(game);
        //Moves calculate
        setMoves(game);
        //Treasure List
        for (Treasure t : game.treasures()) {
            treasures.add(t);
        }
        //Tactics List
        ArrayList<PirateTactics> tactics = new ArrayList<>();
        for (Pirate p : treasurePirates) {
            tactics.add(toTreasure(game, p));
        }
        for (Pirate p : availablePirates) {
            if(treasures.size()!=0)
                tactics.add(toTreasure(game, p));
        }
        //Take actions and check colisions
        PirateTactics t1;
        PirateTactics t2;
        Pirate enemy;
        for (int i = 0; i < tactics.size(); i++) {
            t1 = tactics.get(i);
            for (int j = i; j < game.enemyPiratesWithoutTreasures().size(); j++) {
                enemy = game.enemyPiratesWithoutTreasures().get(i);
                if(j!=i && game.inRange(t1.Pirate, enemy)) {
                    List<Location> possibleLocations = game.getSailOptions(t1.Pirate, enemy.getLocation(), t1.Moves);
                    t1.TempDestination = possibleLocations.get(0);
                }
            }
        }
        for (int i = 0; i < tactics.size(); i++) {
            t1 = tactics.get(i);
            for (int j = i; j < tactics.size(); j++) {
                t2 = tactics.get(j);
                if(j!=i) {
                    if(game.distance(t1.TempDestination, t2.TempDestination) < 3) {
                        tactics.remove(t2);
                    }
                }
            }
            around(game, t1);
            takeAction(game, t1);
        }
        
    }
    

    private class PirateTactics {
        public Pirate Pirate;
        public Location FinalDestination;
        public Location TempDestination;
        public int Moves;
    }
    private PirateTactics toTreasure(PirateGame game, Pirate p) {
        PirateTactics tactic = new PirateTactics();
        tactic.Pirate = p;
        if (!tactic.Pirate.hasTreasure()) {
            tactic.Moves = moves.get(0);
            moves.remove(0);
            tactic.FinalDestination = getNearT(tactic.Pirate, treasures, game);
        }
        else {
            tactic.Moves = 1;
            moves.remove(moves.size()-1);
            tactic.FinalDestination = tactic.Pirate.getInitialLocation();
        }
        List<Location> possibleLocations = game.getSailOptions(tactic.Pirate, tactic.FinalDestination, tactic.Moves);
        tactic.TempDestination = possibleLocations.get(0);
        return tactic;
    }
	
    private void takeAction(PirateGame game, PirateTactics tactics)
    {
        game.setSail(tactics.Pirate, tactics.TempDestination);
    }
    
    //MyFunction
    private Location getNearT(Pirate p, List<Treasure> tr, PirateGame game){
        Treasure closest = tr.get(0);
        for (Treasure t : tr) {
            if(game.distance(p, closest) > game.distance(p, t))
                closest = t;
        }
        treasures.remove(closest); //!!!!!!!!!!!!!
        return closest.getLocation();
    }
    
    private void setMoves(PirateGame game) {
        if(game.mySoberPirates().size()==4) {
            switch (game.myPiratesWithTreasures().size()) {
                case 0:
                case 1:
                case 2:
                    moves.add(2);
                    moves.add(2);
                    moves.add(1);
                    moves.add(1);
                    break;
                case 3:
                    moves.add(3);
                    moves.add(1);
                    moves.add(1);
                    moves.add(1);
                    break;
                case 4:
                    moves.add(1);
                    moves.add(1);
                    moves.add(1);
                    moves.add(1);
                    break;
            }
        }
        else {
            int m = game.getActionsPerTurn()-treasurePirates.size();
            while(m>0 && !availablePirates.isEmpty()) {
                if(m%availablePirates.size()==0) {
                    for (int i = 0; i < availablePirates.size(); i++) {
                        moves.add(m/availablePirates.size());
                        m=m-(m/availablePirates.size());
                    }
                }
                else {
                    moves.add(1);
                    m--;
                }
            }
            for (int i = 0; i < treasurePirates.size(); i++) {
                moves.add(1);
            }
        }
    }
    
    private void piratesSet(PirateGame game) {
        boolean attack;
        for (Pirate p : game.myPirates()) {
            if(!p.isLost() && p.getTurnsToSober()==0) {
                if(p.hasTreasure()) {
                    treasurePirates.add(p);
                }
                else {
                    attack = false;
                    for (Pirate enemy : game.enemySoberPirates()) {
                        if(game.inRange(p, enemy) && !attack){
                            game.attack(p, enemy);
                            attack = true;
                        }
                    }
                    if(!attack)
                        availablePirates.add(p);
                }
            }
        }
    }
    
    private void around(PirateGame game, PirateTactics t1) {
        List<Location> possibleLocations;
        Location loc;
        for (Pirate enemy : game.enemyDrunkPirates()) {
            if(game.distance(t1.Pirate, enemy) < 2 && game.distance(enemy, t1.TempDestination) < 2){
                if(t1.TempDestination.col != enemy.getLocation().col) {
                    if(t1.Pirate.getLocation().row > 3) {
                        loc = new Location(t1.Pirate.getLocation().row-3, t1.Pirate.getLocation().col);
                    } else {
                        loc = new Location(t1.Pirate.getLocation().row+3, t1.Pirate.getLocation().col);
                    }
                }
                else {
                    if(t1.Pirate.getLocation().row > 3) {
                        loc = new Location(t1.Pirate.getLocation().row, t1.Pirate.getLocation().col-3);
                    } else {
                        loc = new Location(t1.Pirate.getLocation().row, t1.Pirate.getLocation().col+3);
                    }
                }
                t1.FinalDestination = loc;
            }
        }
        possibleLocations = game.getSailOptions(t1.Pirate, t1.FinalDestination, t1.Moves);
        t1.TempDestination = possibleLocations.get(0);
    }
}