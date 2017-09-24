import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.Tile;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.game.engine.Point;

public class NestBuilding extends Task {

	private Point nestPosition;

	public NestBuilding(Point nest)
	{
		this.nestPosition = nest;
	}

	public static int calcTime(Point nest, World world, List<FriendlyUnit> friendlyUnits, State[][] board)
	{
		//	if(!(friendlyUnits.size() <= 4))
	//	{
	//		System.out.println("wat " + nest);
	//		System.exit(-1);
	//	}
	//	for(FriendlyUnit friend : friendlyUnits)
	//	{
	//		if(!(nest.getX() - 2 <= friend.getPosition().getX() && friend.getPosition().getX() <= nest.getX() + 2))
	//			System.exit(-1);
	//		if(!(nest.getY() - 2 <= friend.getPosition().getY() && friend.getPosition().getY() <= nest.getY() + 2))
	//			System.exit(-1);
	//	}
	
		List<Point> adjacent = new ArrayList<Point>();
		for(Tile neighbour : world.getTilesAround(nest).values())
			if(neighbour.isNeutral())
				adjacent.add(neighbour.getPosition());
	
		if(adjacent.size() == 0)
			return 0;
	
		int[][][] distFromAdj = new int[adjacent.size()][19][19];
		for(int i = 0; i < adjacent.size(); i++)
		{
			Queue<Point> q = new LinkedList<Point>();
			q.offer(adjacent.get(i));
	
			for(int j = 0; j < 19; j++)
				for(int k = 0; k < 19; k++)
					distFromAdj[i][j][k] = Integer.MAX_VALUE;
			distFromAdj[i][adjacent.get(i).getX()][adjacent.get(i).getY()] = 0;
	
			while(!q.isEmpty())
			{
				Point cur = q.poll();
	
				for(Tile adj : world.getTilesAround(cur).values())
				{
					Point nxt = adj.getPosition();
					if(distFromAdj[i][nxt.getX()][nxt.getY()] == Integer.MAX_VALUE)
						if(board[nxt.getX()][nxt.getY()] == null)
						{
							distFromAdj[i][nxt.getX()][nxt.getY()] = distFromAdj[i][cur.getX()][cur.getY()] + 1;
							q.offer(nxt);
						}
				}
			}
		}

		int[][][] dist = new int[friendlyUnits.size()][1 << adjacent.size()][adjacent.size()];
		int[][] lookup = new int[friendlyUnits.size()][1 << adjacent.size()];
		for(int i = 0; i < friendlyUnits.size(); i++)
		{
			for(int j = 0; j < (1 << adjacent.size()); j++)
				for(int k = 0; k < adjacent.size(); k++)
					dist[i][j][k] = Integer.MAX_VALUE;

			for(int k = 0; k < adjacent.size(); k++)
				dist[i][1 << k][k] = distFromAdj[k][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()];

			for(int j = 0; j < (1 << adjacent.size()); j++)
				for(int k = 0; k < adjacent.size(); k++)
					for(int l = 0; l < adjacent.size(); l++)
						if(k != l && (j & (1 << k)) != 0 && (j & (1 << l)) != 0)
							if(dist[i][j - (1 << k)][l] != Integer.MAX_VALUE && distFromAdj[l][adjacent.get(k).getX()][adjacent.get(k).getY()] != Integer.MAX_VALUE)
								dist[i][j][k] = Math.min(dist[i][j][k], dist[i][j - (1 << k)][l] + distFromAdj[l][adjacent.get(k).getX()][adjacent.get(k).getY()]);

			for(int j = 0; j < (1 << adjacent.size()); j++)
			{
				int best = Integer.MAX_VALUE;
				for(int k = 0; k < adjacent.size(); k++)
					best = Math.min(best,  dist[i][j][k]);
				lookup[i][j] = best;
	
				if(j == 0)
					lookup[i][j] = 0;
			}
//			for(int j = 0; j < (1 << adjacent.size()); j++)
//				for(int k = 0; k < adjacent.size(); k++)
//				{
//					List<Integer> ords = new ArrayList<Integer>();
//					for(int k = 0; k < adjacent.size(); k++)
//						if((j & (1 << k)) != 0)
//							ords.add(k);
//		
//					int[][] dp = new int[1 << ords.size()][ords.size()];
//					for(int ii = 0; ii < (1 << ords.size()); ii++)
//						for(int jj = 0; jj < ords.size(); jj++)
//							dp[ii][jj] = Integer.MAX_VALUE;
//	
//					for(int k = 0; k < ords.size(); k++)
//						dp[1 << k][k] = distFromAdj[ords.get(k)][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()];
//		
//					for(int ii = 0; ii < (1 << ords.size()); ii++)
//						for(int jj = 0; jj < ords.size(); jj++)
//							for(int kk = 0; kk < ords.size(); kk++)
//								if(jj != kk && (ii & (1 << jj)) != 0 && (ii & (1 << kk)) != 0)
//									if(dp[ii - (1 << jj)][kk] != Integer.MAX_VALUE && distFromAdj[kk][adjacent.get(jj).getX()][adjacent.get(jj).getY()] != Integer.MAX_VALUE)
//										dp[ii][jj] = Math.min(dp[ii][jj], dp[ii - (1 << jj)][kk] + distFromAdj[kk][adjacent.get(jj).getX()][adjacent.get(jj).getY()]);
//		
//					int best = Integer.MAX_VALUE;
//					for(int k = 0; k < ords.size(); k++)
//						best = Math.min(best, dp[(1 << ords.size()) - 1][k]);
//					dist[i][j] = best;
//	
//					if(j == 0)
//						dist[i][j] = 0;
//				}
		}
		
		int best = Integer.MAX_VALUE;
		if(friendlyUnits.size() == 0)
			best = Integer.MAX_VALUE;
		else if(friendlyUnits.size() == 1)
		{
			best = lookup[0][(1 << adjacent.size()) - 1];
		}
		else if(friendlyUnits.size() == 2)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					if((i & j) == 0 && (i | j) == (1 << adjacent.size()) - 1)
						if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE)
							best = Math.min(best, Math.max(lookup[0][i], lookup[1][j]));
		}
		else if(friendlyUnits.size() == 3)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					for(int k = 0; k < (1 << adjacent.size()); k++)
						if((i & j) == 0 && (i & k) == 0 && (j & k) == 0 && (i | j | k) == (1 << adjacent.size()) - 1)
							if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE && lookup[2][k] != Integer.MAX_VALUE)
							{
								best = Math.min(best, Math.max(lookup[0][i], Math.max(lookup[1][j], lookup[2][k])));
							}
		}
		else if(friendlyUnits.size() == 4)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					for(int k = 0; k < (1 << adjacent.size()); k++)
						for(int l = 0; l < (1 << adjacent.size()); l++)
							if((i & j) == 0 && (i & k) == 0 && (i & l) == 0 && (j & k) == 0 && (j & l) == 0 && (k & l) == 0 && (i | j | k | l) == (1 << adjacent.size()) - 1)
								if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE && lookup[2][k] != Integer.MAX_VALUE && lookup[3][l] != Integer.MAX_VALUE)
								{
									best = Math.min(best, Math.max(lookup[0][i], Math.max(lookup[1][j], Math.max(lookup[2][k], lookup[3][l]))));
								}
		}
		else
		{
			System.out.println("watthfuk " + friendlyUnits.size());
			System.exit(0);
		}

		return best;
	}
	public static void BuildNest(Point nest, World world, List<FriendlyUnit> friendlyUnits, State[][] board)
	{
		if(!(friendlyUnits.size() <= 4))
		{
			System.out.println("wat " + nest);
			System.exit(-1);
		}
//		for(FriendlyUnit friend : friendlyUnits)
//		{
//			System.out.println("uh oh");
//			if(!(nest.getX() - 2 <= friend.getPosition().getX() && friend.getPosition().getX() <= nest.getX() + 2))
//				System.exit(-1);
//			if(!(nest.getY() - 2 <= friend.getPosition().getY() && friend.getPosition().getY() <= nest.getY() + 2))
//				System.exit(-1);
//			System.out.println("uh noh");
//		}
	
		List<Point> adjacent = new ArrayList<Point>();
		for(Tile neighbour : world.getTilesAround(nest).values())
			if(neighbour.isNeutral())
				adjacent.add(neighbour.getPosition());
	
		if(adjacent.size() == 0)
			return;
	
		int[][][] distFromAdj = new int[adjacent.size()][19][19];
		for(int i = 0; i < adjacent.size(); i++)
		{
			Queue<Point> q = new LinkedList<Point>();
			q.offer(adjacent.get(i));
	
			for(int j = 0; j < 19; j++)
				for(int k = 0; k < 19; k++)
					distFromAdj[i][j][k] = Integer.MAX_VALUE;
			distFromAdj[i][adjacent.get(i).getX()][adjacent.get(i).getY()] = 0;
	
			while(!q.isEmpty())
			{
				Point cur = q.poll();
	
				for(Tile adj : world.getTilesAround(cur).values())
				{
					Point nxt = adj.getPosition();
					if(distFromAdj[i][nxt.getX()][nxt.getY()] == Integer.MAX_VALUE)
						if(board[nxt.getX()][nxt.getY()] == null)
						{
							distFromAdj[i][nxt.getX()][nxt.getY()] = distFromAdj[i][cur.getX()][cur.getY()] + 1;
							q.offer(nxt);
						}
				}
			}
		}

		int[][][] dist = new int[friendlyUnits.size()][1 << adjacent.size()][adjacent.size()];
		int[][][] start = new int[friendlyUnits.size()][1 << adjacent.size()][adjacent.size()];
		int[][] lookup = new int[friendlyUnits.size()][1 << adjacent.size()];
		for(int i = 0; i < friendlyUnits.size(); i++)
		{
			for(int j = 0; j < (1 << adjacent.size()); j++)
				for(int k = 0; k < adjacent.size(); k++)
				{
					dist[i][j][k] = Integer.MAX_VALUE;
					start[i][j][k] = -1;
				}

			for(int k = 0; k < adjacent.size(); k++)
			{
				dist[i][1 << k][k] = distFromAdj[k][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()];
				start[i][1 << k][k] = k;
			}

			for(int j = 0; j < (1 << adjacent.size()); j++)
				for(int k = 0; k < adjacent.size(); k++)
					for(int l = 0; l < adjacent.size(); l++)
						if(k != l && (j & (1 << k)) != 0 && (j & (1 << l)) != 0)
							if(dist[i][j - (1 << k)][l] != Integer.MAX_VALUE && distFromAdj[l][adjacent.get(k).getX()][adjacent.get(k).getY()] != Integer.MAX_VALUE)
								if(dist[i][j - (1 << k)][l] + distFromAdj[l][adjacent.get(k).getX()][adjacent.get(k).getY()] < dist[i][j][k])
								{
									dist[i][j][k] = dist[i][j - (1 << k)][l] + distFromAdj[l][adjacent.get(k).getX()][adjacent.get(k).getY()];
									start[i][j][k] = start[i][j - (1 << k)][l];
								}

			for(int j = 0; j < (1 << adjacent.size()); j++)
			{
				int best = Integer.MAX_VALUE;
				for(int k = 0; k < adjacent.size(); k++)
					best = Math.min(best,  dist[i][j][k]);
				lookup[i][j] = best;
	
				if(j == 0)
					lookup[i][j] = 0;
			}
//			for(int j = 0; j < (1 << adjacent.size()); j++)
//				for(int k = 0; k < adjacent.size(); k++)
//				{
//					List<Integer> ords = new ArrayList<Integer>();
//					for(int k = 0; k < adjacent.size(); k++)
//						if((j & (1 << k)) != 0)
//							ords.add(k);
//		
//					int[][] dp = new int[1 << ords.size()][ords.size()];
//					for(int ii = 0; ii < (1 << ords.size()); ii++)
//						for(int jj = 0; jj < ords.size(); jj++)
//							dp[ii][jj] = Integer.MAX_VALUE;
//	
//					for(int k = 0; k < ords.size(); k++)
//						dp[1 << k][k] = distFromAdj[ords.get(k)][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()];
//		
//					for(int ii = 0; ii < (1 << ords.size()); ii++)
//						for(int jj = 0; jj < ords.size(); jj++)
//							for(int kk = 0; kk < ords.size(); kk++)
//								if(jj != kk && (ii & (1 << jj)) != 0 && (ii & (1 << kk)) != 0)
//									if(dp[ii - (1 << jj)][kk] != Integer.MAX_VALUE && distFromAdj[kk][adjacent.get(jj).getX()][adjacent.get(jj).getY()] != Integer.MAX_VALUE)
//										dp[ii][jj] = Math.min(dp[ii][jj], dp[ii - (1 << jj)][kk] + distFromAdj[kk][adjacent.get(jj).getX()][adjacent.get(jj).getY()]);
//		
//					int best = Integer.MAX_VALUE;
//					for(int k = 0; k < ords.size(); k++)
//						best = Math.min(best, dp[(1 << ords.size()) - 1][k]);
//					dist[i][j] = best;
//	
//					if(j == 0)
//						dist[i][j] = 0;
//				}
		}
	
		int best = Integer.MAX_VALUE;
		List<Integer> dirs = new ArrayList<Integer>();
		if(friendlyUnits.size() == 0)
			best = Integer.MAX_VALUE;
		else if(friendlyUnits.size() == 1)
		{
			best = lookup[0][(1 << adjacent.size()) - 1];
			dirs.clear();
			dirs.add((1 << adjacent.size()) - 1);
		}
		else if(friendlyUnits.size() == 2)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					if((i & j) == 0 && (i | j) == (1 << adjacent.size()) - 1)
						if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE)
						{
							if(Math.max(lookup[0][i], lookup[1][j]) < best)
							{
								best = Math.max(lookup[0][i], lookup[1][j]);
								dirs.clear();
								dirs.add(i);
								dirs.add(j);
							}
						}
		}
		else if(friendlyUnits.size() == 3)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					for(int k = 0; k < (1 << adjacent.size()); k++)
						if((i & j) == 0 && (i & k) == 0 && (j & k) == 0 && (i | j | k) == (1 << adjacent.size()) - 1)
							if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE && lookup[2][k] != Integer.MAX_VALUE)
							{
								if(Math.max(lookup[0][i], Math.max(lookup[1][j], lookup[2][k])) < best)
								{
									best = Math.max(lookup[0][i], Math.max(lookup[1][j], lookup[2][k]));
									dirs.clear();
									dirs.add(i);
									dirs.add(j);
									dirs.add(k);
								}
							}
		}
		else if(friendlyUnits.size() == 4)
		{
			for(int i = 0; i < (1 << adjacent.size()); i++)
				for(int j = 0; j < (1 << adjacent.size()); j++)
					for(int k = 0; k < (1 << adjacent.size()); k++)
						for(int l = 0; l < (1 << adjacent.size()); l++)
							if((i & j) == 0 && (i & k) == 0 && (i & l) == 0 && (j & k) == 0 && (j & l) == 0 && (k & l) == 0 && (i | j | k | l) == (1 << adjacent.size()) - 1)
								if(lookup[0][i] != Integer.MAX_VALUE && lookup[1][j] != Integer.MAX_VALUE && lookup[2][k] != Integer.MAX_VALUE && lookup[3][l] != Integer.MAX_VALUE)
								{
									if(best < Math.max(lookup[0][i], Math.max(lookup[1][j], Math.max(lookup[2][k], lookup[3][l]))))
									{
										best = Math.max(lookup[0][i], Math.max(lookup[1][j], Math.max(lookup[2][k], lookup[3][l])));
										dirs.clear();
										dirs.add(i);
										dirs.add(j);
										dirs.add(k);
										dirs.add(l);
									}
								}
		}
		else
		{
			System.out.println("watthfuk " + friendlyUnits.size());
			System.exit(0);
		}

		for(int i = 0; i < dirs.size(); i++)
		{
			int j = dirs.get(i);
			boolean f = false;
			for(int k = 0; k < adjacent.size(); k++)
				if(dist[i][j][k] == lookup[i][j])
				{
					int s = start[i][j][k];
					boolean ff = false;
					for(Tile adj : world.getTilesAround(friendlyUnits.get(i).getPosition()).values())
					{
						Point nxt = adj.getPosition();
						if(distFromAdj[s][adj.getPosition().getX()][adj.getPosition().getY()] == 1 + distFromAdj[s][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()])
						{
							world.move(friendlyUnits.get(i), adj.getPosition());
							ff = true;
							break;
						}
					}

					if(!ff)
					{
						System.out.println("ff wattthhh");
						System.exit(0);
					}
					f = true;
					break;
				}

			if(!f)
			{
				System.out.println("f watthhh");
				System.exit(0);
			}
		}
	}
	public static void buildNest(Point nest, World world, List<FriendlyUnit> friendlyUnits, State[][] board) {

		System.out.println(friendlyUnits.size());
//		if(!(friendlyUnits.size() <= 4))
//		{
//			System.out.println("wat " + nest);
//			System.exit(-1);
//		}
//		for(FriendlyUnit friend : friendlyUnits)
//		{
//			if(!(nest.getX() - 2 <= friend.getPosition().getX() && friend.getPosition().getX() <= nest.getX() + 2))
//				System.exit(-1);
//			if(!(nest.getY() - 2 <= friend.getPosition().getY() && friend.getPosition().getY() <= nest.getY() + 2))
//				System.exit(-1);
//		}

		List<Point> adjacent = new ArrayList<Point>();
		for(Tile neighbour : world.getTilesAround(nest).values())
			if(neighbour.isNeutral())
				adjacent.add(neighbour.getPosition());

		if(adjacent.size() == 0)
			return;
		System.out.println(adjacent);

		int[][][] distFromAdj = new int[adjacent.size()][19][19];
		for(int i = 0; i < adjacent.size(); i++)
		{
			Queue<Point> q = new LinkedList<Point>();
			q.offer(adjacent.get(i));

			for(int j = 0; j < 19; j++)
				for(int k = 0; k < 19; k++)
					distFromAdj[i][j][k] = Integer.MAX_VALUE;
			distFromAdj[i][adjacent.get(i).getX()][adjacent.get(i).getY()] = 0;

			while(!q.isEmpty())
			{
				Point cur = q.poll();

				for(Tile adj : world.getTilesAround(cur).values())
				{
					Point nxt = adj.getPosition();
					if(distFromAdj[i][nxt.getX()][nxt.getY()] == Integer.MAX_VALUE)
						if(board[nxt.getX()][nxt.getY()] == null)
						{
							distFromAdj[i][nxt.getX()][nxt.getY()] = distFromAdj[i][cur.getX()][cur.getY()] + 1;
							q.offer(nxt);
						}
				}
			}
		}

		boolean[] used = new boolean[adjacent.size()];
		for(FriendlyUnit friend : friendlyUnits)
		{
			System.out.println(friend.getPosition());
			int best = Integer.MAX_VALUE, ans = -1;
			for(int i = 0; i < adjacent.size(); i++)
			{
				int x = friend.getPosition().getX(), y = friend.getPosition().getY();
				if(board[x][y] == State.NEST)
				{
					for(Tile adj : world.getTilesAround(friend.getPosition()).values())
					{
						Point nxt = adj.getPosition();
						if(distFromAdj[i][nxt.getX()][nxt.getY()] + 1 < best)
						{
							best = distFromAdj[i][nxt.getX()][nxt.getY()] + 1;
							ans = i;
						}
					}
				}
				else
				{
					int d = distFromAdj[i][x][y];
					if(d < best)
					{
						best = d;
						ans = i;
					}
				}
			}

			System.out.println(ans);
			if(ans == -1)
				System.exit(-1);
			used[ans] = true;
			for(Tile adj : world.getTilesAround(friend.getPosition()).values())
			{
				Point nxt = adj.getPosition();
				if(distFromAdj[ans][nxt.getX()][nxt.getY()] < best)
					if(board[nxt.getX()][nxt.getY()] == null)
					{
						world.move(friend, nxt);
						break;
					}
			}
		}

//		int[][] dist = new int[friendlyUnits.size()][1 << adjacent.size()];
//		for(int i = 0; i < friendlyUnits.size(); i++)
//			for(int j = 0; j < (1 << adjacent.size()); j++)
//			{
//				List<Integer> ords = new ArrayList<Integer>();
//				for(int k = 0; k < adjacent.size(); k++)
//					if((j & (1 << k)) != 0)
//						ords.add(k);
//
//				int[][] dp = new int[1 << ords.size()][ords.size()];
//				for(int k = 0; k < ords.size(); k++)
//					dp[1 << k][k] = distFromAdj[k][friendlyUnits.get(i).getPosition().getX()][friendlyUnits.get(i).getPosition().getY()];
//
//				for(int ii = 0; ii < (1 << ords.size()); ii++)
//					for(int jj = 0; jj < ords.size(); jj++)
//						for(int kk = 0; kk < ords.size(); kk++)
//							if(jj != kk && (ii & (1 << jj)) != 0 && (ii & (1 << kk)) != 0)
//								dp[ii][jj] = Math.min(dp[ii][jj], dp[ii - (1 << jj)][kk] + distFromAdj[kk][adjacent.get(jj).getX()][adjacent.get(jj).getY()]);
//
//				int best = Integer.MAX_VALUE;
//				for(int k = 0; k < ords.size(); k++)
//					best = Math.min(best, dp[(1 << ords.size()) - 1][k]);
//			}
//
//		int best = Integer.MAX_VALUE;
//		if(friendlyUnits.size() == 2)
//			for(int i = 0; i < (1 << adjacent.size()); i++)
//				for(int j = 0; j < (1 << adjacent.size()); j++)
//					if((i & j) == 0 && (i | j) == (1 << adjacent.size()) - 1)
//						best = Math.min(best, dist[0][i] + dist[1][j]);
	}

	public Point getNestPosition() {
		return nestPosition;
	}

	public Type getType() {
		return Task.Type.NestBuilder;
	}
}
