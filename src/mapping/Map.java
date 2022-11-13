package mapping;

import java.util.ArrayList;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;

public class Map {

	private SackgassenKachel sackgassen[][];
	private ArrayList<SackgassenKachel> sackgassenEntrance;

	public Map(PacmanTileType[][] view) {
		this.sackgassen = new SackgassenKachel[view.length][view[0].length];
		sackgassenEntrance = new ArrayList<SackgassenKachel>();
		checkMap(view);
	}

	//durchläuft die View (ab (1,1)) vertikal und befüllt das Sackgassen-Array
	private void checkMap(PacmanTileType[][] view) {
		for (int x = 1; x < view.length - 1; x++) {
			for (int y = 1; y < view[x].length - 1; y++) {
				int walls = 0;
				PacmanAction a = null;
				if (view[x + 1][y] == PacmanTileType.WALL) {
					walls++;
				} else {
					a = PacmanAction.GO_EAST;
				}
				if (view[x - 1][y] == PacmanTileType.WALL) {
					walls++;
				} else {
					a = PacmanAction.GO_WEST;
				}
				if (view[x][y + 1] == PacmanTileType.WALL) {
					walls++;
				} else {
					a = PacmanAction.GO_SOUTH;
				}
				if (view[x][y - 1] == PacmanTileType.WALL) {
					walls++;
				} else {
					a = PacmanAction.GO_NORTH;
				}
				if (walls == 3) {
					if (view[x][y] != PacmanTileType.WALL) {
						sackgassen[x][y] = new SackgassenKachel(null, new Vector2(x, y), 1, a);

						switch (a) {
						case GO_EAST:
							generateSackgasse(x + 1, y, view, a);
							break;
						case GO_WEST:
							generateSackgasse(x - 1, y, view, a);
							break;
						case GO_SOUTH:
							generateSackgasse(x, y + 1, view, a);
							break;
						case GO_NORTH:
							generateSackgasse(x, y - 1, view, a);
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}

	//erstellt eine Sackgasse vom Ende (3 Walls) bis zum Anfang (Entrance)
	private void generateSackgasse(int x, int y, PacmanTileType[][] view, PacmanAction a) {
		int walls = 0;
		PacmanAction na = null;
		switch (a) {
		case GO_EAST:
			if (view[x + 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_EAST;
			}
			if (view[x][y + 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_SOUTH;
			}
			if (view[x][y - 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_NORTH;
			}
			break;
		case GO_NORTH:
			if (view[x + 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_EAST;
			}
			if (view[x][y - 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_NORTH;
			}
			if (view[x - 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_WEST;
			}
			break;
		case GO_SOUTH:
			if (view[x + 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_EAST;
			}
			if (view[x - 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_WEST;
			}
			if (view[x][y + 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_SOUTH;
			}
			break;
		case GO_WEST:
			if (view[x - 1][y] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_WEST;
			}
			if (view[x][y + 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_SOUTH;
			}
			if (view[x][y - 1] == PacmanTileType.WALL) {
				walls++;
			} else {
				na = PacmanAction.GO_NORTH;
			}
			break;
		default:
			break;
		}
		if (walls == 2) {
			switch (a) {
			case GO_EAST:
				this.sackgassen[x][y] = new SackgassenKachel(sackgassen[x - 1][y], new Vector2(x, y), sackgassen[x - 1][y].getSchritte() + 1, na);
				break;
			case GO_NORTH:
				this.sackgassen[x][y] = new SackgassenKachel(sackgassen[x][y + 1], new Vector2(x, y), sackgassen[x][y + 1].getSchritte() + 1, na);
				break;
			case GO_SOUTH:
				this.sackgassen[x][y] = new SackgassenKachel(sackgassen[x][y - 1], new Vector2(x, y), sackgassen[x][y - 1].getSchritte() + 1, na);
				break;
			case GO_WEST:
				this.sackgassen[x][y] = new SackgassenKachel(sackgassen[x + 1][y],new Vector2(x, y), sackgassen[x + 1][y].getSchritte() + 1, na);
				break;
			default:
				break;
			}
			switch(na) {
			case GO_EAST:
				generateSackgasse(x + 1, y, view, na);
				break;
			case GO_NORTH:
				generateSackgasse(x, y - 1, view, na);
				break;
			case GO_SOUTH:
				generateSackgasse(x, y + 1, view, na);
				break;
			case GO_WEST:
				generateSackgasse(x - 1, y, view, na);
				break;
			default:
				break;
			
			}
		} else {
			switch (a) {
			case GO_EAST:
				this.sackgassen[x - 1][y].setEntrance();
				sackgassenEntrance.add(this.sackgassen[x - 1][y]);
				return;
			case GO_NORTH:
				this.sackgassen[x][y + 1].setEntrance();
				sackgassenEntrance.add(this.sackgassen[x][y + 1]);
				return;
			case GO_SOUTH:
				this.sackgassen[x][y - 1].setEntrance();
				sackgassenEntrance.add(this.sackgassen[x][y - 1]);
				return;
			case GO_WEST:
				this.sackgassen[x + 1][y].setEntrance();
				sackgassenEntrance.add(this.sackgassen[x + 1][y]);
				break;
			default:
				return;
			}
		}
	}

	public boolean isSackgassenkachelVec(Vector2 vec) {
        return sackgassen[vec.getX()][vec.getY()] != null;
    }
	
	public boolean isSackgassenkachelInts(int x, int y) {
        return sackgassen[x][y] != null;
    }

	public SackgassenKachel[][] getSackgassen() {
		return this.sackgassen;
	}
	
	public ArrayList<SackgassenKachel> getSackgassenEntrance() {return sackgassenEntrance;}
}