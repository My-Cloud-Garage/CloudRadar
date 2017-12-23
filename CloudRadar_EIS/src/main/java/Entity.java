import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;

import eis.eis2java.annotation.AsAction;
import eis.eis2java.annotation.AsPercept;
import eis.eis2java.translation.Filter;

public class Entity 
{	
	private String body;	
	private List<String> err;
	String region;
	private AmazonEC2 ec2;
	private String RegionResln;
	boolean TestBelief = false;
	GetBeliefPercepts PerceptsAndBeliefs;

	public Entity(String region)
	{
		try{
			this.region = region;
			ec2 = AmazonEC2ClientBuilder.standard().withRegion(region).build();
			PerceptsAndBeliefs = new GetBeliefPercepts(ec2);
			RegionResln = AwsRegion(region);			
		} catch (Exception e) {
			System.out.println( "ERROR: The Agent was unable to initialize the entity!");	
		}
	}
	
	// --Percepts begin here---- //
	@AsPercept(name = "naclPercept", multiplePercepts = true, multipleArguments = true, filter = Filter.Type.ALWAYS)
	public List<List<String>> naclPercept()
	{
		try{
			return PerceptsAndBeliefs.dispatcher("nacl");
		} catch (Exception e) {
			System.out.println("The Enity was unable to send NACL percepts.");
		}
		
		List<List<String>> rtn = new ArrayList<List<String>>();
		
		ArrayList<String> in = new ArrayList<String>();
		// fill the percept with the correct number of atoms
		for(int i = 0 ; i < 8 ; i++)
			in.add("error");
		rtn.add(in);
		
		return rtn;		
	}
	
		
    //*** ERROR PERCEPTS
	@AsPercept(name = "actionError", multiplePercepts = false, multipleArguments = false, filter = Filter.Type.ALWAYS)
	public List<String> actionError()
	{
		return err;
	}
	
	//---Actions begin here---//
	@AsAction(name = "logfileAlert")
	public void logfileAlert(String Traffic, String ID, String SrcDestIP, String ProtocolAction, String FrmPort, String ToPort, String Var, String FlagType)
	{  
		// int Flag = Integer.parseInt(FlagType); 	
	    sendNaclFlag(Traffic, ID, SrcDestIP, ProtocolAction, FrmPort, ToPort, Var);	
	}
	  
	private void sendNaclFlag(String Traffic, String ID, String IP, String RuleAction, String FrmPort, String ToPort, String RuleNum)
	{  	
		String port = "No Port";
		int FROMPort = Integer.parseInt(FrmPort);
		int TOPort = Integer.parseInt(ToPort);
		if (FROMPort == -1)
			port = "and ALL PORTS are open to ";
		if(FROMPort == TOPort && FROMPort != -1)
			port = "and port " + FrmPort + " is open to "; 
		if(FROMPort != TOPort && FROMPort != -1)
			port = "and port range '" + FrmPort + " - " + ToPort + "' is open to "; 
		
		String alert = "New Network ACL ID = " + ID + ". Region = "+ RegionResln + ". Traffic = "+ Traffic + 
				             ". Rule Number = "+ RuleNum + ". Rule Action = " + RuleAction + ", "+ port + IP + ".";  
		
		System.out.println("");
		System.out.println(alert);
		System.out.println("");
	}	
	
	
	private String AwsRegion(String awsRegion)
	{
		switch (awsRegion)
		{
		     case "us-west-2":
		        return "US West (Oregon)";		   
		     case "us-west-1":
		        return "US West (N. California)";
		     case "us-east-2":
		    	 return "US East (Ohio)";
		     case "us-east-1":
		    	 return "US East (N. Virginia)";
		     case "ap-south-1":
		    	 return "Asia Pacific (Mumbai)";
		     case "ap-northeast-2":
		    	 return "Asia Pacific (Seoul)";
		     case "ap-southeast-1":
		    	 return "Asia Pacific (Singapore)";
		     case "ap-southeast-2":
		    	 return "Asia Pacific (Sydney)";
		     case "ap-northeast-1":
		    	 return "Asia Pacific (Tokyo)";
		     case "ca-central-1":
		    	 return "Canada (Central)";
		     case "cn-north-1":
		    	 return "China (Beijing)";
		     case "eu-central-1":
		    	 return "EU (Frankfurt)";
		     case "eu-west-1":
		    	 return "EU (Ireland)";
		     case "eu-west-2":
		    	 return "EU (London)";
		     case "sa-east-1":
		    	 return "South America (São Paulo)";
		     case "us-gov-west-1":
		    	 return "AWS GovCloud (US)";
		    	 
		     default:
		       return "[Unknown Region!]";
		}
	}
}