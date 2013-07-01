package hr.fer.hmo.solution;

/**
 * Čvor odnosno vrh na grafu.
 */
public class Vertex {
	
	/** Identifikator čvora */
	public int id;
	/** Koordinata X */
	public int x;
	/** Koordinata Y */
	public int y;
	/** Kapacitet čvora */
	public int capacity;
	/** Vrijeme početka obrade - početak vremenskog prozora */
	public int startTime;
	/** Vrijeme kraja obrade - kraj vremenskog prozora */
	public int endTime;
	/** Vrijeme trajanja usluge */
	public int serviceTime;
	
	// Naziv grada
	public String name;
	
	/**
	 * Konstruktor. Ime se postavlja na null.
	 * @param x x koordinata
	 * @param y y koordinata
	 */
	public Vertex(int x, int y) {
		this(null, x, y);
	}

	/**
	 * Konstruktor.
	 * @param name ime grada
	 * @param x x koordinata
	 * @param y y koordinata
	 */
	public Vertex(String name, int x, int y) {
		super();
		this.name = name;
		this.x = x;
		this.y = y;
	}
	
	public Vertex(int id, int x, int y, int capacity, int startTime,
			int endTime, int serviceTime) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.capacity = capacity;
		this.startTime = startTime;
		this.endTime = endTime;
		this.serviceTime = serviceTime;
	}

	@Override
	public String toString() {
		return "Vertex [id=" + id + ", x=" + x + ", y=" + y + ", capacity="
				+ capacity + ", startTime=" + startTime + ", endTime="
				+ endTime + ", serviceTime=" + serviceTime + "]";
	}
	
	
}
