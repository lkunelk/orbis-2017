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
	private List<Point> ords = new ArrayList<Point>();

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

    		if(friend.getHealth() > 99)
    			tasks.put(friend.getUuid(), new NestBreaking(world.getClosestEnemyNestFrom(friend.getPosition(), null)));
    		else if(!any)
				tasks.put(friend.getUuid(), new FortressBuilding());
    		else
    			tasks.put(friend.getUuid(), new NestBuilding(ans));
    	}
    }

    int turns = 0;

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

    	if(turns == 0) {
    		Point nest = world.getNestPositions()[0];

    		int[][][] pos = new int[][][]{
    			{{1, -2}, {2, 1}, {-1, 2}, {-2, -1}, {-1, -3}, {-3, 1}, {3, -1}, {1, 3}},
    			{{1, -2}, {2, -4}, {2, 1}, {3, -1}, {4, -3}, {4, 2}, {5, 0}, {6, -2}},
    			{{-2, 4}, {-1, 2}, {0, 5}, {1, 3}, {2, 1}, {2, 6}, {3, 4}, {4, 3}},
    			{{-6, 2}, {-5, 0}, {-4, -2}, {-4, 3}, {-3, 1}, {-2, -1}, {-2, 4}, {-1, 2}},
    			{{-4, -2}, {-3, -4}, {-2, -6}, {-2, -1}, {-1, -3}, {0, -5}, {1, -2}, {2, -4}},
    		};
    		boolean first = true;
    		int best = -1;
    		int[][] ans = null;
    		for(int[][] p : pos)
    		{
    			int dist = Integer.MAX_VALUE;
    			for(int i = 0; i < 8; i++)
    			{
    				Point cur = new Point(nest.getX() + p[i][0], nest.getY() + p[i][1]);
    				List<EnemyUnit> dis = new ArrayList<EnemyUnit>();
    				dist = Math.min(dist, world.getTaxiCabDistance(cur, world.getClosestEnemyFrom(cur, dis).getPosition()));
    				if(dist <= best)
    					break;
    			}
    			if(first)
    				dist++;
    			if(dist > best)
    			{
    				best = dist;
    				ans = p;
    			}

    			dist = Integer.MAX_VALUE;
    			for(int i = 0; i < 8; i++)
    			{
    				Point cur = new Point(nest.getX() + p[i][1], nest.getY() + p[i][0]);
    				List<EnemyUnit> dis = new ArrayList<EnemyUnit>();
    				dist = Math.min(dist, world.getTaxiCabDistance(cur, world.getClosestEnemyFrom(cur, dis).getPosition()));
    				if(dist <= best)
    					break;
    			}
    			if(first)
    				dist++;
    			if(dist > best)
    			{
    				best = dist;
    				ans = new int[p.length][2];
    				for(int i = 0; i < p.length; i++)
    				{
    					ans[i][0] = p[i][1];
    					ans[i][1] = p[i][0];
    				}
    			}
    			first = false;
    		}
    		for(int[] knight : ans) {
    			Point nxt = new Point((nest.getX() + knight[0] + 19) % 19, (nest.getY() + knight[1] + 19) % 19);
    			ords.add(nxt);
    			board[nxt.getX()][nxt.getY()] = State.NEST;
    		}
    	}
    	turns++;

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
    	NestBreaking.breakNest(world, friendlyUnits, board, tasks);
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

    	for(Map.Entry<Point, List<FriendlyUnit>> entry : nestBuilders.entrySet())
    		NestBuilding.BuildNest(entry.getKey(), world, entry.getValue(), board);
    }
}