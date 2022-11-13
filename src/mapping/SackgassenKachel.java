package mapping;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;

public class SackgassenKachel {

	private int schritte = 0;
	private PacmanAction directionOut;
	private boolean entrance = false;		//Markierung für einen Sackgasseneingang: erstes Feld mit 2 Wänden
	private boolean finished = false;		//Markierung für eine abgeschlossene Sackgasse
	private boolean betreten = false;		//Markierung dafür ob eine Sackgasse schon betreten wurde
	private Vector2 pos;
	private SackgassenKachel parent;

	//Konstruktor für eine Sackgasse
	public SackgassenKachel(SackgassenKachel parent, Vector2 pos, int schritte, PacmanAction direction) {
		this.schritte = schritte;
		this.directionOut = direction;
		this.pos = pos;
		this.parent = parent;
	}
	
	//Konstruktor für einen SackgassenEntrance
	public SackgassenKachel(SackgassenKachel parent, Vector2 pos, int schritte, PacmanAction direction, boolean entrance) {
		this.schritte = schritte;
		this.directionOut = direction;
		this.entrance = true;
		this.pos = pos;
		this.parent = parent;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SackgassenKachel other = (SackgassenKachel) obj;
		if(schritte != other.schritte)
			return false;
		if(!directionOut.equals(other.directionOut))
			return false;
		if(entrance != other.entrance)
			return false;
		if(finished != other.finished)
			return false;
		if(!pos.equals(other.pos))
			return false;
		return true;
	}
	
	public String toString() {
		return schritte + " " + directionOut + " " + pos;
	}

	public int getSchritte() {return schritte;}
	public PacmanAction getAction() {return directionOut;}
	public boolean isEntrance() {return entrance;}
	public void setEntrance() {this.entrance = true;}
	public Vector2 getPos() {return pos;}
	public boolean isFinished() {return finished;}
	public void setFinished() {this.finished = true;}
	public boolean betreten() { return betreten;}
	public void setBetreten() {this.betreten = true;}
	public SackgassenKachel getParent() {return parent;}
	public void setParent(SackgassenKachel parent) {this.parent = parent;}
}
