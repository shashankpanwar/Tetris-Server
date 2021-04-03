/**
 * 
 */
package sfs2x.extensions.games.resource;

/**
 * @author Shashank Panwar
 *
 */
public enum GameStakes {
     
	StakeFirst(1,100,"game1"),
	StakeSecond(2,200,"game2"),
	StakeThird(3,400,"game3"),
	StakeFourth(4,800,"game4"),
	StakeFifth(5,1600,"game5");
	private int id;
	private int stake;
	private String gameName;
	
	/**
	 * @param id
	 * @param stake
	 * @param gameName
	 */
	private GameStakes(int id, int stake, String gameName) {
		this.id = id;
		this.stake = stake;
		this.gameName = gameName;
	}
	
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getStake() {
		return stake;
	}
	public void setStake(int stake) {
		this.stake = stake;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	
}
