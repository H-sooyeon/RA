import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TextEditor {
	private JFrame frame;
	private JTextArea textArea;  // �ؽ�Ʈ ���� ����, ���� ���� �Է��� �� �ִ� ������Ʈ(��ü ��ũ�� ����)
	private JFileChooser fileChooser = new JFileChooser();  // ���� ���� ����
	private File file;
	private JTextArea lines;  // line number
	
	private static String message;  // socket ���� �޼���
	private static final int PORT = 50000;
	private static InetAddress host;
	static Socket link = null;
	static Scanner input= null;
	static PrintWriter output = null;
	static int position = 0;  // ������� Ŀ�� ��ġ
	private String receiveMessage;  // �����κ��� ���� �޼���
	private ArrayList<String> list = new ArrayList<>();
	
	public static void main(String[] args) {
		System.setProperty( "https.protocols", "TLSv1.1,TLSv1.2" );  // connection reset ���� �ذ�

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// host�� ip�ּҸ� �����´�.
					host = InetAddress.getLocalHost();
				} 
				catch(UnknownHostException uhEx) {
					// ȣ��Ʈ ip�� ã�� ���ϸ� �޽��� â�� ���鼭 ����
					JOptionPane.showMessageDialog(null, "Host ID not found!", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				try {
					
					TextEditor window = new TextEditor();
					window.frame.setVisible(true);
				} 
				catch(Exception e) {
					
				}
			}
		});
	}
	
	private static void closingConnecting() {
		// ���� ����
		try {
			System.out.println("\n* Closing connection... *");
			link.close();
			link = null;
		} catch(IOException ioEx) {
			System.out.println("Connecting error");
		}
	}
	
	// ������ ����, ���� inputstream, outputstream ��ȯ
	private static void accessServer() {
		try {
			// ������ ȣ��Ʈ ����
			link = new Socket(host, PORT);
			
			// �������κ��� ����Ʈ�� �д� �� ����� �� �ִ� InputStream ��ȯ
			input = new Scanner(link.getInputStream());
			
			// �������� write�� outputStream ��ȯ
			output = new PrintWriter(link.getOutputStream());
			
			// socekt ������ �ƴ��� Ȯ���ϱ� ���� �ʵ�
			boolean result = link.isConnected();
			if(result) System.out.println("Connecting Server");
			else System.out.println("Fail Connecting Server");
		}
		catch(IOException ioEx) {
			System.out.println("Connection refused");
		}
	}
	
	// Create TextEditor
	public TextEditor() {
		initialize();  // ���� �ʱ�ȭ
	}
	
	private void initialize() {
		frame = new JFrame();  // Ÿ��Ʋ�� ���� ������ ����
		frame.setBounds(100,100,450,300);  // ��ġ, �ʺ�, ���� ����(x,y,w,h)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // ���� �̺�Ʈ ó��
		
		JMenuBar menuBar = new JMenuBar();  // �޴��� ��ü ����
		frame.setJMenuBar(menuBar);  // �����ӿ� �޴��� ����
		
		JMenu mnFile = new JMenu("File");  // �޴� ǥ���ٿ� ���̰� �� File �޴� ����
		menuBar.add(mnFile);  // �޴��ٿ� File �޴� �߰�
		
		JMenuItem mntmNew = new JMenuItem("New");  // �޴��� ���Ե� ���� �޴� ����
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval = fileChooser.showDialog(frame, "New");  // Dialog ����
				if(retval == JFileChooser.APPROVE_OPTION) {  // �� Ȯ���� �������� �� ���ϵǴ� ��
					file = fileChooser.getSelectedFile();  // ������ ������ ��θ� ��ȯ, file�� ����
					textArea.setText("");  // �������� �ʱ�ȭ
				}
			}
		});
		
		mnFile.add(mntmNew);  // �̺�Ʈ ���� �� File �޴��� ���� �޴��� New ����
		
		JMenuItem mntmOpen = new JMenuItem("Open");  // �޴��� ���Ե� ���� �޴� ����
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dialog â���� ���Ⱑ ���������� ����� ��� 0�� ��ȯ, ��Ҹ� ���� ��� 1�� ��ȯ
				int retval = fileChooser.showOpenDialog(frame);  // ����� â ����
				
				// ������ �� ������ ���
				if(retval == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();  // ������ ������ ��� ����
					// ������ ������ �о�
					// JTextArea�� �߰�
					// �˻� ���� ó���ϴ� �ڵ� �ۼ�
					try {
						FileReader fr = new FileReader(file);  // ������ ���Ϸκ��� �о�� ��ü ����
						BufferedReader br = new BufferedReader(fr);  // ���۸� �̿��ؼ� ���(ȿ����)
						
						String str = "";
						String textLine = "";
						
						textLine = br.readLine();  // BufferedReader�� ������ �ִ� ���ڿ� �� �� �б�
						
						// BufferedLine�� readLine�� ���� �����Ͱ� ���� �� null ����
						while((str = br.readLine()) != null) {
							textLine = textLine + "\n" + str;  // �� �پ� ��� ����
						}
						textArea.setText(textLine);  // ������ ������ text�� textArea�� set
					} catch(Exception e) {
						JOptionPane.showMessageDialog(frame, e.getMessage());  //���� ó��
					}
				} else {
					// ������ ����� ������ ���� ��� or �׳� ���� ���
					// Not working �޽��� ����
					JOptionPane.showMessageDialog(frame, "User canced the operation");
				}
			}
		});
		mnFile.add(mntmOpen);  // �̺�Ʈ ���� �� File �޴��� ���� �޴��� Open ����
		
		JMenuItem mntmSave = new JMenuItem("Save");  // �޴��� ���Ե� ���� �޴� ����
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(file != null) {
					// ������ ���Ͽ� ����
					try {
						FileWriter wr = new FileWriter(file, false);  // ���� ��Ʈ���� �̿��Ͽ� file ��ü ����
						BufferedWriter bw = new BufferedWriter(wr);
						bw.write(textArea.getText());  // ���Ͽ� ���۸� ���ؼ� text ����(ȿ����)
						bw.flush();  // ���ۿ� ���� text ����
						bw.close();  // �ݱ�
					} catch(Exception e) {
						// ���� ó��
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}	
			}
		});
		mnFile.add(mntmSave);  // �̺�Ʈ ���� �� File �޴��� ���� �޴��� Save ����
		
		JMenuItem mntmClose = new JMenuItem("Close");  // �޴��� ���Ե� ���� �޴� ����
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// �ؽ�Ʈ ���� ������ �������� �ʱ�ȭ�ϰ�, ���� ���õ� ������ �ʱ�ȭ
				file = null;  // ���� ���õ� ���� �ʱ�ȭ
				// JTextArea�� ���
				textArea.setText("");  // �������� �ʱ�ȭ
			
			}
		});
		mnFile.add(mntmClose);  // �̺�Ʈ ���� �� File �޴��� ���� �޴��� Close ����
		
		JMenuItem mntmQuit = new JMenuItem("Quit");  // �޴��� ���Ե� ���� �޴� ����
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// ���α׷��� ����. System Ŭ������ exit �޼ҵ� ���.
				// ���α׷� ����
				System.exit(0);  // ���� ����
			}
		});
		mnFile.add(mntmQuit);  // �̺�Ʈ ���� �� File �޴��� ���� �޴��� Quit ����
		
		JMenu mnAbout = new JMenu("About");  // �޴� ǥ���ٿ� ���̰� �� About �޴� ����
		menuBar.add(mnAbout);  // �޴��ٿ� About �޴� �߰�
		
		JMenuItem mntmAboutTextEditor = new JMenuItem("About Text Editor");  // About �޴��� ������ ���� �޴�
		mntmAboutTextEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Text Editor Sample");  // �ܼ��� �˸�â�� ���� �Լ�
			}
		});
		mnAbout.add(mntmAboutTextEditor);  // �̺�Ʈ ���� �� About �޴��� ���� �޴��� About Text Editor �߰�
		// ������ ������ �����´�.
		// ������ ������ �����Ǵ� ������ҵ��� ��� ��ġ�� ������ �����ش�.
		// x �������� ������Ʈ���� ��ġ
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
	
		// �ؽ�Ʈ �����Ϳ� ��ũ�� �߰�
		JScrollPane scrollPane = new JScrollPane();
		// frame���� ������ ������ �����´�.
		// ������ ������ scroll�� �߰�
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		// scrollpane�� text �߰�(�ؽ�Ʈ�� ��ũ�� �߰�)
		scrollPane.setViewportView(textArea);
		
		// JTextArea�� line number �߰�
		lines = new JTextArea("1");  // ���� ��ȣ
		lines.setBackground(Color.LIGHT_GRAY); // line number ��� ��
		lines.setEditable(false);  // line number �̹Ƿ� ����(����) �Ұ�
		
		// ������ ������ ����Ǿ��� ���� �̺�Ʈ ����
		textArea.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int caretPosition = textArea.getDocument().getLength();
				
				javax.swing.text.Element root = textArea.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				
				for(int i = 2; i < ((javax.swing.text.Element) root).getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
			}
 
			@Override
			public void insertUpdate(DocumentEvent de) {  // ���� �߰��� ��
				lines.setText(getText());  // �߰��� ��ŭ line number �缳��
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {  // ���� ����� ��
				lines.setText(getText());  // ����� ��ŭ line number �缳��
			}
 
		});
		
		// viewport: �⺻�� �Ǵ� ������ ���� ���� ����ϴ� ����Ʈ(â)
		scrollPane.getViewport().add(textArea);
		scrollPane.setRowHeaderView(lines);  // �� ����� �߰�
		// �׻� ��ũ�ѹٰ� ���̵��� ����
		// setVerticalScrollBarPolicy: ���������� ��å ���� Ȥ�� �б�
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
		frame.add(scrollPane);  // frame�� scollPane ��ü �߰�
		
		// textArea���� tabŰ�� ������ �̺�Ʈ �߻� (������ ������ ����)
		textArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			// Ű�� ������ ������ ���� ���� ����
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if(keyCode == KeyEvent.VK_ENTER) {
					// ����ڰ� �Է��� text�� �����´�. (����?)
					
				}
				else if(keyCode == KeyEvent.VK_TAB) {
					// tab Ű�� ������ ���� ����
					accessServer();
				
					// ����ڰ� �Է��� Ŀ�� �� text�� �����´�.
					position = textArea.getCaretPosition();
					message = textArea.getText();
					message = message.substring(0, position);
					
					// text�� ���� ��ȯ
					int textLength = message.length();
						
					// text�� ���̸� �ϳ��� ���ڿ��� ��ģ��.
					String sendMessage = "" + textLength + " True";
					
					// �������� �ؽ�Ʈ ���� ����
					output.println(sendMessage);
					System.out.println(sendMessage);
					
					// Ŀ�� �� �ؽ�Ʈ ����, true ���� �� ���� ���� ����
					closingConnecting();
					
					// ���� Ȯ�ο� ���߿� ����
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					// �ٽ� ���� ����, Ŀ�� ���� �ؽ�Ʈ ������
					accessServer();
					System.out.println("\n" + message);
					output.println(message);
					// ���� ����
					closingConnecting();
					
					// ���� Ȯ�ο� ���߿� ����
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					// ���� ����, Ŀ�� ���� �ؽ�Ʈ�� ������.
					message = textArea.getText();
					message = message.substring(position, textArea.getText().length());
					
					accessServer();
					output.println(message);
					
					System.out.println(message);
					
					// ���� ����
					closingConnecting();
					
					// ���� Ȯ�ο� ���߿� ����
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					
					accessServer();
					
					// �����κ��� ���ڿ��� �޾ƿ´�.  �̺κ��� ��Ʈ�� window���� �ȵȴ�.(���ڿ��� �������)
					try {
						receiveMessage = input.nextLine();
						
						System.out.println(receiveMessage);
						
					} catch (Exception e1) {
						System.out.println(e1.getMessage());
					}
					
					closingConnecting();
					
					// pattern1 : ��� ���ڿ�
					// pattern2 : ... ���ڿ�
					Pattern pattern1 = Pattern.compile("[\nwhite](.*?)[white]");
					Pattern pattern2 = Pattern.compile("[white](.*?)[\nwhite]");
					
					Matcher matcher1 = pattern1.matcher(receiveMessage);
					Matcher matcher2 = pattern2.matcher(receiveMessage);
					
					// ����� �迭�� ����
					String result = "";
					
					// ��ġ�ϴ°� �ִٸ� ���
					while(matcher1.find()) {
						int i = 0;
						
						// ���� ã�� �� ������ break
						if(matcher1.group(1) == null) break;
						
						System.out.println(matcher1.group(1) + " " + matcher2.group(1));
						// ã�� ���ڿ��� �迭�� �߰� (�޸��� �������� �߰�)
						result = matcher1.group(1) + "," + matcher2.group(2);
						list.add(result);
					}
					
					// ������ �迭�� ������ ������ EndFor �߰�
					int lastIndex = list.size() - 1;
					
					String temp = list.get(lastIndex) + "EndFor";
					
					list.set(lastIndex, temp);
					
					// Ȯ�ο� ���
					for(int i = 0; i < list.size(); i++) {
						System.out.println(list.get(i));
					}
				}
			}

			// Ű�� ������ ���� ���� ���� ����
			@Override
			public void keyReleased(KeyEvent e) {
				// �����κ��� ���� ���ڿ��� context menu�� ���
				int keyCode = e.getKeyCode();
				
			}
			});
		
		frame.pack();  // pack() : JRrame�� ���빰�� �˸°� ������ ũ�� ����
		frame.setSize(500,500);  // ������ â ũ�� ����
		frame.setVisible(true);  // ������ â ���÷���
 
	}
	
}