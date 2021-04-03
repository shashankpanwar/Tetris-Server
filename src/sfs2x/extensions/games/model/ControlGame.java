/**
 * 
 */
package sfs2x.extensions.games.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Shashank Panwar
 *
 */
@Entity
@Table(name = "control")
public class ControlGame {

	@Id
	@Column(name = "idcontrol")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "taskStatus",columnDefinition="tinyint(1)")
	private int status;

	@Column(name = "gameName")
	private String gameName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	
}
