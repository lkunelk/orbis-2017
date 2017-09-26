import java.util.List;
import java.util.Map;

import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.Tile;
import com.orbischallenge.firefly.client.objects.models.World;
import com.orbischallenge.game.engine.Point;

public class NestBreaking extends Task{
	
	public Point nestPosition;
	
	public NestBreaking(Point target){
		nestPosition = target;
	}
	
	public static void breakNest(World world, FriendlyUnit[] friendlyUnits, State[][] board, Map<String, Task> tasks) {
		for(FriendlyUnit u: friendlyUnits) {
			if(tasks.get(u.getUuid()).getType() == Task.Type.NestBreaker) {
				world.move(u, ((NestBreaking)tasks.get(u.getUuid())).nestPosition);
			}
		}
	}
	
	@Override
	Type getType() {
		return Type.NestBreaker;
	}
}
