package controllers.LimitedDepthFirst;

import core.game.ForwardModel;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Agent extends controllers.sampleRandom.Agent{

    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        super(so, elapsedTimer);
    }
    private int depth=5;
    private double heuristic_min_distance=1000000;//set a maximum for comparision.
    private LinkedList<StateObservation> searchedstates=new LinkedList<>();
    private LinkedList<Types.ACTIONS> searchedactions=new LinkedList<>();
    private LinkedList<Types.ACTIONS> bestactions=new LinkedList<>();
    private boolean key=false;//use it to make judgement whether avatar get the key as this relate to the distance.
    public Vector2d keyPosition;
    public Vector2d goalPosition;
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        // read map information
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
        grid = stateObs.getObservationGrid();

        printDebug(npcPositions, "npc");
        printDebug(fixedPositions, "fix");
        printDebug(movingPositions, "mov");
        printDebug(resourcesPositions, "res");
        printDebug(portalPositions, "por");
        System.out.println();

        Types.ACTIONS action = null;
        StateObservation stCopy = stateObs.copy();
        searchedactions=new LinkedList<>();
        bestactions=new LinkedList<>();
        goalPosition=fixedPositions[1].get(0).position;
        if(!key)
        {
            keyPosition=movingPositions[0].get(0).position;
        }

        LDS(stCopy.copy(), depth);
        for(Types.ACTIONS actions:bestactions)
        {
            System.out.println(actions);
        }
        heuristic_min_distance=1000000;//reset the heuristic_min_distance as we need to do LDS every step.
        action=bestactions.get(0);
        bestactions.removeFirst();
        System.out.println(action);
        System.out.println("ACTION!!");
        return action;
    }
    private boolean check_state(StateObservation stCopy)
    {
        for(StateObservation obs:searchedstates)
        {
            if(stCopy.copy().equalPosition(obs.copy()))
            {
                return true;
            }
        }
        return false;
    }
    private void LDS(StateObservation stCopy, int current_depth)
    {
        System.out.println("current depth   "+current_depth);
        //LinkedList<Types.ACTIONS> actionlist=new LinkedList<>();//append the action to the searchedactions directly.
        //StateObservation obs=stCopy.copy();
        searchedstates.addLast(stCopy.copy());
        ArrayList<Types.ACTIONS> action_done=stCopy.copy().getAvailableActions();
        for(Types.ACTIONS action:action_done)
        {
            StateObservation obs=stCopy.copy();
            //searchedstates.addLast(obs.copy());
            obs.advance(action);
            searchedactions.addLast(action);
            Vector2d avatarpos=obs.copy().getAvatarPosition();
            System.out.println("current action is   "+action);
            System.out.println("avatar   "+avatarpos);
            System.out.println("keyPosition   "+keyPosition);
            if(avatarpos.x==keyPosition.x&&avatarpos.y==keyPosition.y)
            {
                key=true;
            }
            System.out.println("key:"+key);
            if(obs.copy().getGameWinner()== Types.WINNER.PLAYER_WINS)
            {
                System.out.println("Winner:"+(obs.copy().getGameWinner()== Types.WINNER.PLAYER_WINS));
                heuristic_min_distance=0;//to represent the ending of finding.
                bestactions=(LinkedList<Types.ACTIONS>) searchedactions.clone();
                return;
            }
            if(current_depth==0)//that means the depth is finished, then, we should compare the current_method
            // distance and the previous distance to choose the best way to be tha bestactions list.
            {
                double now_distance=heuristic_distance(obs.copy());
                if(now_distance<heuristic_min_distance)
                {
                    heuristic_min_distance=now_distance;
                    bestactions=(LinkedList<Types.ACTIONS>) searchedactions.clone();
                }
            }
            else if(!check_state(obs.copy()))
            {
                System.out.println("state check:"+check_state(obs.copy()));
                LDS(obs.copy(), current_depth-1);

            }
            searchedactions.removeLast();
            //obs=stCopy.copy();
        }
        //only those can' t move up and those can' t make an end will do this.
        searchedstates.removeLast();
    }
    private double heuristic_distance(StateObservation stCopy)
    {
        Vector2d avatarPosition=stCopy.copy().getAvatarPosition();
        //goalPosition=stCopy.copy().getImmovablePositions()[1].get(0).position;
        //keyPosition=stCopy.copy().getMovablePositions()[0].get(0).position;
        if(key)//if the avatar has a key.
        {
            return Math.abs(avatarPosition.x-goalPosition.x)+Math.abs(avatarPosition.y-goalPosition.y);
        }
        else if(!key)
        {
            return Math.abs(avatarPosition.x-keyPosition.x)+Math.abs(avatarPosition.y-keyPosition.y)
                    +Math.abs(keyPosition.x-goalPosition.x)+Math.abs(keyPosition.y-goalPosition.y);
        }
        return 0;
    }
    private void printDebug(ArrayList<Observation>[] positions, String str)
    {
        if(positions != null){
            System.out.print(str + ":" + positions.length + "(");
            for (int i = 0; i < positions.length; i++) {
                System.out.print(positions[i].size() + ",");

            }
            System.out.print("); ");
        }else System.out.print(str + ": 0; ");
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {
                if(grid[i][j].size() > 0)
                {
                    Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i*block_size+half_block,j*block_size+half_block);
                }
            }
        }
    }
}
