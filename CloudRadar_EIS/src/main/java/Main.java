import java.util.ArrayList;
import java.util.Map;

// EIS imports
import eis.eis2java.environment.AbstractEnvironment;
import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Parameter;

public class Main extends AbstractEnvironment
{	
	private Entity ControlableEntity;
	public static void main(String[] args){}
	private ArrayList<String> Actions = new ArrayList<String>();
		
	// required methods
	public void init(Map<String, Parameter> param) throws ManagementException 
	{              
		// Prepare GOAL
		reset(param);
		Actions.add("logfileAlert");
		Actions.add("emailAlert");

		try {
			   ControlableEntity = new Entity(parseRegion(param.get("region")));
			   registerEntity("entity", ControlableEntity);		
		} 
		catch (EntityException e) 
		{
			throw new ManagementException("Failed to create entities/entity!", e);
		}
	}

	public void reset(Map<String, Parameter> param) throws ManagementException {
		setState(EnvironmentState.PAUSED);
	}

	@Override
	public void kill() throws ManagementException {
		setState(EnvironmentState.KILLED);
	}

	@Override
	protected boolean isSupportedByEnvironment(Action action) {		
		if(!Actions.contains(action.getName()))
		{		
			String body = "The action " + action.getName()
			+ " is not defined in Entity";
			System.out.println(body);
		}
		return true;
	}
	
	@Override
	protected boolean isSupportedByType(Action action, String type) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private String parseRegion(Parameter region){
		return region.toString().split("\"")[1];
	}
}