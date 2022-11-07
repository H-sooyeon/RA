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
	private JTextArea textArea;  // 텍스트 편집 영역, 여러 줄을 입력할 수 있는 컴포넌트(자체 스크롤 없음)
	private JFileChooser fileChooser = new JFileChooser();  // 파일 선택 열기
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
		initialize();  // 내용 초기화
	}
	
	private void initialize() {
		frame = new JFrame();  // 타이틀이 없는 프레임 생성
		frame.setBounds(100,100,450,300);  // 위치, 너비, 높이 지정(x,y,w,h)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 종료 이벤트 처리
		
		JMenuBar menuBar = new JMenuBar();  // 메뉴바 객체 생성
		frame.setJMenuBar(menuBar);  // 프레임에 메뉴바 적용
		
		JMenu mnFile = new JMenu("File");  // 메뉴 표시줄에 보이게 될 File 메뉴 생성
		menuBar.add(mnFile);  // 메뉴바에 File 메뉴 추가
		
		JMenuItem mntmNew = new JMenuItem("New");  // 메뉴에 포함될 세부 메뉴 생성
		
		/*
		 * New 메뉴가 선택되면 실행할 이벤트 정의
		 * 파일로부터 불러오거나 파일에 저장할 수 있도록 선택 Dialog 생성
		 */
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval = fileChooser.showDialog(frame, "New");  // Dialog 생성
				if(retval == JFileChooser.APPROVE_OPTION) {  // 예 확인을 선택했을 때 리턴되는 값
					file = fileChooser.getSelectedFile();  // 생성한 파일의 경로를 반환, file에 저장
					textArea.setText("");  // 공백으로 초기화
				}
			}
		});
		
		mnFile.add(mntmNew);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 New 적용
		
		JMenuItem mntmOpen = new JMenuItem("Open");  // 메뉴에 포함될 세부 메뉴 생성
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dialog 창에서 열기가 정상적으로 수행된 경우 0을 반환, 취소를 누른 경우 1을 반환
				int retval = fileChooser.showOpenDialog(frame);  // 열기용 창 오픈
				
				// 파일이 잘 열렸을 경우
				if(retval == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();  // 선택한 파일의 경로 저장
					// 선택한 파일을 읽어
					// JTextArea에 추가
					// 검사 예외 처리하는 코드 작성
					try {
						FileReader fr = new FileReader(file);  // 지정한 파일로부터 읽어올 객체 생성
						BufferedReader br = new BufferedReader(fr);  // 버퍼를 이용해서 사용(효율적)
						
						String str = "";
						String textLine = "";
						
						textLine = br.readLine();  // BufferedReader가 가지고 있는 문자열 한 줄 읽기
						
						// BufferedLine의 readLine은 읽을 데이터가 없을 때 null 리턴
						while((str = br.readLine()) != null) {
							textLine = textLine + "\n" + str;  // 한 줄씩 끊어서 저장
						}
						textArea.setText(textLine);  // 지정한 파일의 text를 textArea로 set
					} catch(Exception e) {
						JOptionPane.showMessageDialog(frame, e.getMessage());  //예외 처리
					}
				} else {
					// 파일이 제대로 열리지 않은 경우 or 그냥 닫은 경우
					// Not working 메시지 띄우기
					JOptionPane.showMessageDialog(frame, "User canced the operation");
				}
			}
		});
		mnFile.add(mntmOpen);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Open 적용
		
		JMenuItem mntmSave = new JMenuItem("Save");  // 메뉴에 포함될 세부 메뉴 생성
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 파일 객체가 있다면, 내용을 이전에 열었던 파일에 저장
				// JTextArea에 입력된(수정한) 내용을
				// 선택한 파일에 저장
				// 검사 예외 처리
				if(file != null) {
					// 선택한 파일에 저장
					try {
						FileWriter wr = new FileWriter(file, false);  // 문자 스트림을 이용하여 file 객체 생성
						BufferedWriter bw = new BufferedWriter(wr);
						bw.write(textArea.getText());  // 파일에 버퍼를 통해서 text 저장(효율적)
						bw.flush();  // 버퍼에 남은 text 전송
						bw.close();  // 닫기
					} catch(Exception e) {
						// 예외 처리
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}	
			}
		});
		mnFile.add(mntmSave);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Save 적용
		
		JMenuItem mntmClose = new JMenuItem("Close");  // 메뉴에 포함될 세부 메뉴 생성
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 텍스트 편집 영역을 공백으로 초기화하고, 현재 선택된 파일을 초기화
				file = null;  // 현재 선택된 파일 초기화
				// JTextArea를 비움
				textArea.setText("");  // 공백으로 초기화
			
			}
		});
		mnFile.add(mntmClose);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Close 적용
		
		JMenuItem mntmQuit = new JMenuItem("Quit");  // 메뉴에 포함될 세부 메뉴 생성
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 프로그램을 종료. System 클래스의 exit 메소드 사용.
				// 프로그램 종료
				System.exit(0);  // 정상 종료
			}
		});
		mnFile.add(mntmQuit);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Quit 적용
		
		JMenu mnAbout = new JMenu("About");  // 메뉴 표시줄에 보이게 될 About 메뉴 생성
		menuBar.add(mnAbout);  // 메뉴바에 About 메뉴 추가
		
		JMenuItem mntmAboutTextEditor = new JMenuItem("About Text Editor");  // About 메뉴에 포함할 세부 메뉴
		mntmAboutTextEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Text Editor Sample");  // 단순한 알림창을 띄우는 함수
			}
		});
		mnAbout.add(mntmAboutTextEditor);  // 이벤트 정의 후 About 메뉴에 세부 메뉴인 About Text Editor 추가
		// 컨텐츠 영역을 가져온다.
		// 컨텐츠 영역에 부착되는 구성요소들을 어떻게 배치할 것인지 정해준다.
		// x 방향으로 컴포넌트들을 배치
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
	
		// 텍스트 에디터에 스크롤 추가
		JScrollPane scrollPane = new JScrollPane();
		// frame에서 컨텐츠 영역을 가져온다.
		// 컨텐츠 영역에 scroll을 추가
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		// scrollpane에 text 추가(텍스트에 스크롤 추가)
		scrollPane.setViewportView(textArea);
		
		// JTextArea에 line number 추가
		lines = new JTextArea("1");  // 시작 번호
		lines.setBackground(Color.LIGHT_GRAY); // line number 배경 색
		lines.setEditable(false);  // line number 이므로 편집(수정) 불가
		
		// 문서의 내용이 변경되었을 때의 이벤트 설정
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
			public void insertUpdate(DocumentEvent de) {  // 라인 추가될 때
				lines.setText(getText());  // 추가된 만큼 line number 재설정
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {  // 라인 사라질 때
				lines.setText(getText());  // 사라진 만큼 line number 재설정
			}
 
		});
		// viewport: 기본이 되는 정보를 보기 위해 사용하는 뷰포트(창)
		scrollPane.getViewport().add(textArea);
		scrollPane.setRowHeaderView(lines);  // 행 헤더로 추가
		// 항상 스크롤바가 보이도록 설정
		// setVerticalScrollBarPolicy: 수직방향의 정책 설정 혹은 읽기
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
		frame.add(scrollPane);  // frame에 scollPane 객체 추가
		frame.pack();  // pack() : JRrame의 내용물에 알맞게 윈도우 크기 조절
		frame.setSize(500,500);  // 윈도우 창 크기 지정
		frame.setVisible(true);  // 윈도우 창 디스플레이
 
	}
	
}
