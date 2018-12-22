import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

public class GameFrame extends JFrame implements MouseListener {
	private int row,col;
	private int mineNum,leftToSweep;
	private int level;
	private int diggedNum,numOfHint;
	private int windowLen,windowWidth;
	private int offsetFaceX,offsetCandyX,offsetNumX,offsetTimerX,offsetTimerY;
	private boolean lose;
	private boolean gameEnd,gameStart;  //gameStart==true after the first click on board
	private boolean showHint,showCandy;
	private final Object[] winDialogBtn={"OK","Show more"};
	private final int BLOCKWIDTH=20;
	private final int OFFSET_X=20,OFFSET_Y=50;
	private final int OFFSET_FACE_Y=8;
	private final int MOUSE_OFFSET_X=7,MOUSE_OFFSET_Y=54;
	private final int DIGIT_OFFSET=5; //used when drawing number
	private final int BORDER_OFFSET=3;
	private final int[] LEV_ROWS= {8,10,15};
	private final int PICSIZE=23;
	private Cell[][] cells;	
	
	private int timeLmt;
	private ClockListener cl;
	private Timer timer;
	
	private MinePanel minePanel;
	
	private Rank rank;
	
	public GameFrame(){		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		
		rank=new Rank();
		
		cl=new ClockListener();
		timer=new Timer(1000,cl);
		
		level=1;
		newGame(level);
		setMenu();
						
		addMouseListener(this);			
	}
	
	void initInfo() {
		row=LEV_ROWS[level-1];
		col=row;
		windowLen=BLOCKWIDTH*(col+8);
		windowWidth=BLOCKWIDTH*(row+3);		
		setSize(windowWidth,windowLen);
		offsetFaceX=windowWidth/2-BLOCKWIDTH;
		offsetCandyX=offsetFaceX+2*BLOCKWIDTH;
		offsetNumX=offsetFaceX-2*BLOCKWIDTH;
		offsetTimerX=BLOCKWIDTH*(col-2);
		offsetTimerY=BLOCKWIDTH*(row+4);
		
		//initializing cells
		this.cells=new Cell[row][col];
		for(int i=0;i<row;i++) {
			for(int j=0;j<col;j++) {
				cells[i][j]=new Cell();
			}
		}
		
		showCandy=true;
		showHint=false;
		numOfHint=0;
		
		gameStart=false;
		gameEnd=false;
		this.lose=false;
				
		diggedNum=0;
		
		setMines(level);
		leftToSweep=mineNum;
		setMineAroundNum();
		minePanel=new MinePanel();		
		add(minePanel);
	}
	
	void newGame(int level) {
		initInfo();	
	}

