package controllers.Astar;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Agent extends controllers.sampleRandom.Agent{
    private class Node implements Comparable<Node>
    {
        @Override
        public int compareTo(Node o) {
            return Double.compare(this.F, o.F);
        }
        public double G;//to store the previous cost
        public double H;//to store the future cost
        public double F;//to store the predicted cost
        public StateObservation nodeCopy;//to store the current node information
        public LinkedList<Types.ACTIONS> preActions=null;//to store the parent actions
        public LinkedList<StateObservation> preStates=null;//to store the parent states
        //public StateObservation parent_state=null;
        public boolean key=false;
        public Vector2d avatarPosition;

        /**
         *
         * @param action_ the parent actions
         * @param state_ the parent states
         */
        public Node(LinkedList<Types.ACTIONS> action_, LinkedList<StateObservation> state_)
        {
            if(state_!=null)
            {
                nodeCopy= state_.getLast().copy();
            }

            preActions=action_;
            preStates=state_;
            //System.out.println("preStates size is:   "+preStates.size());
            //parent_state=state_.getLast().copy();
            if(state_==null)
            {
                key=false;
                keyOut=false;
            }
            else {
                key=KEY();
                keyOut=KEY();
            }
            avatarPosition=nodeCopy.copy().getAvatarPosition();
            //System.out.println("avatarPosition is:  "+avatarPosition);
        }
        public void init_node(double g)
        {
            G=g;
            H=heuristic();
            F=G+H;

        }
        public double heuristic()
        {
            if(key)
            {
                return Math.abs(avatarPosition.x-goalPosition.x)+Math.abs(avatarPosition.y-goalPosition.y)
                        +box_and_hole()*0.2+preStates.getLast().copy().getGameScore();
            }
            else {
                return Math.abs(avatarPosition.x-keyPosition.x)+Math.abs(avatarPosition.y-keyPosition.y)
                        +Math.abs(keyPosition.x-goalPosition.x)+Math.abs(keyPosition.y-goalPosition.y)
                        +box_and_hole()*0.2+preStates.getLast().copy().getGameScore();
            }
        }
        public double box_and_hole()
        {
            double cost=0;
            ArrayList<Observation>[] fixedPositions = nodeCopy.copy().getImmovablePositions();
            ArrayList<Observation>[] movingPositions = nodeCopy.copy().getMovablePositions();
            ArrayList<Observation> holes=null;
            ArrayList<Observation> boxes=null;
            if(fixedPositions.length>2)
            {
                holes=fixedPositions[fixedPositions.length-2];
            }
            if(movingPositions.length>1)
            {
                boxes=movingPositions[movingPositions.length-1];
            }
            if(holes!=null&&boxes!=null)
            {
                for(Observation hole:holes)
                {
                    for(Observation box:boxes)
                    {
                        cost+=Math.abs(hole.position.x-box.position.x)+Math.abs(hole.position.y-box.position.y);
                    }
                }
            }
            return cost;
        }
        public boolean KEY()
        {
            for(StateObservation state:this.preStates)
            {
                Vector2d avatarPosition=state.copy().getAvatarPosition();
                if(avatarPosition.x==keyPosition.x&&avatarPosition.y==keyPosition.y) {
                    return true;
                }
            }
            return false;
        }

    }/**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        super(so, elapsedTimer);
    }
    private double heuristic_min_distance=1000000;//set a maximum for comparision.
    private LinkedList<StateObservation> searchedstates=null;
    private LinkedList<Types.ACTIONS> searchedactions=new LinkedList<>();

    private boolean keyOut=false;//use it to make judgement whether avatar get the key as this relate to the distance.
    public Vector2d keyPosition;
    public Vector2d goalPosition;
    private PriorityQueue<Node> Open;
    private ArrayList<Node> Close=new ArrayList<>();
    private ArrayList<Node> Exist=new ArrayList<>();
    //public int times=0;
    public int depth=5;
    public boolean is_exist(Node n)
    {
        for(Node node:Exist)
        {
            if(node.nodeCopy.copy().equalPosition(n.nodeCopy.copy()))
            {
                return true;
            }
        }
        return false;
    }

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
        if(searchedstates==null)
        {
            searchedstates=new LinkedList<>();
        }
        //searchedstates=new LinkedList<>();
        //searchedactions=new LinkedList<>();
        goalPosition=fixedPositions[1].get(0).position;
        searchedstates.addLast(stateObs.copy());
        if(goalPosition==null)
        {
            goalPosition=fixedPositions[1].get(1).position;
        }
        System.out.println("key status is: "+keyOut);
        if(movingPositions[0].size()==0)//to check whether the key is possessed by the avatar.
        {
            keyOut=true;
        }
        if(!keyOut)
        {
            keyPosition=movingPositions[0].get(0).position;
        }
        /*LinkedList<StateObservation> parentstates=new LinkedList<>();
        parentstates.add(stCopy.copy());
        Open=new PriorityQueue<>();
        Node start=new Node(null, parentstates);
        start.init_node(0);
        Open.add(start);
        Exist.add(start);*/
        if(Open==null)
        {
            Open=new PriorityQueue<>();
            Node start=new Node(null,searchedstates);
            start.init_node(0);
            Open.add(start);
            Exist.add(start);
            //times=0;//when establish a new searching, we need to update the time we do act.
        }
        //LinkedList<Types.ACTIONS> actionslist=null;
        System.out.println();
        System.out.println("######################################");
        Node c_node=ASTAR();
        int step=0;
        for(StateObservation state:c_node.preStates)//to find the state on the way, and
        {
            step++;
            if(state.copy().equalPosition(stCopy))
            {
                break;
            }
        }
        int action_step=c_node.preStates.size()-step;//this is for the gap between current step and searched step.
        System.out.println("Prestep is: "+step);
        System.out.println("preState is "+c_node.preStates.size());
        //searchedactions.addLast(actionslist.get(times));//times));//add the second last action to the current searchedaction list.
        //heuristic_min_distance=1000000;//reset the heuristic_min_distance as we need to do LDS every step.
        //times++;//update the time as our next action to do is the times++^th.
        System.out.println("preActions length: "+c_node.preActions.size());
        if(step-1>=c_node.preActions.size())
        {
            return null;
            /*
            if(c_node.preActions.size()-action_step<=0)
            {
                return null;
            }
            action=c_node.preActions.get(c_node.preActions.size()-action_step);
            return action;
            */
        }
        /*if(c_node.preActions.size()-action_step<0)
        {
            return null;
        }*/
        action=c_node.preActions.get(step-1);
        System.out.println("ACTION!!");
        System.out.println("The returned action is :"+action);
        return action;
    }
    private boolean check_state(StateObservation stCopy, Node parent)
    {
        for(StateObservation obs:parent.preStates)
        {
            if(stCopy.copy().equalPosition(obs.copy()))
            {
                return true;
            }
        }
        return false;
    }
    private Node Find_node(Node n)
    {
        for(Node node:Exist)
        {
            if(n.nodeCopy.copy().equalPosition(node.nodeCopy.copy()))
            {
                return node;
            }
        }
        return null;
    }
    private boolean check_node(StateObservation obs)
    {
        for(Node node_: Exist)
        {
            if(node_.nodeCopy.copy().equalPosition(obs.copy()))
            {
                return true;

            }
        }
        return false;

    }
    private Node ASTAR(){//each time we return an action back, we indeed return the preActions of one current_node.
        //LinkedList<Types.ACTIONS> actionsback=new LinkedList<>();//this is for returning a preActions
        int cur_depth=0;
        while(!Open.isEmpty())
        {
            cur_depth++;
            Node current_node= Open.poll();
            System.out.println("The current_node avatarPosition is: "+current_node.avatarPosition);
            Close.add(current_node);
            ArrayList<Observation>[] movingPositions = current_node.nodeCopy.getMovablePositions();
            printDebug(movingPositions, "mov");
            System.out.println();
            ArrayList<Observation>[] moving = current_node.nodeCopy.copy().getMovablePositions();
            ArrayList<Observation> boxes=null;
            boxes=moving[moving.length-1];
            for(Observation box:boxes)
            {
                System.out.println("A box lies in position: "+box.position.x+" and "+box.position.y);
            }
           ArrayList<Types.ACTIONS> action_to_do =current_node.nodeCopy.getAvailableActions();
           for(Types.ACTIONS action:action_to_do)
           {
               System.out.println(action);
           }
           if(current_node.nodeCopy.copy().getGameWinner()==Types.WINNER.PLAYER_WINS)
           {
               Open.add(current_node);
               //actionsback=current_node.preActions;
               return  current_node;
           }
           if(cur_depth==depth)
           {
               Open.add(current_node);
               //actionsback=current_node.preActions;
               return current_node;
           }
           for(Types.ACTIONS action:action_to_do)
           {
               System.out.println("The current action is:"+action);
               StateObservation obs_next=current_node.nodeCopy.copy();
               StateObservation check_state=obs_next.copy();
               check_state.advance(action);
               System.out.println("AvatarPosition is:"+check_state.getAvatarPosition());
               if(check_state(check_state, current_node)||check_state.getGameWinner()==Types.WINNER.PLAYER_LOSES)
               {
                   continue;
               }
               Node temp_node=null;//to store the newly appended node.
               LinkedList<Types.ACTIONS> actionlist=null;
               LinkedList<StateObservation> nodeparent=(LinkedList<StateObservation>) current_node.preStates.clone();
               if(current_node.preActions==null)
               {
                   actionlist=new LinkedList<>();
               } else if (current_node.preActions.size()>0) {
                   actionlist=(LinkedList<Types.ACTIONS>) current_node.preActions.clone();
               }
               actionlist.addLast(action);
               obs_next.advance(action);
               nodeparent.addLast(obs_next);
               temp_node=new Node(actionlist, nodeparent);
               if(current_node.preActions==null)
               {
                   temp_node.init_node(0);
               }
               else if(current_node.preActions.size()>0)
               {
                   temp_node.init_node(current_node.preActions.size()*50);
               }
               System.out.println("Temp_node.F is:"+temp_node.F);
               if(is_exist(temp_node))
               {
                   Node same_node=Find_node(temp_node);
                   if(same_node.F<temp_node.F)
                   {
                       continue;
                   }
               }
               Open.add(temp_node);
               Exist.add(temp_node);

           }
        }
        /*
        //First, I need to poll the first element in the priority queue as current node.
        //then, if the current node don' t exist in the Exist array, then, we iterate the actions of it.
        //System.out.println("ASRAR");
        //System.out.println(Open.isEmpty());
        while(!Open.isEmpty())
        {
            Node current_node=Open.poll();
            Close.add(current_node);
            System.out.println("The current_position of the avatar is "+current_node.avatarPosition);
            //Exist.add(current_node);
            /*for(int i=0;i<current_node.preActions.size()-1;i++)
            {
                System.out.println(current_node.preActions.get(i));
            }
            System.out.println("The game has a winner?="+(current_node.nodeCopy.copy().getGameWinner()==Types.WINNER.PLAYER_WINS));
            if(current_node.nodeCopy.copy().getGameWinner()==Types.WINNER.PLAYER_WINS)
            {
                Open.add(current_node);
                Exist.add(current_node);
                return current_node.preActions;
            }
            ArrayList<Types.ACTIONS> action_done=current_node.nodeCopy.copy().getAvailableActions();
            System.out.println("Actions can be done:");
            for(Types.ACTIONS action:action_done)
            {
                System.out.println(action);
            }
            System.out.println("#####################       #######################");
            for(Types.ACTIONS action:action_done)
            {
                System.out.println(action);
                StateObservation obs_next= current_node.nodeCopy.copy();
                obs_next.advance(action);
                //System.out.println(is_exist(current_node));
                System.out.println("check state is  "+check_node(obs_next.copy()));
                System.out.println("game lose is  "+ (obs_next.copy().getGameWinner()==Types.WINNER.PLAYER_LOSES));
                if(check_node(obs_next.copy())||obs_next.copy().getGameWinner()==Types.WINNER.PLAYER_LOSES)
                {
                    continue;//if the node is already in the Exist list or we get a winner, we can stop from
                    // iterate the actions
                }
                Node temp=null;//temp is a possible element that may be appended to the Open Queue.
                LinkedList<Types.ACTIONS> pastActions=null;
                System.out.println("preActions is   "+(current_node.preActions==null));
                if(current_node.preActions==null)
                {
                    pastActions=new LinkedList<>();

                }
                else
                {
                    pastActions=(LinkedList<Types.ACTIONS>) current_node.preActions.clone();

                }
                pastActions.add(action);
                LinkedList<StateObservation> pastStates=(LinkedList<StateObservation>) current_node.preStates.clone();
                obs_next.copy().advance(action);
                pastStates.add(obs_next);
                temp=new Node(pastActions,pastStates);
                if(pastActions.size()>0)
                {
                    temp.init_node(pastActions.size()*50);
                }
                else if(pastActions.size()==0)
                {
                    temp.init_node(0);
                }
                if(is_exist(temp))
                {
                    Node same_node=Find_node(temp);
                    if(same_node.F<temp.F)
                    {
                       continue;
                    }


                }

                    Open.add(temp);
                    Exist.add(temp);
                    System.out.println("New Node adding successfully!");
                    System.out.println("##########  ############  ############");


            }
            System.out.println("#########################################");
        }*/
        System.out.println("#############################################");
        System.out.println("The list of return action is here:");
        //for(Types.ACTIONS action:actionsback)
        //{
        //    System.out.println(action);
        //}
        System.out.println("###############################################");
        return null;
        //return actionsback;
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
