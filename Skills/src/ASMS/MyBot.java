package ASMS;
import java.util.List;
import pirates.game.Direction;
import pirates.game.Location;
import pirates.game.Treasure;
import pirates.game.Pirate;
import pirates.game.PirateBot;
import pirates.game.PirateGame;

public class MyBot implements PirateBot {
    
    @Override
    public void doTurn(PirateGame game) {
        // choose your first pirate ship
        if (game.mySoberPirates().size()>0) {
            Pirate pirate0 = game.mySoberPirates().get(0);
            // show the ID of the chosen pirate ship on the log screen
            game.debug("pirate: " + pirate0.getId());
            // choose the first available treasure
            int tre=0;
            int min=10000;
            if (game.treasures().size()>0) {
                Treasure close=game.treasures().get(0);
                for(Treasure t : game.treasures()) {
                    if (game.distance(pirate0, t.getLocation())<min) {
                        min=game.distance(pirate0, t.getLocation());
                        close=t;
                    }
                }
                Treasure treasure = close;
                // show the ID of the chosen treasure on the log screen
                game.debug("treasure: " + treasure.getId());
                // the pirate ship will move 1 step each turn
                int moves= 6;
                if (pirate0.hasTreasure()) {
                    moves = 1;
                    if(game.mySoberPirates().size()>1) {
                        Pirate pirate1= game.mySoberPirates().get(1);
                        Location destination1; 
                        if(!pirate1.hasTreasure())
                            destination1 = treasure.getLocation();
                        else { //both with treasure
                            destination1 = pirate1.getInitialLocation();
                            if(game.mySoberPirates().size()>2) {
                                Pirate pirate2= game.mySoberPirates().get(2);
                                int moves2=4;
                                List<Pirate> enemy=game.allEnemyPirates();
                                List<Pirate> enemySober=game.enemySoberPirates();
                                Location destination2=enemy.get(0).getInitialLocation();
                                List<Location> possibleLocations2 = game.getSailOptions(pirate2, destination2, moves2);
                                if (pirate2.getLocation().col==enemy.get(0).getInitialLocation().col &&pirate2.getLocation().row==enemy.get(0).getInitialLocation().row) {
                                    if (game.enemyPiratesWithTreasures().size()>0 && game.inRange(pirate2.getLocation(), game.enemyPiratesWithTreasures().get(0).getLocation()))
                                       game.attack(pirate2, game.enemyPiratesWithTreasures().get(0));
                                }
                                else
                                    game.setSail(pirate2,possibleLocations2.get(0));
                                if(game.mySoberPirates().size()>3 && pirate2.getLocation().col==enemy.get(0).getInitialLocation().col && pirate2.getLocation().row==enemy.get(0).getInitialLocation().row) {
                                    Pirate pirate3= game.mySoberPirates().get(3);
                                    Location destination3=enemy.get(1).getInitialLocation();
                                    List<Location> possibleLocations3 = game.getSailOptions(pirate3, destination3, moves2);
                                    if(pirate3.getLocation().col==enemy.get(1).getInitialLocation().col &&pirate3.getLocation().row==enemy.get(1).getInitialLocation().row) {
                                        if (game.enemyPiratesWithTreasures().size()>0 && game.inRange(pirate3.getLocation(), game.enemyPiratesWithTreasures().get(0).getLocation()))
                                            game.attack(pirate3, game.enemyPiratesWithTreasures().get(0));
                                    }
                                    else
                                       game.setSail(pirate3,possibleLocations3.get(0));
                                }
                            }
                        }
                        int moves1=5;
                        if (pirate1.hasTreasure())
                            moves1=1;
                        List<Location> possibleLocations1 = game.getSailOptions(pirate1, destination1, moves1);
                        game.setSail(pirate1, possibleLocations1.get(0));
                    }
                }
                Location destination;
                // set the destination as the treasure's location if the pirate ship isn't carrying a treasure
                if (!pirate0.hasTreasure())
                    destination = treasure.getLocation();
                else { // has treasure
                    if (pirate0.getLocation().col==pirate0.getInitialLocation().col)
                       destination = pirate0.getInitialLocation();
                    else {
                       Location l=new Location(pirate0.getLocation().row,pirate0.getInitialLocation().col);
                       destination=l;
                    }
                }
                // get a list of possible locations for the pirate ship after assigning it a number of steps ("moves") towards the destination
                List<Location> possibleLocations = game.getSailOptions(pirate0, destination, moves);
                // set sail towards the first possible location
                game.setSail(pirate0, possibleLocations.get(0));
            }
            else {
                for(int i=0 ; i<game.myPiratesWithTreasures().size(); i++) {
                    List<Location> possibleLocations2 = game.getSailOptions(game.myPiratesWithTreasures().get(i), game.myPiratesWithTreasures().get(i).getInitialLocation(), 1);
                    game.setSail(game.myPiratesWithTreasures().get(i),possibleLocations2.get(0));
                }
            }
        }
    }
}