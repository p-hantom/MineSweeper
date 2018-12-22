import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Rank {
	File[] rankFile;
	String rankRes;
	String res;
	PriorityQueue<Integer> pq;
	final int TOP=5;
	final String[] postfix= {"st","nd","rd","th"};
	
	Rank(){
		rankFile=new File[3];
		rankFile[0]=new File("res/rank_easy.txt");
		rankFile[1]=new File("res/rank_medium.txt");
		rankFile[2]=new File("res/rank_hard.txt");
		pq=new PriorityQueue<Integer>();
	}
	
	//write data in a file instead of insert in the queue directly,as
	//the data won't be cleared after exiting the game
	public void writeTimeData(int time,int level) throws IOException {
		PrintWriter out=new PrintWriter(new FileWriter(rankFile[level-1],true));
		out.append(Integer.toString(time)+" ");
		out.close();
	}
	
	//clear the queue and insert from the beginning for the same reason above
	//(Maybe there's a better way...)
	void readTimeData(int level) throws IOException {
		pq.clear();
		Scanner input=new Scanner(rankFile[level-1]);
		while(input.hasNextInt()) {
			int next=input.nextInt();
			pq.add(next);
		}
		
		input.close();
	}
	
	void writeRankData(int time) {
		int i=1,thisRank=0;
		while(!pq.isEmpty() && i<=TOP) {
			int tmp=pq.peek();
			if(tmp==time) {
				thisRank=i;
			}
			String postfix1;
			if(i>3) {
				postfix1=postfix[3];
			}
			else {
				postfix1=postfix[i-1];
			}
			
			rankRes+=i+postfix1+" : "+tmp+"s\r\n";
			pq.remove();
			i++;
		}
		res="Your rank is No."+thisRank+".\r\n";
	}
	
	String getRankRes() {
		return rankRes;
	}
	
	String getRes() {
		return res;
	}
	
	void rank(int time,int level) throws IOException {
		rankRes="";
		writeTimeData(time,level);
		readTimeData(level);
		writeRankData(time);
	}

}
