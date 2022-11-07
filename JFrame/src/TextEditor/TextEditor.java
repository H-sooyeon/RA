package TextEditor;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.lang.model.element.Element;
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

public class TextEditor {
	private JFrame frame;
	private JTextArea textArea;  // �ؽ�Ʈ ���� ����, ���� ���� �Է��� �� �ִ� ������Ʈ(��ü ��ũ�� ����)
	private JFileChooser fileChooser = new JFileChooser();  // ���� ���� ����
	private File file;
	private JTextArea lines;  // line number
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TextEditor window = new TextEditor();
					window.frame.setVisible(true);
				} catch(Exception e) {
					
				}
			}
		});
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
		
		/*
		 * New �޴��� ���õǸ� ������ �̺�Ʈ ����
		 * ���Ϸκ��� �ҷ����ų� ���Ͽ� ������ �� �ֵ��� ���� Dialog ����
		 */
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
				// ���� ��ü�� �ִٸ�, ������ ������ ������ ���Ͽ� ����
				// JTextArea�� �Էµ�(������) ������
				// ������ ���Ͽ� ����
				// �˻� ���� ó��
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
				
				for(int i = 2; i < ((javax.swing.text.Element) root).getElementIndex( caretPosition ) + 1; i++){
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
		frame.pack();  // pack() : JRrame�� ���빰�� �˸°� ������ ũ�� ����
		frame.setSize(500,500);  // ������ â ũ�� ����
		frame.setVisible(true);  // ������ â ���÷���
 
	}
	
}
