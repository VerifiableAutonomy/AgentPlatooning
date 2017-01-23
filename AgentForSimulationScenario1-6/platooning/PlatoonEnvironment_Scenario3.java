package eass.platooning;

import java.util.Random;

import eass.mas.DefaultEASSEnvironment;
import ail.mas.ActionScheduler;
import ail.mas.NActionScheduler;
import ail.syntax.Literal;
import ail.syntax.NumberTermImpl;
import ajpf.util.AJPFLogger;


public class PlatoonEnvironment_Scenario3 extends DefaultEASSEnvironment {
	String logname = "eass.platooning.PlatoonEnvironment";

	
//	VerifyMap<String, Vehicle> vehicles = new VerifyMap<String, Vehicle>();
	
//	Vehicle v = new Vehicle(2);
	int Y = 1;
	int precedingPID=2;
	int egoPID=0;
	int egoID=3;
	boolean agreement=false;
	
	Random r = new Random();

//	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	
	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment_Scenario3() {
		super();
		NActionScheduler s = new NActionScheduler(100);
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
		// If not connected to matlab make the scheduler more verification friendly.
			NActionScheduler s = (NActionScheduler) getScheduler();
			removePerceptListener(s);
			ActionScheduler s1 = new ActionScheduler();
			s1.addJobber(this);
			setScheduler(s1);
			addPerceptListener(s1);
			
//			Literal latency = new Literal("latency");
////			precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
//			latency.addTerm(new NumberTermImpl(0));
//			addPercept("abstraction_follower1",latency);
			
	}

	public void eachrun() {

		if (Y == 10){
			Y=1;
	//		agreement= r.nextBoolean();
	//		egoPID = r.nextInt(2);
		}
		
/*		for (Vehicle v: vehicles) {
			if(v.getEgoID()== 3){
				Literal precedingPlatoonID = new Literal("precedingPID");
				precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
				addUniquePercept("precedingPID",precedingPlatoonID);
				
				Literal egoPlatoonID = new Literal("egoPID");
				egoPlatoonID.addTerm(new NumberTermImpl(v.getEgoPID()));
				addUniquePercept("egoPID",egoPlatoonID);
				
			}
		}
*/
		precedingPID = r.nextInt(4);
	//	System.out.println("precedingPID"+ precedingPID);
		Literal precedingPlatoonID = new Literal("precedingPID");
//		precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
		precedingPlatoonID.addTerm(new NumberTermImpl(precedingPID));
		addUniquePercept("abstraction_follower1",precedingPlatoonID);

		
		precedingPID = r.nextInt(4);
		Literal precedingPlatoonID2 = new Literal("precedingPID");
//		precedingPlatoonID.addTerm(new NumberTermImpl(v.getPrecedingPID()));
		precedingPlatoonID2.addTerm(new NumberTermImpl(precedingPID));
		addUniquePercept("abstraction_follower2",precedingPlatoonID2);
		
		Y++;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#printvalues(ail.syntax.Literal)
	 */
	public void printvalues(Literal pred) {
		if (pred.getFunctor().equals("precedingPID")) {
			AJPFLogger.fine("eass.platooning", pred.toString());
		} 

	}

	public boolean done() {
		   return false;
		}
	
//	public void noconnection_run(String agname, Action act) {
//		if (act.getFunctor().equals("run")) {
//			   Predicate predlist = (Predicate) act.getTerm(0);
//			   Predicate predargs = (Predicate) act.getTerm(1);
//			   int num_name_comps = predlist.getTermsSize();
//			   String predname = "";
//			   for (int i=0; i<num_name_comps; i++) {
//				   // Lots of extra work to get the string correct.
//				   Term nb = predlist.getTerm(i);
//				   String s = nb.toString();
//				   if (nb instanceof VarTerm) {
//					   VarTerm v = (VarTerm) nb;
//					   nb = v.getValue();
//				   }
//				   if (nb instanceof NumberTerm) {
//					   NumberTerm num = (NumberTerm) nb;
//					   Double number = num.solve();
//					   int num_as_int = number.intValue();
//					   s = ((Integer) num_as_int).toString();
//				   }
//				   if (nb instanceof StringTermImpl) {
//					   StringTermImpl string = (StringTermImpl) nb;
//					   s = string.getString();
//				   }
//				   predname += s;
//			   }
//			   
//			   Term arg = predargs.getTerm(0);
////			   String s = arg.toString();
//			   
////			   if(predname.equals("request_to_join")) {
////				    agreement= true;	
////					Literal req_join = new Literal("request_to_join");
//////					egoPlatoonId.addTerm(new NumberTermImpl(v.getegoPID());
////					addPercept("abstraction_follower",req_join);
////
////			   }
//			
//			   
//			}
//			System.err.println("No Matlab Connection: " + act.toString());		
//		}
	


}

