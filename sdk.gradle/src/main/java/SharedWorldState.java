
/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.FCDataInputStream;
import com.swirlds.platform.FCDataOutputStream;
import com.swirlds.platform.FastCopyable;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.swirlds.platform.Utilities;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class SharedWorldState implements SwirldState {
	/**
	 * The shared state is just a list of the strings in all transactions, listed in the order received
	 * here, which will eventually be the consensus order of the community.
	 */
	//serialized String's:
	private List<String> protobufs = new ArrayList<>();

	/** smart contract can execute for max 10 seconds*/
	private final long walltime = 10000L;

	/** names and addresses of all members */
	private AddressBook addressBook;

	/** @return all the protobufs received so far from the network */
	public synchronized List<String> getAllReceived() {
		return protobufs;
	}
	public synchronized String getAllReceived_message() {
		String returnStr="";

		Iterator itr = protobufs.iterator();

		while(itr.hasNext()){
			String _str= (String)itr.next();
			byte[] _bytes = _str.getBytes(StandardCharsets.UTF_8);
			try {

				Hashgraph.Tx _protobuf = Hashgraph.Tx.parseFrom(_bytes);
				returnStr += _protobuf.getMessage()+",";

			}catch(InvalidProtocolBufferException e){
				System.out.println("InvalidProtocolBufferException");
				System.out.println(e.getMessage());
			}
		}
		return returnStr;
	}


	/** @return all the strings received so far from the network */
	/*
	public synchronized List<Hashgraph.Tx> getStrings() {
		return strings;
	}
	*/

	/** @return all the strings received so far from the network, concatenated into one */
	/*
	public synchronized String getAllReceived() {
		return strings.toString();
	}
	*/

	/** @return all the strings received so far from the network, concatenated into one */
	/*
	public synchronized String getReceived() {
		String returnStr="";
		if(strings.size()>0){
			returnStr=strings.get(strings.size()-1);
		}
		return returnStr;
	}
	*/

	/** @return the same as getReceived, so it returns the entire shared state as a single string */
	/*
	public synchronized String toString() {
		return strings.toString();
	}
	*/

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		SharedWorldState copy = new SharedWorldState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public synchronized void copyTo(FCDataOutputStream outStream) {
		try {
			//Utilities.writeStringArray(outStream,strings.toArray(new String[0]));
			Utilities.writeStringArray(outStream,protobufs.toArray(new String[0]));
//			byte[] x = ArrayUtils.toPrimitive(protobufs.toArray(new Byte[0]));
//			Utilities.writeByteArray(outStream,x);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(FCDataInputStream inStream) {
		try {
			//strings = new ArrayList<String>(Arrays.asList(Utilities.readStringArray(inStream)));
			protobufs = new ArrayList<String>(Arrays.asList(Utilities.readStringArray(inStream)));
//			Byte[] x= ArrayUtils.toObject(Utilities.readByteArray(inStream));
//			protobufs = new ArrayList<Hashgraph.Tx>(Arrays.asList(x));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		//strings = new ArrayList<String>(((SharedWorldState) old).strings);
		protobufs = new ArrayList<String>(((SharedWorldState) old).protobufs);
		addressBook = ((SharedWorldState) old).addressBook.copy();
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus, Instant timestamp, byte[] transaction, Address address) {
		//strings.add(new String(transaction, StandardCharsets.UTF_8));
		protobufs.add(new String(transaction, StandardCharsets.UTF_8));

		if(consensus==true){
			if(shouldRunSmartContract(transaction)){
				runSmartContract(transaction);
			}
		}
	}

	@Override
	public void noMoreTransactions() {
	}

	@Override
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.addressBook = addressBook;
	}






























	/*
	 * not every state update causes a smart contract execution
	 *
	 */
	private boolean shouldRunSmartContract(byte[] transaction){

//		//xTODO - for now, the protocol is a very dumb keyword protocol
//		String _str=new String(transaction, StandardCharsets.UTF_8);
//		//System.out.println(_str.trim());

		try {
			Hashgraph.Tx _protobuf = Hashgraph.Tx.parseFrom(transaction);

			if(_protobuf.getType()==1){   //yes, a smart contract :)
				System.out.println("Now execute smart contract!");
				return true;
			}
		}catch(InvalidProtocolBufferException e){
			System.out.println("InvalidProtocolBufferException");
			System.out.println(e.getMessage());
		}

//		if(_str.trim().endsWith("mercury")==true){
//			System.out.println("Now execute smart contract!");
//			return true;
//		}
		return false;
	}

	/*
	 * `docker run -it --rm alpine /bin/ash`
	 * max walltime of ~10 seconds
	 */
	private void runSmartContract(byte[] transaction){

		//xTODO - for now, the protocol is a very dumb keyword protocol
		//String _str=new String(transaction, StandardCharsets.UTF_8);

		String smartContractUri=null;
		try {
			Hashgraph.Tx _protobuf = Hashgraph.Tx.parseFrom(transaction);
			smartContractUri=_protobuf.getSmartContractUri();

		}catch(InvalidProtocolBufferException e){
			System.out.println("InvalidProtocolBufferException");
			System.out.println(e.getMessage());
		}


		//and run smart contract...
		SmartContractRunnable telnetRunnable = new SmartContractRunnable(smartContractUri);
		new Thread(telnetRunnable).start();

	}



















	public class SmartContractRunnable implements Runnable{

		private volatile String _smartContractUri;

		public SmartContractRunnable(String _smartContractUri){
			this._smartContractUri=_smartContractUri;
		}
		@Override
		public void run() {

			//this could be gvisor or some other more secure execution environment
			//SMART_CONTRACT_URI is passed as ENV
			String _cmd = "docker run -t -d -e SMART_CONTRACT_URI=" + _smartContractUri + " alpine";   //run Alpine Linux in background. Returns an ID once successfully called

			try {
				Runtime rt = Runtime.getRuntime();
				Process p = rt.exec(_cmd);
				p.waitFor();
				System.out.println("Process exited with code = " + p.exitValue());

				//Get process's output:
				InputStream is = p.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s;
				String localContainerID = "";
				while ((s = br.readLine()) != null) {
					localContainerID = s;
					//System.out.println(s);
				}
				is.close();
				System.out.println("Started local container with ID: " + localContainerID);


				//checks:
				if (localContainerID == null) {
					System.out.println("ERROR: Problem starting the container! Is Docker running?");
					return;
				}
				if (localContainerID.compareTo("null") == 0) {
					System.out.println("ERROR: Problem starting the container! Is Docker running?");
					return;
				}
				if (localContainerID.length() < 10) {
					System.out.println("ERROR: Problem starting the container! Is Docker running?");
					return;
				}


				//passed checks
				//shutdown after 10 seconds...
				new Timer().schedule(
						new TimerTaskEOH(localContainerID) {
							@Override
							public void run() {
								System.out.println("Stopping localContainerID: " + this.localContainerID);

								//fire and forget:
								try {
									String _cmd = "docker rm -f " + this.localContainerID;
									Runtime rt = Runtime.getRuntime();
									Process p = rt.exec(_cmd);
									p.waitFor();
									System.out.println("`docker rm -f ...` request exited with code = " + p.exitValue());

								} catch (IOException e) {
									e.printStackTrace();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						},
						walltime
				);


			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	private class TimerTaskEOH extends TimerTask {
		public final String localContainerID;

		TimerTaskEOH(String _localContainerID){
			this.localContainerID=_localContainerID;
		}
		@Override
		public void run() {

		}
	}
}