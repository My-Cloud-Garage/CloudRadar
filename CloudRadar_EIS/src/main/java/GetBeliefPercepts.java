import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.DescribeNetworkAclsResult;
import com.amazonaws.services.ec2.model.NetworkAclEntry;

public class GetBeliefPercepts {
	boolean BeliefFileExist;
	boolean naclBeliefExist;
	AmazonEC2 ec2;
		
    public GetBeliefPercepts(AmazonEC2 Builder) 
    {
    	this.ec2 = Builder;
    	this.BeliefFileExist = false; 
    	this.naclBeliefExist = false;
    	
        File BeliefFile = new File("beliefConf.pl"); 
        if (BeliefFile.exists())
        {
            if(BeliefFile.length() > 5)
            		BeliefFileExist = true;
            else {
            	System.out.println("Belief file 'beliefConf.pl' will be created");
            }
        } 	
    }
    
    public List<List<String>> dispatcher(String Percepts)
    {
    	if(Percepts == "nacl")
    		return getNacl();
    	System.out.println("Error! Dispatcher doesn't recognize " + Percepts + ". Dispatcher can only call getNacl() method.");
    	return null;
    }
	public List<List<String>> getNacl() 
	{
		boolean CreateBeliefSet = false;		
        DescribeNetworkAclsResult All_NACLS = ec2.describeNetworkAcls();		
		ArrayList<List<String>> NACL_List = new ArrayList<List<String>>();
		
		for(NetworkAcl AclEntries : All_NACLS.getNetworkAcls())
		{
			for (NetworkAclEntry EntryByEntry : AclEntries.getEntries())
			{
				ArrayList<String> ListOfEntries = new ArrayList<String>();						
				ListOfEntries.add(AclEntries.getNetworkAclId().toString());
				ListOfEntries.add(EntryByEntry.getCidrBlock().toString());
				
				if(EntryByEntry.getEgress() == true)
					ListOfEntries.add("egress");
				else
					ListOfEntries.add("ingress");
				
				ListOfEntries.add(EntryByEntry.getRuleAction());
				ListOfEntries.add(EntryByEntry.getRuleNumber().toString());

				String PortRange = PortRangeCorrection(EntryByEntry.toString());
				if(PortRange == "")
				{
					ListOfEntries.add("-1");
					ListOfEntries.add("-1");
				}
				else{
					PortRange = PortRange.replace("From: ", "");
					PortRange = PortRange.replace("To: ", "");
					String Temp[] = PortRange.split(",");
					ListOfEntries.add(Temp[0]);	
					ListOfEntries.add(Temp[1]);	
				}									
				
				if (BeliefFileExist == false && naclBeliefExist == false) 
				{					
					CreateBelief(ListOfEntries);
					ListOfEntries.add("generated");
					CreateBeliefSet = true;
				}
				else {
					  ListOfEntries.add("nongenerated");
				}
				NACL_List.add(ListOfEntries); 
			}	
		}
		if(CreateBeliefSet) {
			naclBeliefExist = true;
			CreateBeliefSet = false;
		}
		return NACL_List;	
	}
	
	private String PortRangeCorrection(String port)
	{
		int count = port.length();
		String ret = "";
		for(int x = 0; x < count; x++){
			if((port.charAt(x) == 'F') && (port.charAt(x+1) == 'r')){
				 for(int y = x; y < count; y++){
				     if(port.charAt(y) == '}')
					     break;
				     ret = ret + port.charAt(y);
				 }				
			}			
		}
		return ret;
	}

	public void CreateBelief(ArrayList<String> NACL_Entry) {
	     try {	    	   
	            PrintStream myFile = new PrintStream(new FileOutputStream("beliefConf.pl", true));         
	            try {
	            	    String Entry = "";
	            	    int count = 0;
	            	    for (String param : NACL_Entry)
	            	    {
	            	    	if(count < 6)
	            	    		Entry = Entry + "'" + param + "',";
	            	    	else 
	            	    		Entry = Entry + "'" + param + "'"; 	
	            	    	count++;
	            	    }
	            	    String prologString = "initialNacl("+ Entry + ").";	            	    
	            	    myFile.println(prologString);
	              } 
	            finally {
	               myFile.close();
	            }
	         } 
	         catch(IOException ex) {
	            ex.printStackTrace();
	         }
	  }
}