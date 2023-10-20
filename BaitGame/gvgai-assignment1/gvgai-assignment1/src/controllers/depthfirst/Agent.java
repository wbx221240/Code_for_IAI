package controllers.depthfirst;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import core.game.Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

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
    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    private LinkedList<StateObservation> searchedstates=null;
    private LinkedList<Types.ACTIONS> searchedactions=null;
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
        if(searchedactions==null)
        {
            searchedactions=DFS(stCopy);
        }
        if(searchedactions.size()>0)
        {
            action=searchedactions.remove(0);
        }

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
    private LinkedList<Types.ACTIONS> DFS(StateObservation stCopy)
    {
        if(searchedstates==null){//if this is the first state
            searchedstates=new LinkedList<>();
        }
        LinkedList<Types.ACTIONS> actionlist=new LinkedList<>();
        StateObservation obs=stCopy.copy();
        ArrayList<Types.ACTIONS> action_done=obs.getAvailableActions();
        for(Types.ACTIONS action:action_done) {
            System.out.println(action);
        }
        for(Types.ACTIONS action:action_done) {
            actionlist.addLast(action);
            searchedstates.addLast(obs.copy());//add the copy to searchedstates
            obs.advance(action);//advance the action
            if(obs.copy().getGameWinner()==Types.WINNER.PLAYER_WINS) {
                return actionlist;
            }
            LinkedList<Types.ACTIONS> actions_next=null;
            if(check_state(obs.copy())||(actions_next= DFS(obs.copy()))==null) {//recursively call the DFS
                actionlist.removeLast();
                obs=stCopy.copy();
            }
            else {
                for(Types.ACTIONS action_:actions_next) {
                    actionlist.addLast(action_);
                }
                return actionlist;//add searched actions to actionlist for returning.
            }
        }
        // the implementation of Stack
        /*Stack<Types.ACTIONS> actionlist=new Stack<>();
        Stack<StateObservation> statelist=new Stack<>();
        statelist.push(stCopy);
        while(!statelist.empty())
        {
            StateObservation obs=statelist.pop();
            if(obs.copy().getGameWinner()==Types.WINNER.PLAYER_WINS)
            {
                return actionlist.firstElement();
            }
            ArrayList<Types.ACTIONS> action_done=obs.getAvailableActions();
            for(Types.ACTIONS action: action_done)
            {
                actionlist.push(action);
                statelist.push(obs.copy());
                obs.advance(action);
                if(check_state(obs.copy())) {
                    statelist.pop();
                    actionlist.pop();
                    obs = obs.copy();
                }
            }



        }*/
        return null;
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
