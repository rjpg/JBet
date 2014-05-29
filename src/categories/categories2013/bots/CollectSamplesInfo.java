package categories.categories2013.bots;

public class CollectSamplesInfo {

	int categoryId=0;
	int samplesCollected=0;
	String lastSampleEventDate="";
	
	
	
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getSamplesCollected() {
		return samplesCollected;
	}
	public void setAnotherSample(String eventName) {
		this.samplesCollected++;
		lastSampleEventDate=eventName;
	}
	
	public String getLastSampleEventDate() {
		return lastSampleEventDate;
	}
	
}
