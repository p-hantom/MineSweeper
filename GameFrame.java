import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class Cell{
	public boolean isCovered;
	public boolean isMine;
	public boolean isFlag;
	public boolean showMineWhenSuccess;
	public boolean showHint;
	public int mineAroundNum;
	
	Cell(){
		isMine=false;
		isFlag=false;
		isCovered=true;
		mineAroundNum=0;
		showMineWhenSuccess=false;
		showHint=false;
	}
	
}

public class GameFrame extends JFrame implements MouseListener {
	private int row,col;
	private int mineNum;
	private int level;
	private int diggedNum,numOfHint;
	private int windowLen,windowWidth;
	private int offsetFaceX,offsetCandyX;
	private boolean lose;
	private boolean gameEnd;
	private boolean showHint;
	private final int BLOCKWIDTH=20;
	private final int OFFSET_X=10,OFFSET_Y=40;
	private final int OFFSET_FACE_Y=8;
	private final int MOUSE_OFFSET_X=7,MOUSE_OFFSET_Y=54;
	private final int DIGIT_OFFSET=5; //used when drawing number
	private final int BORDER_OFFSET=3;
	private final int[] LEV_ROWS= {8,10,15};
	private final int PICSIZE=23;
	private Cell[][] cells;	
	
	private MinePanel minePanel;
	
	public GameFrame(){		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		
		level=1;
		newGame(level);
		setMenu();
		
		addMouseListener(this);	
	}
	
	void newGame(int level) {
		row=LEV_ROWS[level-1];
		col=row;
		windowLen=BLOCKWIDTH*(col+6);
		windowWidth=BLOCKWIDTH*(row+2);		
		setSize(windowWidth,windowLen);
		offsetFaceX=windowWidth/2-BLOCKWIDTH;
		offsetCandyX=offsetFaceX+2*BLOCKWIDTH;
		
		//initializing cells
		this.cells=new Cell[row][col];
		for(int i=0;i<row;i++) {
			for(int j=0;j<col;j++) {
				cells[i][j]=new Cell();
			}
		}
		
		showHint=false;
		numOfHint=0;
		gameEnd=false;
		this.lose=false;
		diggedNum=0;
		setMines(level);
		setMineAroundNum();
		minePanel=new MinePanel();		
		add(minePanel);
		
	}

