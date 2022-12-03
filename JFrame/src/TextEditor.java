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
	private JTextArea textArea;  // 텍스트 편집 영역, 여러 줄을 입력할 수 있는 컴포넌트(자체 스크롤 없음)
	private JFileChooser fileChooser = new JFileChooser();  // 파일 선택 열기
	private File file;
	private JTextArea lines;  // line number
	private JPopupMenu popupmenu;
	private JScrollBar popupscroll;
	
	private static InetAddress host;
	private static Socket link = null;
	private static int position = 0;  // 사용자의 커서 위치
	private static boolean isReceive;
	
	public static void main(String[] args) {
		System.setProperty( "https.protocols", "TLSv1.1,TLSv1.2" );  // connection reset 문제 해결

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					host = InetAddress.getLocalHost();
				} 
				catch(UnknownHostException uhEx) {
					// 호스트 ip를 찾지 못하면 메시지 창을 띄우면서 종료
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
		initialize();  // 내용 초기화
	}
	
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100,100,450,300);  // 위치, 너비, 높이 지정(x,y,w,h)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 종료 이벤트 처리
		
		JMenuBar menuBar = new JMenuBar();  // 메뉴바 객체 생성
		frame.setJMenuBar(menuBar);  // 메뉴바 적용
		
		JMenu mnFile = new JMenu("File");  // File 메뉴 생성
		menuBar.add(mnFile);  // File 메뉴 추가
		
		JMenuItem mntmNew = new JMenuItem("New");  // 세부 메뉴 생성
		
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval = fileChooser.showDialog(frame, "New");  // Dialog 생성
				if(retval == JFileChooser.APPROVE_OPTION) {  // 예 확인을 선택했을 때 리턴되는 값
					file = fileChooser.getSelectedFile();  // 생성한 파일의 경로를 반환, file에 저장
					textArea.setText("");
				}
			}
		}); // end ActionListener
		
		mnFile.add(mntmNew);  // New 적용
		
		JMenuItem mntmOpen = new JMenuItem("Open");  // 세부 메뉴 생성
		
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dialog 창에서 열기가 정상적으로 수행된 경우 0을 반환, 취소를 누른 경우 1을 반환
				int retval = fileChooser.showOpenDialog(frame);  // 열기용 창 오픈
				
				// 파일이 잘 열렸을 경우
				if(retval == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();  // 선택한 파일의 경로 저장

					try {
						FileReader fr = new FileReader(file);  // 지정한 파일로부터 읽어올 객체 생성
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
					// 파일이 제대로 열리지 않은 경우 or 그냥 닫은 경우 Not working 메시지 띄우기
					JOptionPane.showMessageDialog(frame, "User canced the operation");
				}
			}
		}); // end ActionListener
		
		mnFile.add(mntmOpen);  // Open 적용
		
		JMenuItem mntmSave = new JMenuItem("Save");  // 세부 메뉴 생성
		
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(file != null) {
					// 선택한 파일에 저장
					try {
						FileWriter wr = new FileWriter(file, false);
						BufferedWriter bw = new BufferedWriter(wr);
						bw.write(textArea.getText());
						bw.flush();
						bw.close();
					} catch(Exception e) {
						// 예외 처리
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}	
			}
		}); // end ActionListener
		
		mnFile.add(mntmSave);  // Save 적용
		
		JMenuItem mntmClose = new JMenuItem("Close");  // 세부 메뉴 생성
		
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 텍스트 편집 영역을 공백으로 초기화하고, 현재 선택된 파일을 초기화
				
				file = null;  // 현재 선택된 파일 초기화
				textArea.setText("");
			
			}
		}); // end ActionListener
		
		mnFile.add(mntmClose);  // Close 적용
		
		JMenuItem mntmQuit = new JMenuItem("Quit");  // 세부 메뉴 생성
		
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);  // 정상 종료
			}
		}); // end ActionListener
		
		mnFile.add(mntmQuit);  // Quit 적용
		
		JMenu mnAbout = new JMenu("About");  // About 메뉴 생성
		menuBar.add(mnAbout);  // About 메뉴 추가
		
		JMenuItem mntmAboutTextEditor = new JMenuItem("About Text Editor");  // About 메뉴에 포함할 세부 메뉴
		
		mntmAboutTextEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Text Editor Sample");
			}
		}); // end ActionListener
		
		mnAbout.add(mntmAboutTextEditor);  // About Text Editor 추가

		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
	
		// 텍스트 에디터에 스크롤 추가
		JScrollPane scrollPane = new JScrollPane();
		// frame에서 컨텐츠 영역을 가져오고 컨텐츠 영역에 scroll 추가
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		// scrollpane에 text 추가(텍스트에 스크롤 추가)
		scrollPane.setViewportView(textArea);
		
		// JTextArea에 line number 추가
		lines = new JTextArea("1");  // 시작 번호
		lines.setBackground(Color.LIGHT_GRAY); // line number 배경 색
		lines.setEditable(false);  // line number 이므로 편집(수정) 불가
		
		// 문서의 내용이 변경되었을 때의 이벤트 설정
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
			public void insertUpdate(DocumentEvent de) {  // 라인 추가될 때
				lines.setText(getText());  // 추가된 만큼 line number 재설정
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {  // 라인 사라질 때
				lines.setText(getText());  // 사라진 만큼 line number 재설정
			}
 
		}); // end DocumentListener
		
		// viewport: 기본이 되는 정보를 보기 위해 사용하는 뷰포트(창)
		scrollPane.getViewport().add(textArea);
		scrollPane.setRowHeaderView(lines);  // 행 헤더로 추가
		// 항상 스크롤바가 보이도록 설정
		// setVerticalScrollBarPolicy: 수직방향의 정책 설정 혹은 읽기
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
		frame.add(scrollPane);
		
		// textArea에서 tab키를 누르면 이벤트 발생 (서버로 데이터 전달)
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
					
					// 확인용 출력, popupmenu에 추가
					for(int i = 1; i < list.size(); i++) {
						// popupmenu에 문자열 추가
						JMenuItem menuitem = new JMenuItem(list.get(i));
						
						// 문자열 "..." 위치 추출
						int setcursor = list.get(i).indexOf("...");
						
						// ...이 있으면 처음 ... 위치로 커서 위치 변경
						cursorList.add(setcursor);
						
						if(setcursor != -1) {
							// list에 있는 ... 문자열 제거
							list.set(i, list.get(i).replace("...", ""));
						}
						
						int stridx = list.get(i).length();
						
						// 공백이 있으면 공백 앞 문자열 추출, 없으면 문자열 그대로 추출
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

			// 키를 눌렀다 뗐을 때의 동작 정의
			@Override
			public void keyReleased(KeyEvent e) {
				// 서버로부터 받은 문자열을 popup menu로 출력
				int lineNum = 1;
		        int columnNum = 0;
				int keyCode = e.getKeyCode();
				if( isReceive && (keyCode == KeyEvent.VK_TAB) ) {
					// 커서 위치를 원래 위치로 변경
					try {
						// tab키로 인한 공백 제거
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
		
		frame.pack();  // JRrame의 내용물에 알맞게 윈도우 크기 조절
		frame.setSize(500,500);  // 윈도우 창 크기 지정
		frame.setVisible(true);  // 윈도우 창 디스플레이
 
	}
	
}