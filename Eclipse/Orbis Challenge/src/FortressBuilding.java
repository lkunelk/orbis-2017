import java.util.Map;
import java.util.ArrayList;

import com.orbischallenge.firefly.client.objects.models.EnemyUnit;
import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.game.engine.Point;

public class FortressBuilding extends Task {
	
    public static void buildFortress(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits, Map<String, Task> tasks, State[][] state) {
    	for(FriendlyUnit fu: friendlyUnits) {
    		if(tasks.get(fu.getUuid()).getType() == Task.Type.FortressBuilder) {
    			
    			//if firefly is on a nest then move to make room for he next one
    			Point pos = fu.getPosition();
    			ArrayList<Point> exclude = new ArrayList<Point>();
    			exclude.add(pos);
    			if(world.getClosestFriendlyNestFrom(pos, null).equals(pos)) {
    				moveTowardsEmpty(world, friendlyUnits, fu, pos, state);
    			}
    			
    		}
    	}
    	
    	//if in the way of someone move
		for(FriendlyUnit fu: friendlyUnits) {
			if(tasks.get(fu.getUuid()).getType() == Task.Type.FortressBuilder) {
				Point pos = fu.getPosition();
				if(state[pos.getY()][pos.getX()] == State.PUSH) {
					moveTowardsEmpty(world, friendlyUnits, fu, pos, state);
				}
			}
		}
    	
		//else do nothing stay in one spot
    }

    //for units on the nest
    public static void moveTowardsEmpty(World world, FriendlyUnit[] fus, FriendlyUnit u, Point nest, State[][] state) {
    	//keep map of occupied spaces
    	boolean[][] occupied = new boolean[world.getHeight()][world.getWidth()];
    	for(FriendlyUnit fu: fus) {
    		Point p = fu.getPosition();
    		occupied[p.getY()][p.getX()] = true;
    	}
    	
    	//look for closest unoccupied tile to the nest
    	int nx = nest.getX();
    	int ny = nest.getY();
    	int minDist = Integer.MAX_VALUE;
    	Point minP = null;
    	for(int y = 0; y < world.getHeight(); y++) {
    		for(int x = 0; x < world.getWidth(); x++) {
    			if(!occupied[y][x] && !world.isWall(new Point(x,y))) {
    				int val = Math.abs(y-ny) + Math.abs(x-nx); //taxicab dist
    				if(val < minDist) {
    					minDist = val;
    					minP = new Point(x,y);
    				}
    			}
    		}
    	}
    	
    	//move newbie in direction of empty tile
    	int dx = minP.getX() - nest.getX();
    	int dy = minP.getY() - nest.getY();
    	
    	if(dx == 0) {
    		dy = dy>0?1:-1;
    	} else if(dy == 0) {
    		dx = dx>0?1:-1;
    	} else if(Math.abs(dx) < Math.abs(dy)) {
    		dx = dx>0?1:-1;
    		dy = 0;
    	} else {
    		dy = dy>0?1:-1;
    		dx = 0;
    	}
    	
    	Point up = u.getPosition();
    	int ux = up.getX();
    	int uy = up.getY();
    	world.move(u, new Point(ux+dx, uy+dy));
    	state[ux+dx][uy+dy] = State.PUSH;
    }
    
    public void deb(Object o) {
    	System.out.println(o);
    }
    
	public Type getType() {
		return Task.Type.FortressBuilder;
	}
}


//for(int i = 0; i < 19; i++) {
//	for(int j = 0; j < 19; j++)
//		System.out.print(occupied[i][j]?"0":".");
//	System.out.println();
//}