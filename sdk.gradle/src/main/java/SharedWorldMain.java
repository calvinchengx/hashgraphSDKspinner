
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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.swirlds.platform.Browser;
import com.swirlds.platform.Console;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldMain;
import com.swirlds.platform.SwirldState;





/**
 * This HelloSwirld creates a single transaction, consisting of the string "Hello Swirld", and then goes
 * into a busy loop (checking once a second) to see when the state gets the transaction. When it does, it
 * prints it, too.
 */
public class SharedWorldMain implements SwirldMain {


	/** the platform running this app */
	public Platform platform;
	/** ID number for this member */
	public long selfId;
	/** a console window for text output */
	public Console console;
	/** sleep this many milliseconds after each sync */
	public final int sleepPeriod = 100;

	/** telnet port connections are available at this address:**/
	private int telnetPortOffset = -1;   //Integer.valueOf(System.getenv("PORT_TELNET"));


	/**
	 * Constructor
	 */
	public SharedWorldMain(){
		//check environment variables are set correctly...
		try{
			telnetPortOffset = Integer.valueOf(System.getenv("TELNET_PORT_OFFSET"));
		}catch(Exception e){
			System.err.println("");
			System.err.println("ERROR: environment variables not set!");
			System.err.println("Try running: `export $(grep -v '^#' ./.env | xargs)`");
			System.exit(1);
		}
	}

