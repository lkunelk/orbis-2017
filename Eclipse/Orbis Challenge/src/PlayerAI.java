import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orbischallenge.firefly.client.objects.models.EnemyUnit;
import com.orbischallenge.firefly.client.objects.models.FriendlyUnit;
import com.orbischallenge.firefly.client.objects.models.World;

public class PlayerAI {
    // Any field declarations go here

	private Map<String, Task> tasks = new HashMap<String, Task>();
	private State[][] board = new State[19][19];

    public void assignTasks(World world, List<FriendlyUnit> friendlyUnits) {
    	
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

    	List<FriendlyUnit> newbies = new ArrayList<FriendlyUnit>();
    	for(FriendlyUnit friend : friendlyUnits)
    		if(!tasks.containsKey(friend.getUuid()))
    			newbies.add(friend);
    	assignTasks(world, newbies);

//    	buildNests();
//    	buildFortress();
    }

//    public void buildNests(World world, FriendlyUnit[] friendlyUnits, EnemyUnit[] enemyUnits) {
//    	
//    }
}