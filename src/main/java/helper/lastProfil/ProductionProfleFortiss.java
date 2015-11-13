/**
 * 
 */
package helper.lastProfil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import simulation.SimulationStarter;

/**
 * @author Denis Bytschkow
 * @author balsa
 */
public class ProductionProfleFortiss {
	
	
	private static final int NUM = 961;
	
	private static double[] loadDomestic = new double[NUM];
	
	private static LocalDateTime[] time = new LocalDateTime[NUM];
	
	private static boolean isDataThere = false;  // flag
	private static int timeIndex = 0;
	
	public static double getLoadFortissProduction(LocalDateTime inputTime){
		updateTimeIndex(inputTime);
		return loadDomestic[timeIndex];
	}
	
	public static synchronized boolean updateTimeIndex(LocalDateTime inputTime) {
		if (!isDataThere) {						
			readFile();
		}
		
		// Consider just year 2014, August month and days between 5. and 14.
		LocalDateTime iTime;
		if (inputTime.getDayOfMonth() > 14 || inputTime.getDayOfMonth() < 5) {
			iTime = LocalDateTime.of(2014, 8, 5, inputTime.getHour(), inputTime.getMinute());
		}
		else {
			iTime = LocalDateTime.of(2014, 8, inputTime.getDayOfMonth(), inputTime.getHour(), inputTime.getMinute());
		}
		
		int minIndex = iTime.getMinute() / 15;
		int hIndex = iTime.getHour() * 4;	
		
		// We need to scale dates because we have only 10 days in our sheet
		// Therefore we are taking 5. as 1. day and so on
		// If we have days that are not in the scope we are using profile from existing days starting from 1. day
		int day = 1;
		if (inputTime.getDayOfMonth() > 14 || inputTime.getDayOfMonth() < 5) {
			day = 1;
		}
		else {
			day = inputTime.getDayOfMonth() - 4;
		}
		
		int dayIndex = (day - 1) * 24 * 4;
		
		timeIndex = dayIndex + hIndex + minIndex;		
		return true;
	}

	public static boolean readFile(){
		try {
			String source = "src/main/resources/lastProfiles/fortiss-production.csv";
			
			String location = ProductionProfleFortiss.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			location = location.replace("%20", " ");
			location = location.substring(0, location.length()-15);
			location = location + source;
			
			FileReader fr = new FileReader(location);
			BufferedReader br = new BufferedReader(fr);
		    read(br);			
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			try {
				String source = "/resources/lastProfiles/fortiss-production.csv";
				InputStreamReader isr = new InputStreamReader(ProductionProfleFortiss.class.getResourceAsStream(source));					
				BufferedReader br2 = new BufferedReader(isr);					
				read(br2);		
			} catch (IOException | ParseException e1) {
				e1.printStackTrace();
				SimulationStarter.stopSimulation();
				return false;
			}
		}		
		isDataThere = true;		
		return true;
	}
	
	private static void read(BufferedReader br) throws IOException, ParseException{
	    String zeile;
	    String[] buffer;
	    
	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	    
	    NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
	    
    	int i = 0;
    	
    	while ((zeile = br.readLine()) != null){		    			
				buffer = zeile.split(",");
				String dateTime = buffer[0];
				String [] dateTimeArray;
				dateTimeArray = dateTime.split(" ");				
				LocalDateTime ldt = LocalDateTime.of(LocalDate.parse(dateTimeArray[0], dateFormatter), LocalTime.parse(dateTimeArray[1], timeFormatter));
				time[i] = ldt;
				
				loadDomestic[i]= nf.parse(buffer[1]).doubleValue();							
				i++;
		}
    	
	    br.close();
	}	

}
