package bsi.lars.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Diese Klasse verwaltet die Konfigurationsdaten. Momentan ist hier nur der Datenbankpfad.
 * Die Konfiguration wird immer von Datei gelesen, bzw. in Datei geschrieben.
 * 
 *
 */
public class Config {
	/**
	 * Schl�sselworte
	 */
	private static final String DB_PATH = "DB-PATH";
	private static final String PERCENTAGE = "PERCENTAGE";//TODO: remove
	private static final String OPENREPORT = "OPENREPORT";
	
	/**
	 * Konfigurationsdateiname
	 */
	private static final String CONFIG_XML = "config.xml";
	public static final boolean DEBUG = true;
	private static File configFile = new File(CONFIG_XML);
	
	/**
	 * Funktion, die den Wert f�r das �bergebene Schl�sselwort aus der Datei abruft.
	 * @param key das Schl�sselwort, f�r das der Wert abgerufen werden soll
	 * @return gibt den Wert f�r das �bergebene Schl�sselwort zur�ck
	 */
	private static String getProperty(String key) {
		Properties props = new Properties();
		if(configFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(configFile);
				props.loadFromXML(in);
				in.close();
			} catch (Exception e) {
			}
		}
		return props.getProperty(key);
	}
	
	/**
	 * Funktion, die den Wert f�r das �bergebene Schl�sselwort in die Datei schreibt.
	 * @see Properties#setProperty(String, String)
	 * @param key key das Schl�sselwort, f�r das der Wert geschrieben werden soll
	 * @param value tder Wert f�r das �bergebene Schl�sselwort
	 * @return der vorherige Wert f�r das �bergeben Schl�sselwort, oder null wenn vorher kein Wert gesetzt war.
	 */
	private static Object setProperty(String key, String value) {
		Properties props = new Properties();
		if(configFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(configFile);
				props.loadFromXML(in);
				in.close();
			} catch (Exception e) {
			}
		}
		Object result = props.setProperty(key, value);
		try {
			FileOutputStream os = new FileOutputStream(configFile);
			props.storeToXML(os, "LARS ICS - Konfigurationsdatei");
			os.close();
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * Sichtbare Funktionen die die Schl�sselworte abstrahieren
	 * lese den Datenbankpfad
	 * @return gibt den Datenbankpfad, der in der Konfigurationsdatei gespeichert ist zur�ck, oder null wenn nichts angegeben ist
	 */
	public static String getDataBasePath() {
		return getProperty(DB_PATH);
	}
	
	/**
	 * Sichtbare Funktionen die die Schl�sselworte abstrahieren
	 * schreibe den Datenbankpfad
	 * @param dbPath der Pfad zur Datenbank, der in der Konfigurationsdatei gespeichert werden soll
	 */
	public static void setDataBasePath(String dbPath) {
		setProperty(DB_PATH, dbPath);
	}

	/**
	 * Liest den Prozentsatz f�r die Berechnung
	 * @param default_value Standard-Wert f�r den Parameter
	 * @return Gibt den aktuell spezifizierten Prozentsatz zur�ck
	 */
	public static int getScorePercentage(int default_value) {
		String value = getProperty(PERCENTAGE);
		if(value != null) {
			try{
				return Integer.parseInt(value);
			}catch(Exception e) {
			}
		}
		setProperty(PERCENTAGE, default_value + "");
		return default_value;
	}

	/**
	 * Liest, ob die Berichte nach erzeugung direkt angezeigt werden sollen
	 * @return Gibt true zur�ck, wenn die Berichte nach Erzeugung ge�ffnet werden sollen.
	 */
	public static boolean isOpenReportAfterCreation() {
		String value = getProperty(OPENREPORT);
		if(value != null) {
			try{
				return Boolean.parseBoolean(value);
			}catch(Exception e) {
			}
		}
		return true;
	}
}
