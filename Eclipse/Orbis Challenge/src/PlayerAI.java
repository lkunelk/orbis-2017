import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orbischallenge.firefly.client.objects.models.EnemyUnit;
import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.Tile;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.firefly.objects.enums.Direction;
import com.orbischallenge.game.engine.Point;

public class PlayerAI {
    // Any field declarations go here

	private Map<String, Task> tasks = new HashMap<String, Task>();
	private State[][] board = new State[19][19];

	Point[] ords = {
		new Point(13, 11),
		new Point(14, 13),
//		new POint(15, 15),

		new Point(11, 12),
		new Point(12, 14),
		new Point(13, 16),
//
		new Point( 9, 13),
		new Point(10, 15),
		new Point(11, 17),
	};

    public void assignTasks(World world, List<FriendlyUnit> friendlyUnits, List<FriendlyUnit> all) {
  
    	for(FriendlyUnit friend : friendlyUnits)
    	{
    		boolean any = false;
    		int best = Integer.MAX_VALUE;
    		Point ans = null;
    		for(Point p : ords)
    		{
    			List<FriendlyUnit> cur = new ArrayList<FriendlyUnit>();
    			for(FriendlyUnit f : all)
    				if(tasks.containsKey(f.getUuid()))
    		    		if(tasks.get(f.getUuid()).getType() == Task.Type.NestBuilder)
    		    			if(((NestBuilding) tasks.get(f.getUuid())).getNestPosition() == p)
    		    				cur.add(f);

    			if(cur.size() == 4)
    				continue;
  
    			int pre  = NestBuilding.calcTime(p, world, cur, board);
    			cur.add(friend);
    			int post = NestBuilding.calcTime(p, world, cur, board);

    			if(post < pre)
    				if(post < best)
	    			{
    					best = post;
	    				any = true;
	    				ans = p;
	    			}
    		}

    		if(!any)
				tasks.put(friend.getUuid(), new FortressBuilding());
    		else
    			tasks.put(friend.getUuid(), new NestBuilding(ans));
    	}

    	for(Point p : ords)
    		board[p.getX()][p.getY()] = State.NEST;
    }

    /**
     * This method will get called every turn.
     *
     * @param world The latest state of the world.
     * @param friendlyUnits An array containing all remaining firefly units in your team
     * @param enemyUnits An array containing all remaining enemy firefly units
     *
     */
    public void doMove(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits) {
        /* Fly away to freedom, daring fireflies
        Build thou nests
        Grow, become stronger
        Take over the world */

    	for(FriendlyUnit friend : friendlyUnits)
    		if(tasks.containsKey(friend.getUuid()))
				if(tasks.get(friend.getUuid()).getType() == Task.Type.NestBuilder)
		    	{
		    		Point nest = ((NestBuilding) tasks.get(friend.getUuid())).getNestPosition();
					List<Point> adjacent = new ArrayList<Point>();
					for(Tile neighbour : world.getTilesAround(nest).values())
						if(neighbour.isNeutral())
							adjacent.add(neighbour.getPosition());
	
					if(adjacent.size() == 0)
						tasks.remove(friend.getUuid());
		    	}

    	List<FriendlyUnit> newbies = new ArrayList<FriendlyUnit>();
    	for(FriendlyUnit friend : friendlyUnits)
    		if(!tasks.containsKey(friend.getUuid()))
    			newbies.add(friend);
    	assignTasks(world, newbies, new ArrayList<FriendlyUnit>(Arrays.asList(friendlyUnits)));

    	buildNests(world, friendlyUnits, enemyUnits);
    	FortressBuilding.buildFortress(world, friendlyUnits, enemyUnits, tasks, board);
    	
    	//attack whenever you see someone
    	for(FriendlyUnit u: friendlyUnits) {
    		for(EnemyUnit e: enemyUnits) {
    			Map<Direction, Point> m = world.getNeighbours(u.getPosition());
    			if(m.get(Direction.NORTH).equals(e.getPosition())) world.move(u, m.get(Direction.NORTH));
    			if(m.get(Direction.SOUTH).equals(e.getPosition())) world.move(u, m.get(Direction.SOUTH));
    			if(m.get(Direction.EAST).equals(e.getPosition())) world.move(u, m.get(Direction.EAST));
    			if(m.get(Direction.WEST).equals(e.getPosition())) world.move(u, m.get(Direction.WEST));
    		}
    	}
    }

    public void buildNests(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits) {
    	Map<Point, List<FriendlyUnit>> nestBuilders = new HashMap<Point, List<FriendlyUnit>>();
    	for(FriendlyUnit friend : friendlyUnits)
    		if(tasks.get(friend.getUuid()).getType() == Task.Type.NestBuilder)
    		{
    			if(!nestBuilders.containsKey(((NestBuilding) tasks.get(friend.getUuid())).getNestPosition()))
    				nestBuilders.put(((NestBuilding) tasks.get(friend.getUuid())).getNestPosition(), new ArrayList<FriendlyUnit>());

    			nestBuilders.get(((NestBuilding) tasks.get(friend.getUuid())).getNestPosition()).add(friend);
    		}
    	System.out.println(nestBuilders.size());

    	for(Map.Entry<Point, List<FriendlyUnit>> entry : nestBuilders.entrySet())
    		NestBuilding.BuildNest(entry.getKey(), world, entry.getValue(), board);
    }
}