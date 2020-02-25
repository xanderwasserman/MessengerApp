import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class D1Messenger extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ServerSocket MyServerSocket;
	Socket s;
	JTextField text = new JTextField(); // GUI for typing a message
	JButton send = new JButton(); // GUI for the send button
	JTextArea messages = new JTextArea(); // GUI for previous messages
	PrintWriter PrintWriterOut;
	BufferedReader br;
	ActionListener al; // listens for the "send" button being pressed
	String ipstring; // ip address
	boolean r2s = false; // ready to send flag
	D1Messenger point;
	boolean HostClient; // acting as a Host or a Client
	String ClientServer; // Message sent from client or server?
	
	
	
	public String retreiveXMLElement(String searchTerm) {
		
		String deals = null;
		
		try {
			
			File fXMLfile = new File("C:\\Users\\xande\\Google Drive\\Varsity\\Masters\\Reconfigurable control\\TaskD1\\SpecialsXML.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXMLfile);
			
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getChildNodes();
			
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				
			if (node.getNodeType() == Node.ELEMENT_NODE) {	
				
				Element eElement = (Element) node;
				
				if(searchTerm.equals(node.getNodeName())) {
					
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer transformer;
					transformer = tf.newTransformer();
					StringWriter writer = new StringWriter();
					transformer.transform(new DOMSource(node), new StreamResult(writer));
					String XMLstring = writer.getBuffer().toString();
					
					deals = XMLstring;
				}
			}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deals;
		
	}
	

	public D1Messenger(boolean hostOrConnect, String ip) {

		ipstring = ip;

		// default layout for the GUI window
		setLayout(null);
		setSize(500, 1000);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// text box
		add(text);
		text.setLocation(5, 940);
		text.setSize(getWidth() - 40 - 60, 30);
		text.setEnabled(false);

		// send button
		add(send);
		send.setLocation(10 + text.getWidth(), text.getY());
		send.setText("Send");
		send.setSize(60, 30);

		// messages window
		add(messages);
		messages.setEditable(false);
		messages.setBorder(new EtchedBorder());
		messages.setLocation(15, 5);
		messages.setSize(460, 920);


		al = new ActionListener() { // creates an action listener to listen for "send" button being pressed
			public void actionPerformed(ActionEvent e) {
				r2s = true; // sets flag ready to send as true
			}
		};

		send.addActionListener(al); // adds the action listener to the button
		point = this; // refers to this specific instance of the D1Messenger object

		// sets the correct prefix for the messages based on whether it is the host or
		// client (This is opposite, ie Host must say Client etc
		HostClient = hostOrConnect;
		if (HostClient)
			ClientServer = "\n[Client]: ";
		else
			ClientServer = "\n[Server]: ";

		Messenger.start(); // calls the messenger thread to start in the background

	}

	public static void main(String args[]) {

		Object[] options1 = { "Server", "Client" };
		int result = JOptionPane.showOptionDialog(null, "Choose a function", "Choose a function", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);

		if (result == 0) {
			new D1Messenger(true, null).setVisible(true); // makes the GUI window visible along with creating a new host instance of D1Messenger
		} else {
			JFrame frame = new JFrame("Host IP");
			String ipstring = JOptionPane.showInputDialog(frame, "Enter the Host IP Address:", "Host IP", JOptionPane.WARNING_MESSAGE); // asks for the host IP address to
			
			try {
				InetAddress.getByName(ipstring);
				new D1Messenger(false, ipstring).setVisible(true); // makes the GUI window visible along with creating a new client instance of D1Messenger
			} catch (Exception e) { // catch the connection exception
				JOptionPane.showMessageDialog(null, "Invalid or Unreachable IP");
			}
		}

	}

	Thread Messenger = new Thread() { // creates a new thread to run in the background to keep the connection alive
		public void run() { // runs the thread
			try {

				if (HostClient) { // if it is the host
					messages.setText("HOST APPLICATION\n");
					messages.setText(messages.getText() + "Awaitng Client Connection.\nEnter this IP address at client.\nIP: " + InetAddress.getLocalHost().getHostAddress());
					MyServerSocket = new ServerSocket(9999); // creates the socket for TCP/IP connection
					s = MyServerSocket.accept(); // waits for a connection to be made over TCP/IP
					s.setKeepAlive(true); // keeps the socket connection alive
				} else { // if it is the client
					messages.setText("CLIENT APPLICATION\n");
					messages.setText(messages.getText() + "Connecting to:" + ipstring + ":9999");
					s = new Socket(InetAddress.getByName(ipstring), 9999); // connects to the specified host
				}
				text.setEnabled(true);
				PrintWriterOut = new PrintWriter(s.getOutputStream(), true); // printwriter instance that sends on the
																				// socket
				br = new BufferedReader(new InputStreamReader(s.getInputStream())); // creates a buffer for receiving
																					// the incoming messages from the
																					// socket
				messages.setText(messages.getText() + "\nConnected to:" + s.getInetAddress().getHostAddress() + ":"	+ s.getPort());
				while (true) {
					if (r2s == true) { // if message is ready to send
						PrintWriterOut.println(text.getText()); // send the message on the socket
						messages.setText(messages.getText() + "\nMe: " + text.getText());
						text.setText("");
						r2s = false;
					}
					if (br.ready()) { // if message is waiting to be received
						String message = br.readLine();
						messages.setText(messages.getText() +  ClientServer  + message);

						try {
							FileWriter writer = new FileWriter("MessageLog.txt", true); // creates a FileWriter instance
							writer.write(java.time.LocalDateTime.now() + ":" + ClientServer  + message); // print the message along with who sent it and the time it was sent
							writer.write("\r\n"); // write new line
							writer.close();
						} catch (IOException e) { // catches the exception from trying to write to a file
							e.printStackTrace();
						}
						
						if (HostClient) {
							
							String XMLstring = retreiveXMLElement(message);
							PrintWriterOut.println(XMLstring); // send the message on the socket

					}
					Thread.sleep(80); // sleeps the message receiving thread for 80ms
				}
				}
			} catch (Exception ex) { // catches the exception of trying to run the thread that sends and receives
										// messages
				JOptionPane.showMessageDialog(point, ex.getMessage()); // dialog box for exception handling
				messages.setText("Connection Unavalable!");
				try {
					wait(3000);// gives the system some time to try and connect
				} catch (InterruptedException ex1) {
				}
				System.exit(0); // exits the program if the system does not wait to reestablish a connection
			}
		}
	};

}
