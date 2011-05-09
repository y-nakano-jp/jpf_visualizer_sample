import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.jvm.JVM;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.lang.SecurityException;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import java.io.UnsupportedEncodingException;
public class SampleListener implements SearchListener{
	private Socket sock = null;
	private PrintWriter print_writer = null;

	public SampleListener(){
		try{
			sock = new Socket("127.0.0.1",12345);
		}catch(SocketException e){
			System.err.println("socket exception");
			sock = null;
		}catch(SecurityException e){
			System.err.println("security exception");
			sock = null;
		}catch(UnknownHostException e){
			System.err.println("unknown host");
			sock = null;
		}catch(IOException e){
			sock = null;
		}

		if(sock != null){
			try{
				print_writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF8"), true);
			}catch(UnsupportedEncodingException e){
				print_writer = null;
			}catch(IOException e){
				print_writer = null;
			}

		}
	}
	/**
	* got the next state
	* Note - this will be notified before any potential propertyViolated, in which
	* case the currentError will be already set
	*/
	public void stateAdvanced (Search search){
			
		if(print_writer != null){
			JVM vm = search.getVM();
			//print_writer.print(">>"+search.getVM().getStateId()+" "+search.getVM().getThreadNumber() + " " + vm.getAliveThreadCount()+"\0");
			//print_writer.print(""+search.getDepth()+"\0");
			print_writer.print("{" + 
					"\"e\": \"a\"" +
					",\"id\": " + vm.getStateId() +
					",\"d\": "+ search.getDepth() +
					",\"tn\": "+ vm.getThreadNumber()+
					",\"an\": " + vm.getAliveThreadCount()+
					"}" + "\0"); 
			print_writer.flush();
		}
	}

	/**
	* state is fully explored
	*/
	public void stateProcessed (Search search){
	}

	/**
	* state was backtracked one step
	*/
	public void stateBacktracked (Search search){
		if(print_writer != null){
			
			JVM vm = search.getVM();
			//print_writer.print("<<"+search.getVM().getStateId()+"\0");
			print_writer.print("{" + 
					"\"e\": \"b\"" +
					",\"id\": " + vm.getStateId() +
					",\"d\": "+ search.getDepth() +
					",\"tn\": "+ vm.getThreadNumber()+
					",\"an\": " + vm.getAliveThreadCount()+
					"}" + "\0"); 
			/*print_writer.print("{" + 
					"\"Event\": \"backtrack\"" 	
					",\"StateId\": " + vm.getStateId() +
					//",\"Depth\": "+ search.getDepth() +
					//",\"ThreadNumber\": "+ vm.getThreadNumber()+
					//",\"AliveThreadCount\": " + vm.getAliveThreadCount()+
					"}"); */
			//search.getVM().getThreadNumber() + " " + vm.getAliveThreadCount()					
			//print_writer.print(""+search.getDepth()+"\0");
			print_writer.flush();
		}
	}

	/**
	* some state is not going to appear in any path anymore
	*/
	public void statePurged (Search search){
	}

	/**
	* somebody stored the state
	*/
	public void stateStored (Search search){
	}

	/**
	* a previously generated state was restored
	* (can be on a completely different path)
	*/
	public void stateRestored (Search search){
	}

	/**
	* JPF encountered a property violation.
	* Note - this is always preceeded by a stateAdvanced
	*/
	public void propertyViolated (Search search){
	}

	/**
	* we get this after we enter the search loop, but BEFORE the first forward
	*/

  	public void searchStarted (Search search){
		/*System.out.println("*****************************Hello JPF**************************");
		File f = new File("search_started.txt");
		try{
			f.createNewFile();
		}catch(IOException e){
		}*/
		if(print_writer != null){
			print_writer.print("search start"+"\0");
			print_writer.flush();
		}
	}
	/**
	* there was some contraint hit in the search, we back out
	* could have been turned into a property, but usually is an attribute of
	* the search, not the application
	*/
	public void searchConstraintHit (Search search){
	}

	/**
	* we're done, either with or without a preceeding error
	*/
	public void searchFinished(Search search){
		if(print_writer != null){
			print_writer.print("search finished"+"\0");
			print_writer.print("end"+"\0");
			print_writer.flush();
			print_writer.close();
			//sock.close();
		}
	}
};