	void setMenu() {
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
		//-if clicked on the smiling face
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
		
		//-if not clicked on the smiling face
		//--if clicked on the candy
		if(showHint) { 
			showHint=false;
		}		
		if(numOfHint==0 && fx>=offsetCandyX && fx<=offsetCandyX+PICSIZE && fy>=OFFSET_FACE_Y && fy<=OFFSET_FACE_Y+PICSIZE) {
			showHint=true;
			showCandy=false;
			numOfHint++;
			repaint();
		}
		
		//--clicked on cells	
		if(gameStart==false) {
			gameStart=true;
			cl.setX();
			timer.start();
		}
		
		int px,py;
		px=(x-OFFSET_X-MOUSE_OFFSET_X)/BLOCKWIDTH;
		py=(y-OFFSET_Y-MOUSE_OFFSET_Y)/BLOCKWIDTH;  //coordination of cells
		
		if(inRange(px,py)) { //if clicked within range	
			if(e.getButton() == MouseEvent.BUTTON3) {  //right-click
				cells[px][py].isFlag=!cells[px][py].isFlag;
				if(cells[px][py].isFlag && cells[px][py].isMine) {
					leftToSweep--;
				}
				else if(!cells[px][py].isFlag && cells[px][py].isMine) {
					leftToSweep++;
				}
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
				timer.stop();
				cl.time=0;
				gameEnd=true;						
				
				setShowLose();
			}
			if(isSuccess()) {
				timer.stop();
				
				try {
					//write the time spent
					rank.rank(this.timeLmt/1000-(int)cl.getX()/1000,level);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				int choice=JOptionPane.showOptionDialog(this,"You win! "+rank.getRes()+"Click the smiling face to restart.", "",JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE,null,winDialogBtn,winDialogBtn[0]);
				if(choice==0) {					
				}
				else {
					JOptionPane.showMessageDialog(null, rank.getRankRes(),"Record",JOptionPane.PLAIN_MESSAGE);
				}
				
				gameEnd=true;
				
				setShowSuccess();
				leftToSweep=0;
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
			case 1:mineNum=10;timeLmt=50000;break;
			case 2:mineNum=20;timeLmt=80000;break;
			case 3:mineNum=40;timeLmt=160000;break;
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
		private final Color color[]= {new Color(11, 83, 185),new Color(11, 185, 47),new Color(189, 42, 10),
				new Color(228, 5, 80),new Color(250, 148, 4),new Color(250, 25, 153),new Color(4, 216, 178)};
		
		MinePanel(){
			img=new Image[10];
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			img[0]=new ImageIcon("res\\covered.png").getImage();
			img[1]=new ImageIcon("res\\mine.png").getImage();
			img[2]=new ImageIcon("res\\flag3.png").getImage();			
			img[3]=new ImageIcon("res\\flag2.png").getImage();
			img[4]=new ImageIcon("res\\hint2.png").getImage();
			
			//show face
			Image smileFace=new ImageIcon("res\\smile.png").getImage();
			Image cryFace=new ImageIcon("res\\dead.png").getImage();
			if(lose) {
				g.drawImage(cryFace,offsetFaceX,OFFSET_FACE_Y,PICSIZE,PICSIZE,this);
			}
			else {
				g.drawImage(smileFace,offsetFaceX,OFFSET_FACE_Y,PICSIZE,PICSIZE,this);
			}
			
			//show candy
			if(showCandy) {
				Image candy=new ImageIcon("res\\candy.png").getImage();
				g.drawImage(candy, offsetCandyX, OFFSET_FACE_Y,PICSIZE,PICSIZE,this);
			}
			
			//border
			g.setColor(Color.black);
			g.drawRect(OFFSET_X, OFFSET_Y, BLOCKWIDTH*row+BORDER_OFFSET, BLOCKWIDTH*col+BORDER_OFFSET);
			
			//time
			g.setFont(new Font("Purisa", Font.PLAIN, 13));
			g.drawString("Timer: "+Integer.toString((int) cl.time/1000), offsetTimerX, offsetTimerY);
			
			//show leftToSweep
			g.drawImage(img[1], offsetNumX-BLOCKWIDTH-3, OFFSET_FACE_Y, PICSIZE,PICSIZE,this);
			g.drawString(Integer.toString(leftToSweep), offsetNumX, OFFSET_FACE_Y+BLOCKWIDTH/2+DIGIT_OFFSET);
			
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
							if(showHint && cells[i][j].isMine) { //show hint
								picType=4;
							}
							else {
								picType=0;  //covered
							}
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
								g.setFont(new Font("Purisa", Font.PLAIN, 13));
								g.setColor(color[cells[i][j].mineAroundNum-1]);
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
	
	class ClockListener implements ActionListener{
		long time;
		long getX() {
			return time;
		}
		void setX() {
			time=timeLmt;
		}
		boolean ifTimeIsUp() {
			if(time==0&&gameEnd==false) {
				return true;
			}
			return false;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			time-=1000;
			if(ifTimeIsUp()) {
				gameEnd=true;
				setShowLose();
				lose=true;
				timer.stop();
			}		
				
			repaint();
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

