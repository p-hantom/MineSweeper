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