	/**
	 * This is just for debugging: it allows the app to run in Eclipse. If the config.txt exists and lists a
	 * particular SwirldMain class as the one to run, then it can run in Eclipse (with the green triangle
	 * icon).
	 *
	 * @param args
	 *            these are not used
	 */
	public static void main(String[] args) {

		Browser.main(args);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public void preEvent() {
	}

	@Override
	public void init(Platform platform, long id) {
		this.platform = platform;
		this.selfId = id;
		//only show if GUI is ON:
		if(!System.getenv("GUI_ONOFF").toLowerCase().equals("off")){
			this.console = platform.createConsole(true); // create the window, make it visible
		}

		/////platform.setAbout("Hello Swirld v. 1.0\n"); // set the browser's "about" box
		platform.setSleepAfterSync(sleepPeriod);
		platform.getParameters();

	}

	@Override
	public void run() {



		String myName = platform.getState().getAddressBookCopy().getAddress(selfId).getSelfName();
		InetAddress ipv4 = null;
		int port_ipv4 = 0;



		try{
			ipv4=InetAddress.getByAddress(platform.getState().getAddressBookCopy().getAddress(selfId).getAddressExternalIpv4());   //.getHostAddress();
		}catch(UnknownHostException e){
			System.err.println("ERROR: UnknownHostException!");
			System.err.println(e.toString());
		}
		port_ipv4 = platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4();





		//only show if GUI is ON:
		if(!System.getenv("GUI_ONOFF").toLowerCase().equals("off")) {
			console.out.println("Hello Swirld from " + myName + " (" + ipv4.getHostAddress() + ":" + String.valueOf(port_ipv4) + ")");
		}



		//let's construct the protobuf:
		Hashgraph.Tx tx = Hashgraph.Tx.newBuilder()
				.setType(0)
				.setMessage(myName)
				.build();


		// create a transaction. For this example app,
		// we will define each transactions to simply
		// be a string in UTF-8 encoding.
		//byte[] transaction = myName.getBytes(StandardCharsets.UTF_8);
		byte[] transaction = tx.toByteArray();

		// Send the transaction to the Platform, which will then
		// forward it to the State object.
		// The Platform will also send the transaction to
		// all the other members of the community during syncs with them.
		// The community as a whole will decide the order of the transactions
		platform.createTransaction(transaction);



		String lastAllReceived = "";







		/////
		//stdin - raw stdin connections
		/////
		if(!System.getenv("GUI_ONOFF").toLowerCase().equals("off")) {
			this.console.addKeyListener(new KeyListener() {
				private String _buffer = "";

				@Override
				public void keyTyped(KeyEvent e) {

				}

				@Override
				public void keyPressed(KeyEvent e) {
					this._buffer += e.getKeyChar();

					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						this._buffer = this._buffer.trim();
						//only show if GUI is ON:
						if (!System.getenv("GUI_ONOFF").toLowerCase().equals("off")) {
							console.out.println("Writing (stdin): " + this._buffer);
						}
						//this.eoh_write();

						//and write the buffer:
						//first let's construct the protobuf:
						Hashgraph.Tx tx = Hashgraph.Tx.newBuilder()
								.setType(0)
								.setMessage(this._buffer)
								.build();
						platform.createTransaction(tx.toByteArray());

						//and clear the buffer:
						this._buffer = "";
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {

				}

				//			private void eoh_write(){
				////				byte[] transaction = this._buffer.getBytes(StandardCharsets.UTF_8);
				////				platform.createTransaction(transaction);
				//
				//
				//			}

			});
		}



		/////
		//network in - raw telnet connections (separate Thread)
		/////
		TelnetRunnable telnetRunnable = new TelnetRunnable(ipv4,port_ipv4+telnetPortOffset);
		new Thread(telnetRunnable).start();




		/////
		//And listen for consensus updates - removed infinite loop
		/////
		ConsensusRunnable consensusRunnable = new ConsensusRunnable(lastAllReceived);
		new Thread(consensusRunnable).start();




		/////
		//...More...
		/////


	}

	@Override
	public SwirldState newState() {
		return new SharedWorldState();
	}









	public class ConsensusRunnable implements Runnable {

		public volatile String _lastAllReceived;


		ConsensusRunnable(String _lastAllReceived){
			this._lastAllReceived=_lastAllReceived;
		}

		@Override
		public void run() {


			while (true) {
				SharedWorldState state = (SharedWorldState) platform.getState();
				//String allReceived = state.getAllReceived();
				//String received = state.getReceived();
				String allReceived = state.getAllReceived_message();



				if (!_lastAllReceived.equals(allReceived)) {
					_lastAllReceived = allReceived;
					//only show if GUI is ON:
					if (!System.getenv("GUI_ONOFF").toLowerCase().equals("off")) {
						console.out.println("Received: " + allReceived); // print all received transaction
					}
				}
				try {
					Thread.sleep(sleepPeriod);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}


		}
	}
	public class TelnetRunnable implements Runnable {

		public volatile InetAddress _ipv4;
		public volatile int _port_ipv4;

		public TelnetRunnable(InetAddress ipv4,int port_ipv4){
			this._ipv4 = ipv4;
			this._port_ipv4 = port_ipv4;
		}

		public void run(){

			PrintWriter out = null;
			BufferedReader in = null;

			System.out.println("Remote socket listening on: "+_ipv4.getHostAddress()+":"+String.valueOf(_port_ipv4));

			ServerSocket listener = null;
			try {
				listener = new ServerSocket(_port_ipv4,0,_ipv4);
				while (true) {
					Socket socket = listener.accept();
					//try {
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String payload=in.readLine();
					//only show if GUI is ON:
						if(!System.getenv("GUI_ONOFF").toLowerCase().equals("off")) {
							SharedWorldMain.this.console.out.println("Writing (telnet): " + payload);
						}
//						out = new PrintWriter(socket.getOutputStream(), true);
//						out.println(new Date().toString());

						//and write the buffer:
						//first let's construct the protobuf:
						Hashgraph.Tx tx = Hashgraph.Tx.newBuilder()
								.setType(0)
								.setMessage(payload)
								.build();
						SharedWorldMain.this.platform.createTransaction(tx.toByteArray());

						in.close();

					//} finally {
						//socket.close();
//						in.close();
//						out.close();
					//}
				}
			}catch (IOException e){
				System.out.println(e.getMessage());
			}


//			try{
//				socket = new ServerSocket(_port_ipv4,0,_ipv4);
//				out = new PrintWriter(socket.getOutputStream(), true);
//				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//				out.println("ping");
//				//System.out.println(in.readLine());
//				SharedWorldMain.this.console.out.println(in.readLine());
//				out.close();
//				in.close();
//				socket.close();
//			} catch (IOException e) {
//				System.out.println(e.getMessage());
//				return;
//			}
		}

	}
}
