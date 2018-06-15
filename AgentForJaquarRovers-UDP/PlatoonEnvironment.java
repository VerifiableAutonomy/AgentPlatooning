package eass.imp.platooning.udp;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import ail.mas.scheduling.NActionScheduler;
import eass.imp.platooning.udp.util.Leader;
import eass.imp.platooning.udp.util.Vehicle;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.Message;
import ail.syntax.NumberTerm;
import ail.syntax.NumberTermImpl;
import ail.syntax.Predicate;
import ail.syntax.SendAction;
import ail.syntax.StringTermImpl;
import ail.syntax.Term;
import ail.syntax.Unifier;
import ail.syntax.VarTerm;
import ail.util.AILexception;
import ajpf.util.AJPFLogger;
import eass.mas.DefaultEASSEnvironment;


//import eass.mas.matlab.EASSMatLabEnvironment;


public class PlatoonEnvironment extends DefaultEASSEnvironment{
	String logname = "eass.imp.platooning.udp.PlatoonEnvironment";

	int initial= 0;
	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	Vehicle v1 = new Vehicle(1);
	int counter=0;
	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment() {
		super();
	//	RoundRobinScheduler s = new RoundRobinScheduler();
		NActionScheduler s = new NActionScheduler(20);
		s.addJobber(this);
		setScheduler(s);
		addPerceptListener(s);
	}
		
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#initialise()
	 */
	public void initialise() {
		super.initialise();
		
		vehicles.add(v1);
		
        /*
		// Assume v1 is the leader
		Literal id1 = new Literal("id");
		id1.addTerm(new Literal("leader"));	
		id1.addTerm(new NumberTermImpl(1));	
		id1.addTerm(new Literal("agent2"));
		addPercept("abstraction_leader", id1);
		*/
        
        Scanner in = new Scanner(System.in);
        int agentId = in.nextInt();
        String agentBehind = in.next();
        
        Literal id = new Literal("id");
        id.addTerm(new Literal((agentId == 1) ? "leader" : "agent" + agentId));
        id.addTerm(new NumberTermImpl(agentId));
        id.addTerm(new Literal(agentBehind));
        addPercept("abstraction_" + ((agentID == 1) ? "leader" : "agent" + v1.getID()), id);
        
        /*
        1 if leader
        >= 2 if agent
            
        id is always "id"
        first term is "leader" if 1 ir "agent"+num if num > 1
        third term is just number
            
        java -jar something PATH num third-term
            
        try
        {
            int x = Integer.parseInt(the argument)
        }
        catch(Exception e)
        {
            
        }
        */
        
        /*
		Literal id2 = new Literal("id");
		id2.addTerm(new Literal("agent2"));
		id2.addTerm(new NumberTermImpl(2));	
		id2.addTerm(new Literal("last"));
		addPercept("abstraction_agent"+ v2.getID(), id2);
		
		Literal id3 = new Literal("id");
		id3.addTerm(new Literal("agent3"));	
		id3.addTerm(new NumberTermImpl(3));	
		id3.addTerm(new Literal("none"));
		addPercept("abstraction_agent"+ v3.getID(), id3);
         */
        
        if(agentID == 1) {
            // according to j_pos value, leader add a percept about allowed_position
            Literal allowed_p = new Literal("allowed_position");
            allowed_p.addTerm(new Literal("agent2"));
            /*		allowed_p.addTerm(new Literal("follower2")); */
            // join from behind, allowed_position is 0
            /*		allowed_p.addTerm(new NumberTermImpl(0)); */
            addPercept("abstraction_leader", allowed_p);
            
            Literal platoon_member2 = new Literal("platoon_m");
            platoon_member2.addTerm(new Literal("leader"));
            platoon_member2.addTerm(new Literal("agent2"));
            addPercept("abstraction_leader", platoon_member2);
        }
        
        if(agentID == 3){
            // in front of which vehicle follower3 wants to join to platoon, e.g., here follower2
            Literal j_pos = new Literal("j_pos");
            j_pos.addTerm(new Literal("agent2"));
            j_pos.addTerm(new NumberTermImpl(1));
            /*		j_pos.addTerm(new Literal("follower2"));
             j_pos.addTerm(new NumberTermImpl(2));   */
            // join from behind, j_pos == 0
            /*		j_pos.addTerm(new Literal("0"));
             j_pos.addTerm(new NumberTermImpl(0)); */
            addPercept("abstraction_agent"+ v3.getID(), j_pos);
        }
		
		
//		Literal platoon_member = new Literal("platoon_m");
//		platoon_member.addTerm(new Literal("follower1"));
//		platoon_member.addTerm(new Literal("follower2"));
//		addPercept("abstraction_agent"+ v1.getID(), platoon_member);				
	}

