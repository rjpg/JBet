package categories.categories2011;

public class Category {
	
	public boolean time10m=true;
	public int oddCat=-1;
	public boolean favorite=false;
	public int am=-1;
	
	public int ahead=0;
	public int aheadOffset=0;

	public int windowSize=0;
	public int axisSize=0;
	public int axisSizeVolumeDiff=0;
	public int axisSizeVolume=0;
	public int axisSizeAmounts=0;
	
	public Category(boolean atime10m,int aoddCat,boolean fav,int aam)
	{
		time10m=atime10m;
		oddCat=aoddCat;
		favorite=fav;
		am=aam;
	}
	
	public boolean isTime10m() {
		return time10m;
	}

	public void setTime10m(boolean time10m) {
		this.time10m = time10m;
	}

	public int getOddCat() {
		return oddCat;
	}

	public void setOddCat(int oddCat) {
		this.oddCat = oddCat;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public int getAm() {
		return am;
	}

	public void setAm(int am) {
		this.am = am;
	}

	public int getAhead() {
		return ahead;
	}

	public void setAhead(int ahead) {
		this.ahead = ahead;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getAxisSize() {
		return axisSize;
	}

	public void setAxisSize(int axisSize) {
		this.axisSize = axisSize;
	}

	public int getAxisSizeVolume() {
		return axisSizeVolume;
	}

	public void setAxisSizeVolume(int axisSizeVolume) {
		this.axisSizeVolume = axisSizeVolume;
	}

	public int getAxisSizeAmounts() {
		return axisSizeAmounts;
	}

	public void setAxisSizeAmounts(int axisSizeAmounts) {
		this.axisSizeAmounts = axisSizeAmounts;
	}
	
	public int getAxisSizeVolumeDiff() {
		return axisSizeVolumeDiff;
	}

	public void setAxisSizeVolumeDiff(int axisSizeVolumeDiff) {
		this.axisSizeVolumeDiff = axisSizeVolumeDiff;
	}

	public int getNumberInputValues()
	{
		return (windowSize*((axisSize*2)+0))+(axisSizeVolumeDiff*2);//+/*(axisSizeAmounts*2)+*/(axisSizeVolume*2);
		//return (windowSize*((axisSize*2)+1))+(axisSizeVolumeDiff*2)+(axisSizeAmounts*2)+(axisSizeVolume*2);
	}
	
	public int getNumberInputValues2()
	{
		return (windowSize*((axisSize*2)+0))+(axisSizeVolumeDiff*2);//+/*(axisSizeAmounts*2)+*/(axisSizeVolume*2);
		//return (windowSize*((axisSize*2)+1))+(axisSizeVolumeDiff*2)+(axisSizeAmounts*2)+(axisSizeVolume*2);
	}
	
	public int getAheadOffset() {
		return aheadOffset;
	}

	public void setAheadOffset(int aheadOffset) {
		this.aheadOffset = aheadOffset;
	}

	
}
