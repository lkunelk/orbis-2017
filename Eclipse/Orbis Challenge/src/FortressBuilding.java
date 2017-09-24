import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;

import com.orbischallenge.firefly.client.objects.models.EnemyUnit;
import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.game.engine.Point;

public class FortressBuilding extends Task {
	
	static ArrayList<Point> path;
	static int[][] unitPos;
	static boolean[][] blocked;
	
    public static void buildFortress(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits, Map<String, Task> tasks, State[][] state) {
    	//stores index of unit as a position on map
    	unitPos = new int[19][19];
    	blocked = new boolean[19][19];
    	for(int i = 0; i < 19*19; i++)unitPos[i/19][i%19] = -1;
    	for(int i = 0; i < friendlyUnits.length; i++) {
    		Point p = friendlyUnits[i].getPosition();
    		if(tasks.get(friendlyUnits[i].getUuid()).getType() == Task.Type.FortressBuilder) {
    			unitPos[p.getY()][p.getX()] = i;
    		} else {
    			blocked[p.getY()][p.getX()] = true;
    		}
    	}
//    	for(int i = 0; i < 19; i++) {
//			for(int j = 0; j < 19; j++)
//				System.out.print(unitPos[i][j]+",");
//			System.out.println();
//    	}
    	
    	//contains shortest path from nest to an empty point
    	for(Point nest: world.getFriendlyNestPositions()) {
    		path = new ArrayList<Point>();
    		blocked[nest.getY()][nest.getX()] = true;
    		BFS(world, nest);
    		
    		deb(nest);
    		
    		shiftFortress(world, friendlyUnits);
    	}
    }
    
    public static void BFS(World world, Point p) {
    	
    	LinkedList<Point> q = new LinkedList<Point>();
    	int[][] dist = new int[19][19];
    	for(int i = 0; i < 19*19; i++)dist[i/19][i%19] = Integer.MAX_VALUE;

    	int gx = -1, gy = -1;
    	
    	dist[p.getY()][p.getX()] = 0;
    	q.offer(p);
    	while(!q.isEmpty()) {
    		Point pos = q.poll();
    		int x = pos.getX();
    		int y = pos.getY();
    		
    		if(!isBlocked(world,x,y)) {
    			gx = x;
    			gy = y;
    			break;
    		}
    		
    		if(dist[y][(x+1)%19] == Integer.MAX_VALUE && !world.isWall(new Point((x+1)%19,y))) {
    			dist[y][(x+1)%19] = dist[y][x]+1;
    			q.offer(new Point((x+1)%19,y));
    		}
    		if(dist[y][(x+18)%19] == Integer.MAX_VALUE && !world.isWall(new Point((x+18)%19,y))){
    			dist[y][(x+18)%19] = dist[y][x]+1;
    			q.offer(new Point((x+18)%19,y));
    		}
    		if(dist[(y+1)%19][x] == Integer.MAX_VALUE && !world.isWall(new Point(x,(y+1)%19))) {
    			dist[(y+1)%19][x] = dist[y][x]+1;
    			q.offer(new Point(x,(y+1)%19));
    		}
    		if(dist[(y+18)%19][x] == Integer.MAX_VALUE && !world.isWall(new Point(x,(y+18)%19))) {
    			dist[(y+18)%19][x] = dist[y][x]+1;
    			q.offer(new Point(x,(y+18)%19));
    		}
    	}
    	
    	for(int i = 0; i < 19; i++) {
    		for(int j = 0; j < 19; j++)
    			if(dist[i][j]<10)System.out.print(dist[i][j]+",");
    			else System.out.print(".,");
    		System.out.println();
    	}
    	
    	//backtrack
    	for(int i = dist[gy][gx]; i >= 0 ; i--) {
    		path.add(new Point(gx, gy));
    		
    		if(dist[gy][(gx+1)%19] == dist[gy][gx]-1) {
    			gx = (gx+1)%19;
    		}
    		else if(dist[gy][(gx+18)%19] == dist[gy][gx]-1){
    			gx = (gx+18)%19;
    		}
    		else if(dist[(gy+1)%19][gx] == dist[gy][gx]-1) {
    			gy = (gy+1)%19;
    		}
    		else if(dist[(gy+18)%19][gx] == dist[gy][gx]-1) {
    			gy = (gy+18)%19;
    		}
    	}
    	
    }
    
    public static boolean isBlocked(World world, int x, int y) {
    	return blocked[y][x] || unitPos[y][x] != -1;
    }
    
    public static void shiftFortress(World world, FriendlyUnit[] fu) {
    	for(int i = 0; i < path.size()-1; i++){
    		Point p1 = path.get(i);
    		Point p2 = path.get(i+1);
    		
    		deb("p2 "+p2);
    		
    		int ind = unitPos[p2.getY()][p2.getX()];
    		world.move(fu[ind], p1);
    	}
    }
    
    public static void deb(Object o) {
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