package general;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;

public class Knoten {

	private LinkedList<Knoten> children;
	private PacmanTileType[][] welt;
	private Vector2 pacPos, ghostPos;
	private Knoten parent;
	private double heuristikKosten = 0.0;
	private int kantenKosten = 0;

	public Knoten(Knoten parent, PacmanTileType[][] welt, Vector2 pacman, Vector2 hunter) {
		this.parent = parent;
		this.pacPos = pacman;
		this.ghostPos = hunter;
		this.welt = welt;
		children = new LinkedList<Knoten>();
	}

	public List<Knoten> expand() {

		// Left
		if (welt[ghostPos.x - 1][ghostPos.y] != PacmanTileType.WALL) {
			children.add(new Knoten(this, welt, pacPos, new Vector2(ghostPos.x - 1, ghostPos.y)));
		}

		// UP
		if (welt[ghostPos.x][ghostPos.y - 1] != PacmanTileType.WALL) {
			children.add(new Knoten(this, welt, pacPos, new Vector2(ghostPos.x, ghostPos.y - 1)));
		}

		// Right
		if (welt[ghostPos.x + 1][ghostPos.y] != PacmanTileType.WALL) {
			children.add(new Knoten(this, welt, pacPos, new Vector2(ghostPos.x + 1, ghostPos.y)));
		}

		// Down
		if (welt[ghostPos.x][ghostPos.y + 1] != PacmanTileType.WALL) {
			children.add(new Knoten(this, welt, pacPos, new Vector2(ghostPos.x, ghostPos.y + 1)));
		}

		return children;
	}

	//Prüfen ob der Ghost den Pacman erreicht hat
	public boolean isGoal() {
		return ghostPos.equals(pacPos);
	}
	
	//
	public boolean isGoal(Vector2 pos) {
		return ghostPos.equals(pos);
	}

	//Luftlinie zwischen Ghost und Pacman
	public double heuristikberechnen() {
		return Math.sqrt( ((ghostPos.x - pacPos.x) * (ghostPos.x - pacPos.x)) + ((ghostPos.y - pacPos.y) * (ghostPos.y - pacPos.y)) );
	}
	
	public int kantenkostenberechnen() {
		return parent == null ? 1 : parent.getKantenkosten() + 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ghostPos.x;
		result = prime * result + ghostPos.y;
		result = prime * result + pacPos.x;
		result = prime * result * pacPos.y;
		result = prime * result + Arrays.deepHashCode(welt);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Knoten other = (Knoten) obj;
		if (pacPos.x != other.pacPos.x)
			return false;
		if (pacPos.y != other.pacPos.y)
			return false;
		if (ghostPos.x != other.ghostPos.x)
			return false;
		if (ghostPos.y != other.ghostPos.y)
			return false;
		if (!Arrays.deepEquals(welt, other.welt))
			return false;
		return true;
	}
	
	public Knoten getParent() {
		return parent;
	}

	public void setParent(Knoten neu) {
		parent = neu;
	}

	public double getHeuristikkosten() {
		return heuristikKosten;
	}

	public void setHeuristikkosten(double d) { 
		this.heuristikKosten = d;
	}

	public int getKantenkosten() {
		return kantenKosten;
	}

	public void setKantenkosten(int kantenKosten) {
		this.kantenKosten = kantenKosten;
	}

	public int getPacPos_x() {
		return this.pacPos.x;
	}

	public int getPacPos_y() {
		return this.pacPos.y;
	}
	
	public Vector2 getPacPos() {
		return this.pacPos;
	}
	
	public Vector2 getGhostPos() {
		return this.ghostPos;
	}

	public PacmanTileType[][] getWelt() {
		return welt;
	}

}