	void setMenu() {
		//System.out.println("menu");
		JMenuBar menuBar=new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu1 = new JMenu("level");
		menuBar.add(menu1);
		JMenuItem item1 = new JMenuItem("easy");
		JMenuItem item2 = new JMenuItem("medium");
		JMenuItem item3 = new JMenuItem("hard");
		menu1.add(item1);
		menu1.add(item2);
		menu1.add(item3);
		
		item1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				level=1;
				newGame(level);
				repaint();
			}
		});
		
		item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				level=2;
				newGame(level);
				repaint();
			}
		});
		
		item3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				level=3;
				newGame(level);
				repaint();
			}
		});
	}
	
	boolean inRange(int px,int py) {
		if(px>=0 && px<row && py>=0 && py<col) {
			return true;
		}
		return false;
	}
	
	boolean isSuccess() {
		return (row*col-diggedNum)==mineNum;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//if clicked on the smiling face
		int x=e.getX(),y=e.getY();
		int fx=x-MOUSE_OFFSET_X,fy=y-MOUSE_OFFSET_Y;
		if(fx>=offsetFaceX && fx<=offsetFaceX+PICSIZE && fy>=OFFSET_FACE_Y && fy<=OFFSET_FACE_Y+PICSIZE) {
			gameEnd=true;
			newGame(level);
			repaint();
			return;
		}
		
		if(gameEnd) {
			return;
		}
		//if not clicked on the smiling face
		
//		if(showHint) { 
//			showHint=false;
//		}		
//		if(numOfHint==0 && fx>=offsetCandyX && fx<=offsetCandyX+PICSIZE && fy>=OFFSET_FACE_Y && fy<=OFFSET_FACE_Y+PICSIZE) {
//			System.out.println("hint");
//			showHint=true;
//			numOfHint++;
//		}
		
		int px,py;
		px=(x-OFFSET_X-MOUSE_OFFSET_X)/BLOCKWIDTH;
		py=(y-OFFSET_Y-MOUSE_OFFSET_Y)/BLOCKWIDTH;  //coordination of cells
		
		if(inRange(px,py)) { //if clicked within range	
			if(e.getButton() == MouseEvent.BUTTON3) {  //right-click
				cells[px][py].isFlag=!cells[px][py].isFlag;
			}
			else {  //left-click
				if(cells[px][py].isCovered) {
					if(cells[px][py].isMine) {
						this.lose=true;
					}
					else {
						dig(px,py);
					}					
				}
			}
						
			if(lose) {
				gameEnd=true;
				JOptionPane.showMessageDialog(this, "You lose! Click the face to restart.");
				setShowLose();
			}
			if(isSuccess()) {
				gameEnd=true;
				JOptionPane.showMessageDialog(this, "You win! Click the smiling face to restart.");
				setShowSuccess();
			}
			repaint();
		}
	}
	
	void setShowSuccess() {
		for(int i=0;i<row;i++) {
			for(int j=0;j<col;j++) {
				if(cells[i][j].isMine) {
					cells[i][j].showMineWhenSuccess=true;
				}
			}
		}
	}
	
	void setShowLose() {
		for(int i=0;i<row;i++) {
			for(int j=0;j<col;j++) {
				if(cells[i][j].isMine) {
					cells[i][j].isCovered=false;
				}
			}
		}
	}
	
	void dig(int px,int py) {
		if(!cells[px][py].isCovered||cells[px][py].isFlag) { //flag
			return;
		}
		if(!cells[px][py].isMine && cells[px][py].mineAroundNum!=0) { //dig the cell of number
			diggedNum++;
			cells[px][py].isCovered=false;
			return;
		}
		diggedNum++;
		cells[px][py].isCovered=false;
		for(int i=-1;i<=1;i++) {
			for(int j=-1;j<=1;j++) {
				int tx=px+i,ty=py+j;
				if((i==0&&j==0) || !inRange(tx,ty)) {
					continue;
				}
				dig(tx,ty);
			}
		}
	}
	
	void setMines(int level) {
		Random rand=new Random();
		int[][] book=new int[row][row];
		int i=0;
		switch(level) {
			case 1:mineNum=10;break;
			case 2:mineNum=20;break;
			case 3:mineNum=40;break;
		}
			
		int randR,randC;
		while(i<mineNum) {
			randR=rand.nextInt(row);
			randC=rand.nextInt(row);
			if(book[randR][randC]==1) { //if already set
				continue;
			}
			cells[randR][randC].isMine=true;
			book[randR][randC]=1;
			i++;
		}
	}
	
	void setMineAroundNum() {		
		for(int i=0;i<row;i++) {  //traversing every cell
			for(int j=0;j<col;j++) {				
				if(cells[i][j].isMine) {
					continue;
				}
				//counting mines around
				for(int m=-1;m<=1;m++) {
					for(int n=-1;n<=1;n++) {
						int tx=m+i,ty=n+j;
						if((m==0&&n==0) || !inRange(tx,ty)) {
							continue;
						}
						if(cells[tx][ty].isMine) {
							cells[i][j].mineAroundNum++;							
						}								
					}
				}				
			}
		}
	}
	
	class MinePanel extends JPanel{	
		private Image img[];
		
		MinePanel(){
			img=new Image[10];
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			img[0]=new ImageIcon("res\\covered.png").getImage();
			img[1]=new ImageIcon("res\\mine.png").getImage();
			img[2]=new ImageIcon("res\\flag3.png").getImage();			
			img[3]=new ImageIcon("res\\flag2.png").getImage();			
			
			Image smileFace=new ImageIcon("res\\smile.png").getImage();
			g.drawImage(smileFace,offsetFaceX,OFFSET_FACE_Y,PICSIZE,PICSIZE,this);
			
//			if(numOfHint==0) {
//				Image candy=new ImageIcon("res\\candy.png").getImage();
//				g.drawImage(candy, offsetCandyX, OFFSET_FACE_Y,PICSIZE,PICSIZE,this);
//			}
						
			g.drawRect(OFFSET_X, OFFSET_Y, BLOCKWIDTH*row+BORDER_OFFSET, BLOCKWIDTH*col+BORDER_OFFSET);
			
			for(int i=0;i<row;i++) {
				for(int j=0;j<col;j++) {
					int picType=0;
					boolean drawPic=true;
					int px=OFFSET_X+i*BLOCKWIDTH;
					int py=OFFSET_Y+j*BLOCKWIDTH;
					
					if(cells[i][j].showMineWhenSuccess) {  //show the undigged mines with candles
						picType=3;
					}
					else if(cells[i][j].isCovered) {  //not digged
						if(cells[i][j].isFlag) {
							picType=2; //flag
						}
						else {
							picType=0;  //covered
						}					
					}
					else {  //digged
						if(cells[i][j].isMine) {  //mine
							picType=1; 
						}
						else {  //not mine
							if(cells[i][j].mineAroundNum==0) {
								drawPic=false;  //blank
							}
							else if(cells[i][j].mineAroundNum!=0) {
								drawPic=false;  //number
								g.drawString(Integer.toString(cells[i][j].mineAroundNum), px+BLOCKWIDTH/2, py+BLOCKWIDTH/2+DIGIT_OFFSET);
							}
						}										
					}	
					
					if(drawPic) {
						g.drawImage(img[picType],px,py,PICSIZE,PICSIZE,this);
					}					
				}
			}
		}		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}

