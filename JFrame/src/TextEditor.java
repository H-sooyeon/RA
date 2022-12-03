import java.awt.Color;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

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
	private JPopupMenu popupmenu;
	private JScrollBar popupscroll;
	
	private static InetAddress host;
	private static Socket link = null;
	private static int position = 0;  // ������� Ŀ�� ��ġ
	private static boolean isReceive;
	
	public static void main(String[] args) {
		System.setProperty( "https.protocols", "TLSv1.1,TLSv1.2" );  // connection reset ���� �ذ�

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
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
					e.printStackTrace();
				}
			}
		});
	}

	// Create TextEditor
	public TextEditor() {
		initialize();  // ���� �ʱ�ȭ
	}
	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100,100,450,300);  // ��ġ, �ʺ�, ���� ����(x,y,w,h)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // ���� �̺�Ʈ ó��
		
		JMenuBar menuBar = new JMenuBar();  // �޴��� ��ü ����
		frame.setJMenuBar(menuBar);  // �޴��� ����
		
		JMenu mnFile = new JMenu("File");  // File �޴� ����
		menuBar.add(mnFile);  // File �޴� �߰�
		
		JMenuItem mntmNew = new JMenuItem("New");  // ���� �޴� ����
		
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval = fileChooser.showDialog(frame, "New");  // Dialog ����
				if(retval == JFileChooser.APPROVE_OPTION) {  // �� Ȯ���� �������� �� ���ϵǴ� ��
					file = fileChooser.getSelectedFile();  // ������ ������ ��θ� ��ȯ, file�� ����
					textArea.setText("");
				}
			}
		}); // end ActionListener
		
		mnFile.add(mntmNew);  // New ����
		
		JMenuItem mntmOpen = new JMenuItem("Open");  // ���� �޴� ����
		
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dialog â���� ���Ⱑ ���������� ����� ��� 0�� ��ȯ, ��Ҹ� ���� ��� 1�� ��ȯ
				int retval = fileChooser.showOpenDialog(frame);  // ����� â ����
				
				// ������ �� ������ ���
				if(retval == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();  // ������ ������ ��� ����

					try {
						FileReader fr = new FileReader(file);  // ������ ���Ϸκ��� �о�� ��ü ����
						BufferedReader br = new BufferedReader(fr);
						
						String str = "";
						String textLine = "";
						
						textLine = br.readLine();
						
						while((str = br.readLine()) != null) {
							textLine = textLine + "\n" + str;
						}
						textArea.setText(textLine);
					} catch(Exception e) {
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				} else {
					// ������ ����� ������ ���� ��� or �׳� ���� ��� Not working �޽��� ����
					JOptionPane.showMessageDialog(frame, "User canced the operation");
				}
			}
		}); // end ActionListener
		
		mnFile.add(mntmOpen);  // Open ����
		
		JMenuItem mntmSave = new JMenuItem("Save");  // ���� �޴� ����
		
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(file != null) {
					// ������ ���Ͽ� ����
					try {
						FileWriter wr = new FileWriter(file, false);
						BufferedWriter bw = new BufferedWriter(wr);
						bw.write(textArea.getText());
						bw.flush();
						bw.close();
					} catch(Exception e) {
						// ���� ó��
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}	
			}
		}); // end ActionListener
		
		mnFile.add(mntmSave);  // Save ����
		
		JMenuItem mntmClose = new JMenuItem("Close");  // ���� �޴� ����
		
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// �ؽ�Ʈ ���� ������ �������� �ʱ�ȭ�ϰ�, ���� ���õ� ������ �ʱ�ȭ
				
				file = null;  // ���� ���õ� ���� �ʱ�ȭ
				textArea.setText("");
			
			}
		}); // end ActionListener
		
		mnFile.add(mntmClose);  // Close ����
		
		JMenuItem mntmQuit = new JMenuItem("Quit");  // ���� �޴� ����
		
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);  // ���� ����
			}
		}); // end ActionListener
		
		mnFile.add(mntmQuit);  // Quit ����
		
		JMenu mnAbout = new JMenu("About");  // About �޴� ����
		menuBar.add(mnAbout);  // About �޴� �߰�
		
		JMenuItem mntmAboutTextEditor = new JMenuItem("About Text Editor");  // About �޴��� ������ ���� �޴�
		
		mntmAboutTextEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Text Editor Sample");
			}
		}); // end ActionListener
		
		mnAbout.add(mntmAboutTextEditor);  // About Text Editor �߰�

		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
	
		// �ؽ�Ʈ �����Ϳ� ��ũ�� �߰�
		JScrollPane scrollPane = new JScrollPane();
		// frame���� ������ ������ �������� ������ ������ scroll �߰�
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		// scrollpane�� text �߰�(�ؽ�Ʈ�� ��ũ�� �߰�)
		scrollPane.setViewportView(textArea);
		
		// JTextArea�� line number �߰�
		lines = new JTextArea("1");  // ���� ��ȣ
		lines.setBackground(Color.LIGHT_GRAY); // line number ��� ��
		lines.setEditable(false);  // line number �̹Ƿ� ����(����) �Ұ�
		
		// ������ ������ ����Ǿ��� ���� �̺�Ʈ ����
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			public String getText(){
				int caretPosition = textArea.getDocument().getLength();
				
				javax.swing.text.Element root = textArea.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				
				for(int i = 2; i < ((javax.swing.text.Element) root).getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			} // end getText
			
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
 
		}); // end DocumentListener
		
		// viewport: �⺻�� �Ǵ� ������ ���� ���� ����ϴ� ����Ʈ(â)
		scrollPane.getViewport().add(textArea);
		scrollPane.setRowHeaderView(lines);  // �� ����� �߰�
		// �׻� ��ũ�ѹٰ� ���̵��� ����
		// setVerticalScrollBarPolicy: ���������� ��å ���� Ȥ�� �б�
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
		frame.add(scrollPane);
		
		// textArea���� tabŰ�� ������ �̺�Ʈ �߻� (������ ������ ����)
		textArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if(keyCode == KeyEvent.VK_TAB) {
					popupmenu = new JPopupMenu();
					
					ServerCommunication SC = new ServerCommunication(link, host, textArea);
					
					ArrayList<String> list = SC.list;
					ArrayList<Integer> cursorList = new ArrayList<>();
					cursorList.add(0);
					
					position = SC.position;
					isReceive = SC.isReceive;
					
					// Ȯ�ο� ���, popupmenu�� �߰�
					for(int i = 1; i < list.size(); i++) {
						// popupmenu�� ���ڿ� �߰�
						JMenuItem menuitem = new JMenuItem(list.get(i));
						
						// ���ڿ� "..." ��ġ ����
						int setcursor = list.get(i).indexOf("...");
						
						// ...�� ������ ó�� ... ��ġ�� Ŀ�� ��ġ ����
						cursorList.add(setcursor);
						
						if(setcursor != -1) {
							// list�� �ִ� ... ���ڿ� ����
							list.set(i, list.get(i).replace("...", ""));
						}
						
						int stridx = list.get(i).length();
						
						// ������ ������ ���� �� ���ڿ� ����, ������ ���ڿ� �״�� ����
						String itemHeader = list.get(i).substring(0, stridx);
						
						menuitem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								textArea.append(itemHeader);
								int listIndex = list.indexOf(itemHeader);
								
								if(cursorList.get(listIndex) != -1) {
									textArea.setCaretPosition(position + cursorList.get(listIndex));
								}
							}
							
						}); // end ActionListener
						
						popupmenu.add(menuitem);
						
					} // end for
					frame.add(popupmenu);
				}
			} // end keyPressed

			// Ű�� ������ ���� ���� ���� ����
			@Override
			public void keyReleased(KeyEvent e) {
				// �����κ��� ���� ���ڿ��� popup menu�� ���
				int lineNum = 1;
		        int columnNum = 0;
				int keyCode = e.getKeyCode();
				if( isReceive && (keyCode == KeyEvent.VK_TAB) ) {
					// Ŀ�� ��ġ�� ���� ��ġ�� ����
					try {
						// tabŰ�� ���� ���� ����
						textArea.replaceRange("", position, position+1);
			            
			            int caretpos = textArea.getCaretPosition();
			            
			            lineNum = textArea.getLineOfOffset(caretpos);
			            columnNum = caretpos - textArea.getLineStartOffset(lineNum) + 1;
			            lineNum += 1;
			            
			        }
			        catch(Exception ex){
			        	ex.printStackTrace();
				}
					popupmenu.show(textArea, (int)(columnNum * 4.5), lineNum * 15);
			 }
			} // end keyReleased
			}); // end KeyListener
		textArea.setComponentPopupMenu(popupmenu);
		
		frame.pack();  // JRrame�� ���빰�� �˸°� ������ ũ�� ����
		frame.setSize(500,500);  // ������ â ũ�� ����
		frame.setVisible(true);  // ������ â ���÷���
 
	}
	
}