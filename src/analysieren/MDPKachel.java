package analysieren;

import de.fh.pacman.enums.PacmanAction;
import exception.NoWayException;

public class MDPKachel {
	
	private double north, south, east, west, erg, ergVorherigeRunde;
	private boolean isWall;
	private PacmanAction move;
	
	public MDPKachel() {
		this.north = 0.0;
		this.south = 0.0;
		this.east = 0.0;
		this.west = 0.0;
		this.ergVorherigeRunde = 0.0;
		this.erg = 0.0;
		this.move = PacmanAction.WAIT;
		this.isWall = false;
	}
	
	//findet den größten Wert und setzt die PacmanAction (bei einem RH Spiel)
	public double findErg(boolean[] walls) throws NoWayException{
		double[] ergebnisse = { this.north, this.south, this.west, this.east };
		int max = 0;
		
		//Wände bekommen eine schlecht Bewertung, damit der Pacman nicht hineinläuft
		for(int i = 0; i < ergebnisse.length; i++)
			if(walls[i] == true)
				ergebnisse[i] = -10;

		for (int i = 0; i < ergebnisse.length; i++)
			if (ergebnisse[i] >= ergebnisse[max])
				max = i;
				
		switch (max) {
			case 0:
				move = PacmanAction.GO_NORTH;
				break;
			case 1:
				move = PacmanAction.GO_SOUTH;
				break;
			case 2:
				move = PacmanAction.GO_WEST;
				break;
			case 3:
				move = PacmanAction.GO_EAST;
				break;
			default:
				throw new NoWayException();
		}

		return ergVorherigeRunde = ergebnisse[max];
	}
	
	public double getNorth() {
		return north;
	}

	public void setNorth(double north) {
		this.north = north;
	}

	public double getSouth() {
		return south;
	}

	public void setSouth(double south) {
		this.south = south;
	}

	public double getEast() {
		return east;
	}

	public void setEast(double east) {
		this.east = east;
	}

	public double getWest() {
		return west;
	}

	public void setWest(double west) {
		this.west = west;
	}
	
	public double getErg() {
		return erg;
	}
	
	public void setErg(double erg) {
		this.erg = erg;
	}
	
	public double getErgVorherigeRunde() {
		return ergVorherigeRunde;
	}
	
	public void setErgVorherigeRunde(double erg) {
		this.ergVorherigeRunde = erg;
	}
	
	public PacmanAction getAction() {
		return move;
	}
	
	public void setAction(PacmanAction new_move) {
		this.move = new_move;
	}
	
	public boolean isWall() {
		return isWall;
	}

	public void setWall(boolean isWall) {
		this.isWall = isWall;
	}
	
}
