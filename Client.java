import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class Client extends JFrame
{
	//GUI stuff
    private JTextArea serverTextArea = new JTextArea(3, 8);
	private JButton sendButton = new JButton("Send");
	private JTextField inputArea = new JTextField(10);
    private PutTextHandler putText = new PutTextHandler();
	private ScrollPane scrollPane = new ScrollPane();
	private Panel topPanel = new Panel();
	private Panel bottomPanel = new Panel();
	private JLabel topLeftCornerLabel = new JLabel("");
	private JLabel titleLabel = new JLabel();
	private JLabel topRightCornerLabel = new JLabel("");
	
    private JMenuBar menuBar = new JMenuBar();
    private JMenu file = new JMenu("File");
    private JMenu edit = new JMenu("Edit");
    private JMenuItem changeName = new JMenuItem("Change name");
    private JMenuItem exit = new JMenuItem("Exit");
    private WindowListener exitBehavior;
	
	private String message = "";  //Sent to server
    private String chat = "";     //Recieved from server
    private String userName = "";
    
    //Connection stuff
    private Socket socket;
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private int clientNumber;
	
	public Client() throws UnknownHostException, IOException
	{
		//Connects to the server and obtains its client number.
        socket = new Socket("127.0.0.1", 8000);
        toServer = new DataOutputStream(socket.getOutputStream());
        fromServer = new DataInputStream(socket.getInputStream());
        clientNumber = fromServer.readInt();//Ray - this gets the client's assigned number from the server
		userName = "Client" + clientNumber;
        
        //--------Layout Stuff Below--------//
        
		//JFrame Properties
        setIconImage(Toolkit.getDefaultToolkit().getImage(Client.class.getResource("/linkChat/images/triforceIcon2.gif")));
        setBackground(new Color(51, 0, 0));
		setTitle(userName);
		setBounds(100, 100, 400, 300);
		setLayout(new BorderLayout(0, 0));
		
		//has the x button send "bye" to the server
		//to close everything so that there won't be 
		//connection errors
		exitBehavior = new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				sendMessage("bye");
			}
		};
		addWindowListener(exitBehavior);
			
		//------Middle of the Frame Stuff------
		//Display text box nested in a scrollPane
		serverTextArea.setBorder(null);
		serverTextArea.setForeground(new Color(255, 255, 255));
		serverTextArea.setBackground(new Color(0, 51, 0));
		serverTextArea.setEditable(false);
		
		scrollPane.setBackground(new Color(51, 0, 0));
		scrollPane.add(serverTextArea);
		add(scrollPane, BorderLayout.CENTER);
		
		//------Top Row Stuff------
		//Top Panel
		topPanel.setBackground(new Color(51, 0, 0));
		topPanel.setSize(new Dimension(0, 100));
		topPanel.setLayout(new BorderLayout(0, 0));
		add(topPanel, BorderLayout.NORTH); //Adds this to north of main panel.
		
		//Top Left Corner Label (With decoration in it)
		topLeftCornerLabel.setIcon(new ImageIcon(Client.class.getResource("/linkChat/images/clientBorderCorn.gif")));
		topPanel.add(topLeftCornerLabel,BorderLayout.WEST);
		
		//Top Midgle Label (With the client number in it)
		titleLabel = new JLabel(userName);
		titleLabel.setForeground(new Color(255, 204, 51));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		topPanel.add(titleLabel,BorderLayout.CENTER);
		
		//Top Left Corner Label (With decoration in it)
		topRightCornerLabel.setIcon(new ImageIcon(Client.class.getResource("/linkChat/images/clientBorderCorn.gif")));
		topPanel.add(topRightCornerLabel, BorderLayout.EAST);
		
		//------Bottom Row Stuff------
		//Bottom Panel
		bottomPanel.setBackground(new Color(51, 0, 0));//set
		bottomPanel.setLayout(new BorderLayout(0, 0));
		
		//Type Area
		inputArea.setBackground(new Color(51, 0, 0));
		inputArea.setForeground(new Color(255, 255, 255));
		inputArea.setText("Start chatting!");
		inputArea.setBorder(null);
		inputArea.setColumns(10);
		inputArea.addActionListener(putText);
		bottomPanel.add(inputArea, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);  //Adds this to south of main panel
		
		//Send Button
		sendButton.setPreferredSize(new Dimension(75, 23));
		bottomPanel.add(sendButton, BorderLayout.EAST);
		sendButton.addActionListener(putText);
		
		//menubar stuff
		edit.add(changeName);
		file.add(exit);
		menuBar.add(file);
		menuBar.add(edit);
		setJMenuBar(menuBar);
		
		//action listener of menuitems
		changeName.addActionListener(new ActionListener()
		{
			//prompts the user for a new username
			public void actionPerformed(ActionEvent e)
			{
				try {
					String newName = JOptionPane.showInputDialog("Enter new user name");
					String lowerCaseCheck = ""; 
					
					//if the user pressed cancel, then newName = null
					//also checks if the user enters a blank name
					if(newName == null || newName.equals(""))
						return;//jump out of the method if the user pressed cancel
					else
						lowerCaseCheck = newName.toLowerCase();
					
					/*if the userName isn't null, then make it lower case
					 *and then check if it contains any bad words. That way,
					 *If the user types "uGly" or "Ugly", the program won't have to worry about
					 *these cases because the program makes the userName lower case and checks the content.
					*/
					if(lowerCaseCheck.contains("bad") || lowerCaseCheck.contains("damn") ||
							lowerCaseCheck.contains("ugly"))
					{
						JOptionPane.showMessageDialog(null, "Don't say bad words.");
						actionPerformed(e);//re-prompts the user
						return;//this return; is so that once the user finally enters a suitable username
						//this method will be exited and won't print onto the inputArea that
						//they can't enter bad words
					}
					
					toServer.writeUTF(userName);
					toServer.writeUTF(" changed their name to " + newName);
					userName = newName;//changes the userName sent to the server
					
					//changes the title and the title on the messaging screen to match current name
					titleLabel.setText(userName);
					setTitle(userName);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendMessage("bye");
			}
		});
		

		//--------End of Layout Stuff--------//
	}
	
	public void createWindow(int x, int y)
	{
		setTitle(userName);
		setVisible(true);
		setSize(x, y);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);//centers window
	}
	
	/* This method is put outside of the action TextHandler in case 
	 * other functions need to type out a message to other users
	 * (ie having the exit function on the menubar send "exit" to everyone, so that everything closes) 
	 */
	public void sendMessage(String m)
	{
		try 
		{
			message = m;
			//if the message is blank, don't send it (mainly for sending via button)
			if(message.equals(""))
				return;
			inputArea.setText("");  //clears Textbox
			toServer.writeUTF(userName + ": ");
			toServer.writeUTF(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}   
	}
        
	public void listen() throws IOException 
	{
		while (true)
		{
			chat = fromServer.readUTF();
                
			if (chat.equals("bye"))
		        System.exit(0);
			else
				serverTextArea.append(chat);
		}
	}
	
	//inner class put at the bottom
	class PutTextHandler implements ActionListener 
	{
		public void actionPerformed(ActionEvent e)
		{
			message = inputArea.getText();
			sendMessage(message);
        }
	}

    public static void main(String[] args) throws UnknownHostException, IOException
	{
		Client window = new Client();
		window.createWindow(400, 300);
        window.listen();
	}
    
}