	public void eachrun() {

		counter++;		
		if(counter ==2){
		for (Vehicle v: vehicles) {			
			v.update();
			
//			if(v.getID()==1){
//				// decide if the leader needs raw data from its vehicle
//				try {
//					addMessage("leader", v.getMessage());
//				} catch (SocketTimeoutException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}

			
				Literal speed = new Literal("speed");
				//System.out.println("speed is "+ v.getSpeed());
				speed.addTerm(new NumberTermImpl(v.getSpeed()));
				addUniquePercept("abstraction_agent"+ v.getID(), "speed", speed);

				Literal timeStamp = new Literal("timeStamp");
				timeStamp.addTerm(new NumberTermImpl(v.getTimeStamp()));
				//System.out.println("follower "+v.getID()+" time is "+ v.getTimeStamp());
				addUniquePercept("abstraction_agent"+ v.getID(), "timeStamp", timeStamp);

				Literal lateral = new Literal("lateral");
				lateral.addTerm(new NumberTermImpl(v.getLateralPosition()));
				addUniquePercept("abstraction_agent"+ v.getID(), "lateral", lateral);
			
				Literal leaderLatency = new Literal("leaderLatency");
				leaderLatency.addTerm(new NumberTermImpl(v.getLeaderLatency()));
				addUniquePercept("abstraction_agent"+ v.getID(), "leaderLatency", leaderLatency);

				Literal distance = new Literal("distance");
				distance.addTerm(new NumberTermImpl(v.getDistance()));
				addUniquePercept("abstraction_agent"+ v.getID(), "distance", distance);		
				
				if(v.getID()==1){
					addMessage("leader", v.getAgentMessage());
				}
				addMessage("agent"+ v.getID(), v.getAgentMessage());
				counter=0;
			
		}
		} 
	}
	
	public boolean done() {
		   return false;
		}

	public Unifier executeAction(String agName, Action act) throws AILexception {
		   
		Unifier u = new Unifier();
		boolean printed = false;
		 
	   if (act.getFunctor().equals("run")) {
//			   int agentnum = Integer.parseInt(((String) agName).substring(14));
//			   System.out.println("agentnumber is "+ agentnum+ " and its name is "+ agName);
			   Predicate predlist = (Predicate) act.getTerm(0);
			   Predicate predargs = (Predicate) act.getTerm(1);
//			   int num_args = predargs.getTermsSize();
//			   System.out.println("number of arguments is "+ num_args);

			   //			   String num_arg_s = "" + num_args;
			   int num_name_comps = predlist.getTermsSize();
//			   System.out.println("length of action predicate is "+ num_name_comps);

			   String predname = "";

			   // to extract predname as String
			   for (int i=0; i<num_name_comps; i++) {
				   // Lots of extra work to get the string correct.
				   Term nb = predlist.getTerm(i);
				   String s = nb.toString();
				   if (nb instanceof VarTerm) {
					   VarTerm v = (VarTerm) nb;
					   nb = v.getValue();
				   }
				   if (nb instanceof NumberTerm) {
					   NumberTerm num = (NumberTerm) nb;
					   Double number = num.solve();
					   int num_as_int = number.intValue();
					   s = ((Integer) num_as_int).toString();
				   }
				   if (nb instanceof StringTermImpl) {
					   StringTermImpl string = (StringTermImpl) nb;
					   s = string.getString();
				   }
				   predname += s;
			   }
			   
			   	double arg_list[] = new double[1];
			   	for (int i=0; i < 1; i++) {
			   		Term arg = predargs.getTerm(i);
			   		arg_list[i]= 0;
//			   		String s = arg.toString();
			   		if (arg instanceof VarTerm) {
			   			VarTerm v = (VarTerm) arg;
			   			arg = v.getValue();
			   		}
			   		if (arg instanceof NumberTerm) {
			   			NumberTerm num = (NumberTerm) arg;
			   			Double number = num.solve();
			   			arg_list[i] = number.intValue();
			   		}
			   		if (arg instanceof StringTermImpl) {
			   			arg_list[i]= 0;
			   		}
			   	}
//			   System.out.println("real call for performing an action for "+ predname);

			   // send the predicate for vehicle to convert it to binary
				for (Vehicle v: vehicles) {
					if (agName.equals("abstraction_agent"+v.getID()))
//						System.out.println("action predicate is "+ predname);
//						System.out.println("argument predicate is "+ arg_list[0]);
					
						v.execute(predname, 1, arg_list);
				}
	   }  else {
     		 u = super.executeAction(agName, act);
    		 printed = true;
    	}
	   
	   if (!printed) {
		   AJPFLogger.info("eass.mas.DefaultEASSEnvironment", agName + " done " + printAction(act));
	   }
	   return u;
	  }
 		
    public void executeSendAction(String agName, SendAction act) {
    	Message m = act.getMessage(agName);
 		String r = m.getReceiver();
 		
        v1.sendMessage(r, m.toString());
        
 //    	super.executeSendAction(agName, act);
    }
	
	
}
