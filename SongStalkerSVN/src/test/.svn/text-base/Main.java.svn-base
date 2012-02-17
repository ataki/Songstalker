package test;
// added by vlad for testing svn
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.aw20.commons.amazon.SimpleDB;

@SuppressWarnings("unused" )
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SimpleDB s = new SimpleDB("AKIAJ7GNYSCZJLFAX5EA", "SKI1pgws2LrDPAtPglWSOSuFN82jFO8FRXZAZ3OE");			
		List<HashMap> res;
		try {
			res = s.select("select * from StoreToTheCore where Team = 'Stanford'");
			String[] scores = (String[]) res.get(0).get("Score");
			int number = Integer.parseInt(scores[0]);

			System.out.println("Stanford " + number);
			
			res = s.select("select * from StoreToTheCore where Team = 'Berkeley'");
			scores = (String[]) res.get(0).get("Score");
			number = Integer.parseInt(scores[0]);
			
			System.out.println("Berkeley " + number);
			
			Map<String, String> m = new HashMap<String, String>();
			m.put("Team", "Stanford");

			String newNumber = "0";
			if(number == 25) {
				newNumber = "1";
			} else {
				newNumber = "25";
			}

			m.put("Score", newNumber);									
			Set<String> replaces = new HashSet<String>();
			replaces.add("Score");
			replaces.add("Team");
			
			// s.putAttributes("StoreToTheCore", "1", m, replaces);
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
