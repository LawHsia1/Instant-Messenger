import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Toolkit;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PromptWindow extends JFrame{

	//Global Stuff
	private JPanel contentPane = new JPanel();
	private JLabel clientIconLabel = new JLabel("");
	private JComboBox clientComboBox = new JComboBox();
	private JButton readyButton = new JButton("Link Up!");
	private JLabel backgroundLabel = new JLabel("");
	private int clientNum;
	
	public void openWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try 
				{
					//JFrame stuff in here so that there's not 2 prompt windows created.
					setSize(605,350);
					setResizable(false);
					setLocationRelativeTo(null);
					setIconImage(Toolkit.getDefaultToolkit().getImage(PromptWindow.class.getResource("/linkChat/images/triforceIcon2.gif")));
					setTitle("LinkChat");
					setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public PromptWindow() {
		
		/*--Almost all of this stuff is design elements.--*/
		
		//JPanel Stuff
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//Start Button
		readyButton.setBounds(478, 263, 80, 31);
		readyButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						clientNum = clientComboBox.getSelectedIndex() + 1;
						try {
							//connects and sends the # of clients to the server
							Socket socket = new Socket("127.0.0.1", 8000);
							DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
							toServer.writeInt(clientNum);
							dispose();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				}}); 
		contentPane.add(readyButton);
		
		//Drop-down menu for selecting the number of clients.
		clientComboBox.setModel(new DefaultComboBoxModel(new String[] {"1 Client", "2 Clients", "3 Clients", "4 Clients", "5 Clients"}));
		clientComboBox.setBounds(42, 265, 243, 26);
		
		clientComboBox.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						JComboBox cb = (JComboBox)e.getSource();
						clientNum = cb.getSelectedIndex() + 1;
						updateLabel(clientNum);
				}}); 
		
		contentPane.add(clientComboBox);
		
		//A Label that changes images depending on how many clients the user selects.
		clientIconLabel.setBackground(new Color(255, 255, 255));
		clientIconLabel.setBounds(307, 260, 150, 38);
		updateLabel(clientComboBox.getSelectedIndex() + 1);
		contentPane.add(clientIconLabel);
		
		//Background Image
		backgroundLabel.setIcon(new ImageIcon(PromptWindow.class.getResource("/linkChat/images/linkchat1-01.jpg")));
		backgroundLabel.setBounds(0, 0, 600, 325);
		contentPane.add(backgroundLabel);
		
	}
	
	public void updateLabel(int fileNum) {
		clientIconLabel.setIcon(new ImageIcon(PromptWindow.class.getResource("/linkChat/images/Clients" + fileNum + ".jpg")));
	}
	
}
