package eass.verification.platoon.modelExtractionForVerification6Jan17;

import ail.mas.scheduling.ActionScheduler;
import ail.syntax.Action;
import ail.syntax.Literal;
import ail.syntax.SendAction;
import ail.syntax.Unifier;
import ail.util.AILexception;
import ajpf.MCAPLcontroller;
import eass.mas.DefaultEASSEnvironment;


//import eass.mas.matlab.EASSMatLabEnvironment;


public class PlatoonEnvironment extends DefaultEASSEnvironment{
	String logname = "eass.verification.platoon.modelExtraction.PlatoonEnvironment";

	/**
	 * Constructor.
	 *
	 */
	public PlatoonEnvironment() {
		super();
		/*NActionScheduler should not be used for verification purpose----- it is the reason that 
		* I got weird transitions
		* NActionScheduler s = new NActionScheduler(20);
		* s.addJobber(this);
		*/
		ActionScheduler s = new ActionScheduler();
		setScheduler(s);
		addPerceptListener(s);
	}
		
	
	/*
	 * (non-Javadoc)
	 * @see eass.mas.DefaultEASSEnvironment#initialise()
	 */
	public void initialise() {
		super.initialise();
						
	}

/* for verification purpose when environment does not provide anything should not 
 * be used	
 * public void eachrun() {
 *		super.eachrun();
 *				
 *	} 

	
 *	public boolean done() {
 *		   return false;
 *		}
*/
	public Unifier executeAction(String agName, Action act) throws AILexception {

		super.executeAction(agName, act);
		
		if (act instanceof SendAction) {
			MCAPLcontroller.force_transition();
		}
		
		
		Unifier u = new Unifier();
	   return u;
	  }	  		
}