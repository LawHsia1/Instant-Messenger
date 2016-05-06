import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame {
	//server variables
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataOutputStream[] outputStreams; 
	private int clientNum;
	
	//for GUI
	private JTextArea serverTextArea = new JTextArea();
	private JPanel contentPane = new JPanel(); // Will hold the all of the content.
	private JPanel panelTop = new JPanel();
	private JLabel labelTopLeft = new JLabel("");
	private JLabel labelTopRight = new JLabel("");
	private BufferedImage imageTop = ImageIO.read(Server.class.getResource("/linkChat/images/borderHorizTop.gif"));
	private TiledImage topImagePanel = new TiledImage(imageTop);
	private JPanel panelBottom = new JPanel();
	private JPanel panelRight = new JPanel();
	private JPanel panelLeft = new JPanel();
	private JLabel labelBottomLeft = new JLabel("");
	private JLabel labelBottomRight = new JLabel("");
	private BufferedImage imageBottom = ImageIO.read(Server.class.getResource("/linkChat/images/borderHorizBot.gif"));
	private BufferedImage imageRight = ImageIO.read(Server.class.getResource("/linkChat/images/borderVert.gif"));
	private BufferedImage imageLeft = ImageIO.read(Server.class.getResource("/linkChat/images/borderVertleft.gif"));
	private TiledImage bottomImagePanel = new TiledImage(imageBottom);
	private TiledImage imageRightPanel = new TiledImage(imageRight);
	private TiledImage imageLeftPanel = new TiledImage(imageLeft);
	
	//For receiving data from the window client.
	private Socket windowSocket;
	private DataInputStream windowInput;
	private ImageIcon borderCorner = new ImageIcon(Server.class.getResource("/linkChat/images/borderCorner.gif"));
	private PromptWindow promptWindow = new PromptWindow();
	
	
	public Server() throws IOException
	{
		//--------Layout Stuff Below-------//
		
		//JFrame Stuff
		setIconImage(Toolkit.getDefaultToolkit().getImage(Server.class.getResource("/linkChat/images/triforceIcon2.gif")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("LinkChatServer");
		setSize(602, 337);
		setLocationRelativeTo(null);
		setVisible(true);
		
		//Main Panel
		contentPane.setBackground(new Color(0, 0, 0));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		//Text Display Area
		serverTextArea.setMargin(new Insets(2, 5, 2, 2));
		serverTextArea.setForeground(new Color(255, 255, 255));
		serverTextArea.setBackground(new Color(0, 51, 0));
		serverTextArea.setEditable(false);
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(serverTextArea);
		contentPane.add(scrollPane, BorderLayout.CENTER);  //Places the scrollPane onto the contentPane
		
		//panelTop - Panel North of Main Panel
		panelTop.setBackground(new Color(0, 51, 0));
		panelTop.setLayout(new BorderLayout());
		contentPane.add(panelTop, BorderLayout.NORTH);
		
		//panelTop Elements
		labelTopLeft.setIcon(borderCorner);
		panelTop.add(labelTopLeft, BorderLayout.WEST);
		labelTopRight.setIcon(borderCorner);
		panelTop.add(labelTopRight, BorderLayout.EAST);
		panelTop.add(topImagePanel, BorderLayout.CENTER);
		
		//panelBottom - Panel south of the Main Panel
		panelBottom.setBackground(new Color(0, 51, 0));
		panelBottom.setLayout(new BorderLayout());
		contentPane.add(panelBottom, BorderLayout.SOUTH);
		
		//panelBottom Elements
		labelBottomLeft.setIcon(borderCorner);
		panelBottom.add(labelBottomLeft, BorderLayout.WEST);
		labelBottomRight.setIcon(borderCorner);
		panelBottom.add(labelBottomRight, BorderLayout.EAST);
		panelBottom.add(bottomImagePanel, BorderLayout.CENTER);
		
		//panelRight - Panel West of the Main Panel
		panelRight.setBackground(new Color(0, 51, 0));
		panelRight.setLayout(new BorderLayout());
		contentPane.add(panelRight, BorderLayout.EAST);
		panelRight.add(imageRightPanel, BorderLayout.CENTER);

		//panelLeft - Panel West of the Main Panel
		panelLeft.setBackground(new Color(0, 51, 0));
		panelLeft.setLayout(new BorderLayout());
		contentPane.add(panelLeft, BorderLayout.WEST);
		panelLeft.add(imageLeftPanel, BorderLayout.CENTER);
		
		//--------Layout Stuff Over-------//
		
		//Opens the "Number of Clients" prompt.
		promptWindow.openWindow();
		
		//Receives the number of clients from windowInput.
		serverSocket = new ServerSocket(8000);
		windowSocket = serverSocket.accept();
		windowInput = new DataInputStream(windowSocket.getInputStream());
		clientNum = windowInput.readInt();
		serverTextArea.append("Waiting for " + clientNum + " clients to connect." + '\n');
		windowInput.close();
		windowSocket.close();
		
		//Instantiates an array of output streams for each client.
		outputStreams = new DataOutputStream[clientNum];
		
		//Inputs data into each array each time a client connects.
		//Also starts a thread for each client.
		for(int i = 0; i < clientNum; i++)
		{
			clientSocket = serverSocket.accept();//socket is instantiated here. 
			outputStreams[i] = new DataOutputStream(clientSocket.getOutputStream());
			outputStreams[i].writeInt(i + 1);//gives the client its # so it'll know what # to put in its title
			serverTextArea.append("Accepted client " + (i+1) + " at " + clientSocket + '\n');
			
			TalkToClient task = new TalkToClient(clientSocket); //Send the socket of this client to the thread constructor.
			new Thread(task).start();
		}

	}
	
	//Thread
	class TalkToClient implements Runnable {
		private Socket socket;
		private DataInputStream fromClient;
		private DataOutputStream toClient;
		private String clientName;
		private String clientMessage;
		
		public TalkToClient(Socket s) {
			socket = s;
		}
		
		public void run() 
		{
			//Sets up input/output streams for the client that connected here.
			//(Only needs to do this once for each client)
			try 
			{
				fromClient = new DataInputStream(socket.getInputStream()); 
				toClient =  new DataOutputStream(socket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//Continues to listen to and deliver messages to this client.
			while (true)
			{
		        try {
		        	//All clients send a name and then a message.
		        	clientName = fromClient.readUTF();
					clientMessage = fromClient.readUTF();
					
					//checks client's message for bad words
					if (clientMessage.contains("bad") || clientMessage.contains("ugly") || clientMessage.contains("damn"))
					{
						toClient.writeUTF("Don't say bad words." + '\n');
						serverTextArea.append("**Client sent a bad word. The following message was not sent**" + '\n');
					}
					//exits program if a client sends "bye"
					else if (clientMessage.equals("bye"))
					{
						for(int i = 0; i < clientNum; i++)
							outputStreams[i].writeUTF("bye");
				        System.exit(0); 
					}
					//otherwise, send client's message to everyone by 
					//looping through each client's outputstream and writing to them.
					else
						for(int i = 0; i < clientNum; i++)
							outputStreams[i].writeUTF(clientName + clientMessage + '\n');
					
					serverTextArea.append(clientName + clientMessage  + '\n');
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//This method tiles the images used for the borders. Doesn't need to be static
	private class TiledImage extends JPanel {  
	    private BufferedImage tileImage;  
	   
	    public TiledImage(BufferedImage image) {  
	        tileImage = image;  
	    }  
	    
	   
	    public Dimension getPreferredSize() {  
	        return new Dimension(48, 30);  
	    }  
		
	    protected void paintComponent(Graphics g) 
	    {  
	        int width = getWidth();  
	        int height = getHeight();  
	        int imageW = tileImage.getWidth(this);  
	        int imageH = tileImage.getHeight(this); 
	        //
	        for (int x = 0; x < width; x += imageW) 
	        {  
	            for (int y = 0; y < height; y += imageH) 
	                g.drawImage(tileImage, x, y, this);  
	        }  
	    } 
	}
		
	public static void main (String[] args) throws IOException
	{
		new Server();
	}
}