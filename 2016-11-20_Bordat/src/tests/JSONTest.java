package tests;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JSONTest {

	@Test
	public void SampleTest() throws JSONException {
		String json = "{\"items\" : {" + 
		                             "\"file\" : \"bfh\"," +
		                             "\"org\" : \"Bern University of Applied Sciences and Arts - BFH\"," +
		                             "\"inst\" : \"BFH-TI\"," +
		                             "\"label\" : \"Name, first name\"," +
		                             "\"title\" : \"Prof.\"," +
		                             "\"given-name\" : \"Firstname\"," +
		                             "\"family-name\" : \"LastName\"," +
		                             "\"home-page\" : \"https://bfh.ch/\"," +
		                             "\"group\" : \"Abteilung Informatik\"," +
		                             "\"group-page\" : \"http://www.ti.bfh.ch/de/bachelor/informatik.html\"," +
		                             "\"since\" : \"1900\"," +
		                             "\"email\" : \"name@bfh.ch\"" +
		                           "}" +
		                         "}";
		JSONObject everything = new JSONObject(json);
		String[] attrs = JSONObject.getNames(everything.getJSONObject("items"));
		assertEquals(12, attrs.length);
	}
}
