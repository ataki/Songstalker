/*
 * Public Domain License
 * 
 * Maintained and released by aw2.0 Ltd 
 *    http://www.aw20.co.uk/
 * 
 * Full Support for the Amazon SimpleDB 
 *  http://docs.amazonwebservices.com/AmazonSimpleDB/2007-11-07/DeveloperGuide/
 *
 * Completely stand alone POJO; no external library requirements
 * 
 * Released: September 2009
 * 
 * Change notes
 *   - Removed dependency on Sun's Base64 implementation
 *   - Support for non-Amazon Simple Implementations
 *   - Improved Error handling
 *   - Support for HTTP-POST for sending large packets
 */
package net.aw20.commons.amazon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SimpleDB extends Object {
	private static String signatureMethod		= "HmacSHA1";
	private static String	apiVersion 				= "2009-04-15";
	private static String signatureVersion	= "2";

	static Mac mac;
	static {
		try {
			mac = Mac.getInstance(signatureMethod);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String id, httpEndPoint;
	private SecretKeySpec secret;
	
	private String	lastRequestId = null;
	private String	lastBoxUsage = null;
	private String	lastToken = null;

	public SimpleDB(String awsId, String secretKey) {
		this( "sdb.amazonaws.com", awsId, secretKey );
	}
	
	public SimpleDB(String host, String awsId, String secretKey) {
		httpEndPoint	= host;
		this.id 			= awsId;
		this.secret 	=	 new SecretKeySpec( secretKey.getBytes(), signatureMethod);
	}

	public SimpleDB(SimpleDB original) {
		this.httpEndPoint = original.httpEndPoint;
		this.id 					= original.id;
		this.secret 			= original.secret;
	}
	
	public String getLastRequestId(){
		return lastRequestId;
	}

	public String getLastBoxUsage(){
		return lastBoxUsage;
	}

	public String getLastToken(){
		return lastToken;
	}
	
	
	/*
	 * The CreateDomain operation creates a new domain. The domain name must be unique among the domains associated 
	 * with the Access Key ID provided in the request. The CreateDomain operation might take 10 or more seconds to complete.
	 * 
	 * Returns the domain that was create
	 */
	public String createDomain(String domainName) throws Exception {
		TreeMap	uriParams	= createStandardParams( "CreateDomain" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		
		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		return domainName;
	}

	
	/*
	 * The ListDomains operation lists all domains associated with the Access Key ID. It returns domain names up to the 
	 * limit set by MaxNumberOfDomains. A NextToken is returned if there are more than MaxNumberOfDomains domains. Calling 
	 * ListDomains successive times with the NextToken returns up to MaxNumberOfDomains more domain names each time.
	 */
	public List listDomains() throws Exception {
		return listDomains( -1, null );
	}
	
	public List listDomains( int MaxNumberOfDomains, String NextToken ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "ListDomains" );
		
		if ( MaxNumberOfDomains > 0 )
			uriParams.put( "MaxNumberOfDomains", String.valueOf(MaxNumberOfDomains) );
		
		if ( NextToken != null )
			uriParams.put( "NextToken", NextToken );
		
		uriParams.put( "Signature", 	getSignature( uriParams ) );

		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		
		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		return getElements( resp, "DomainName" );
	}


	/*
	 * The DeleteDomain operation deletes a domain. Any items (and their attributes) in the domain 
	 * are deleted as well. The DeleteDomain operation might take 10 or more seconds to complete.
	 * 
	 * returns back the domainName we just deleted
	 */
	public String deleteDomain(String domainName) throws Exception {
		TreeMap	uriParams	= createStandardParams( "DeleteDomain" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}

		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		return domainName;
	}

	
	

	/*
	 * Returns information about the domain, including when the domain was created,
	 * the number of items and attributes, and the size of attribute names and values.
	 * 
	 * returns back a hashmap of properties about this domain
	 */
	public HashMap domainMetaData(String domainName) throws Exception {
		TreeMap	uriParams	= createStandardParams( "DomainMetadata" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}

		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		
		HashMap	el	= new HashMap();
		
		List t = getElements( resp, "Timestamp" );
		if ( t.size() > 0 )
			el.put( "Timestamp", t.get(0).toString() );
		
		t = getElements( resp, "ItemCount" );
		if ( t.size() > 0 )
			el.put( "ItemCount", t.get(0).toString() );
		
		t = getElements( resp, "AttributeValueCount" );
		if ( t.size() > 0 )
			el.put( "AttributeValueCount", t.get(0).toString() );
		
		t = getElements( resp, "AttributeNameCount" );
		if ( t.size() > 0 )
			el.put( "AttributeNameCount", t.get(0).toString() );
		
		t = getElements( resp, "ItemNamesSizeBytes" );
		if ( t.size() > 0 )
			el.put( "ItemNamesSizeBytes", t.get(0).toString() );
		
		t = getElements( resp, "AttributeValuesSizeBytes" );
		if ( t.size() > 0 )
			el.put( "AttributeValuesSizeBytes", t.get(0).toString() );
		
		t = getElements( resp, "AttributeNamesSizeBytes" );
		if ( t.size() > 0 )
			el.put( "AttributeNamesSizeBytes", t.get(0).toString() );
		
		return el;
	}

	
	
	/*
	 * With the BatchPutAttributes operation, you can perform multiple PutAttribute 
	 * operations in a single call. This helps you yield savings in round trips and 
	 * latencies, and enables Amazon SimpleDB to optimize requests, which generally 
	 * yields better throughput.
	 */
	public String batchPutAttributes( String domainName, Map<String, Map<String,String>> itemValues ) throws Exception {
		return batchPutAttributes( domainName, itemValues, new HashMap() );
	}
		
	public String batchPutAttributes( String domainName, Map<String, Map<String,String>> itemValues, Map<String, Set> itemReplaces ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "BatchPutAttributes" );
		uriParams.put( "DomainName", 	domainName );
		
		int itemCount = 0;
		for (Map.Entry<String, Map<String,String>> itemMap : itemValues.entrySet()) {
			
			int count = 0;
			Map<String,String> map = itemMap.getValue();
			Set<String> replace	= itemReplaces.get(itemMap.getKey());
			
			uriParams.put( "Item." + itemCount + ".ItemName", itemMap.getKey() );
			
			for ( Map.Entry<String, String> x : map.entrySet() ) {
				uriParams.put( "Item." + itemCount + ".Attribute." + count + ".Name", x.getKey());
				uriParams.put( "Item." + itemCount + ".Attribute." + count + ".Value", x.getValue());

				if ( replace != null && replace.contains(x.getKey()) )
					uriParams.put( "Item." + itemCount + ".Attribute." + count + ".Replace", "true");

				++count;
			}

			++itemCount;
		}
		
		uriParams.put( "Signature",	getSignature( false, httpEndPoint, uriParams ) );
		Map<String,String>	results	= postToConnection( new URL("https://" + httpEndPoint), uriParams);
		if ( !results.get("code").equals("200") ){
			handleErrorAndThrow( results.get("body") );
		}else
			readResponse( results.get("body") );
		
		return domainName;
	}

	
	
	
	/*
	 * The PutAttributes operation creates or replaces attributes in an item. You specify new 
	 * attributes using a combination of the Attribute.X.Name and Attribute.X.Value parameters. 
	 * You specify the first attribute by the parameters Attribute.0.Name and Attribute.0.Value, 
	 * the second attribute by the parameters Attribute.1.Name and Attribute.1.Value, and so on.
	 */
	public String putAttributes( String domainName, String itemName, Map<String, String> map ) throws Exception {
		return putAttributes( domainName, itemName, map, null );
	}
	
	public String putAttributes( String domainName, String itemName, Map<String, String> map, Set replace ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "PutAttributes" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "ItemName", 		itemName );

		int count = 0;
		for (Map.Entry<String, String> x : map.entrySet()) {
			uriParams.put( "Attribute." + count + ".Name", x.getKey());
			uriParams.put( "Attribute." + count + ".Value", x.getValue());
			
			if ( replace != null && replace.contains(x.getKey()) )
				uriParams.put( "Attribute." + count + ".Replace", "true" );

			++count;
		}

		uriParams.put( "Signature",	getSignature( false, httpEndPoint, uriParams ) );

		Map<String,String>	results	= postToConnection( new URL("https://" + httpEndPoint), uriParams);
		
		if ( !results.get("code").equals("200") ){
			handleErrorAndThrow( results.get("body") );
		}else
			readResponse( results.get("body") );
		
		return domainName;
	}



	/*
	 * Deletes one or more attributes associated with the item. If all 
	 * attributes of an item are deleted, the item is deleted.
	 * 
	 * If the value of the map is null, then all the attributes of that name will be deleted
	 */
	public String deleteAttributes( String domainName, String itemName ) throws Exception {
		return deleteAttributes(domainName, itemName, null);
	}	
	
	public String deleteAttributes( String domainName, String itemName, Map<String, String> map ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "DeleteAttributes" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "ItemName", 		itemName );

		if ( map != null ){
			int count = 0;
			for (Map.Entry<String, String> x : map.entrySet()) {
				uriParams.put( "Attribute." + count + ".Name", x.getKey());
	
				if ( x.getValue() != null )
					uriParams.put( "Attribute." + count + ".Value", x.getValue());
	
				++count;
			}
		}
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}

		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		return domainName;
	}

	
	
	/*
	 * Returns all of the attributes associated with the item. Optionally, the attributes 
	 * returned can be limited to one or more specified attribute name parameters.
	 * 
	 * If the item does not exist on the replica that was accessed for this operation, 
	 * an empty set is returned. The system does not return an error as it cannot 
	 * guarantee the item does not exist on other replicas.
	 * 
	 * Returns a HashMap of key/String[]
	 * 
	 */
	public Map getAttributes( String domainName, String itemName ) throws Exception {
		return getAttributes( domainName, itemName, null );
	}
	
	public Map getAttributes( String domainName, String itemName, String attributeName ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "GetAttributes" );
		uriParams.put( "DomainName", 	domainName );
		uriParams.put( "ItemName", 		itemName );

		if ( attributeName != null )
			uriParams.put( "AttributeName", 		attributeName );
		
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}

		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );
		
		HashMap	m	= new HashMap();
		
		List attributes = getElements( resp, "Attribute");
		for (int x = 0; x < attributes.size(); x++) {
			String t = attributes.get(x).toString();

			String key = t.substring(t.indexOf("<Name>") + 6, t.indexOf("</Name>"));
			String val = t.substring(t.indexOf("<Value>") + 7, t.indexOf("</Value>"));
			
			if ( m.containsKey(key) ){
				String[] oldA = (String[])m.get(key);
				String[] newA = new String[ oldA.length + 1 ];
				System.arraycopy(oldA, 0, newA, 0, oldA.length ); 
				newA[ newA.length - 1 ] = val;
				m.put( key, newA );
			}else{
				m.put(key, new String[]{val} );
			}
		}
		
		return m;
	}



	/*
	 * The Select operation returns a set of Attributes for ItemNames that match
	 * the select expression. Select is similar to the standard SQL SELECT statement.
	 * 
	 * The total size of the response cannot exceed 1 MB in total size. Amazon SimpleDB 
	 * automatically adjusts the number of items returned per page to enforce this limit. 
	 * For example, even if you ask to retrieve 2500 items, but each individual item is 
	 * 10 kB in size, the system returns 100 items and an appropriate next token so you 
	 * can get the next page of results.
	 */
	public List	select( String selectExpression ) throws Exception {
		return select( selectExpression, null );
	}
	
	public List	select( String selectExpression, String nextToken ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "Select" );
		uriParams.put( "SelectExpression", 		selectExpression );

		if ( nextToken != null )
			uriParams.put( "NextToken", 		nextToken );
		
		uriParams.put( "Signature", 	getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}

		String	resp	= getString(con.getInputStream(), "utf-8");
		readResponse( resp );

		List<HashMap> resultList = new ArrayList<HashMap>();

		List itemList = getElements(resp, "Item");
		for (int x = 0; x < itemList.size(); x++) {
			String i = itemList.get(x).toString();

			HashMap<String, String[]> map = new HashMap<String, String[]>();

			List nameId = getElements(i, "Name");
			map.put("ItemName", new String[]{nameId.get(0).toString()});

			List attributes = getElements(i, "Attribute");
			for (int xx = 0; xx < attributes.size(); xx++) {
				String t = attributes.get(xx).toString();

				String key = t.substring(t.indexOf("<Name>") + 6, t.indexOf("</Name>"));
				String val = t.substring(t.indexOf("<Value>") + 7, t.indexOf("</Value>"));
				
				if ( map.containsKey(key) ){
					String[] oldA = (String[])map.get(key);
					String[] newA = new String[ oldA.length + 1 ];
					System.arraycopy(oldA, 0, newA, 0, oldA.length ); 
					newA[ newA.length - 1 ] = val;
					map.put( key, newA );
				}else{
					map.put(key, new String[]{val} );
				}
			}

			resultList.add(map);
		}

		return resultList;
	}
	
	
	
	
	/*
	 * Retrieve the standard Response elements
	 */
	private void readResponse( String resp ){
		try{
			List t = getElements( resp, "RequestId" );
			if ( t.size() > 0 )
				lastRequestId	= t.get(0).toString();
			
			t = getElements( resp, "BoxUsage" );
			if ( t.size() > 0 )
				lastBoxUsage	= t.get(0).toString();
			
			t = getElements( resp, "NextToken" );
			if ( t.size() > 0 )
				lastToken	= t.get(0).toString();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Retrieve the standard Response elements
	 */
	private void handleErrorAndThrow( String resp ) throws Exception {
		String errMessage = "[Bad Request]";
		List t = getElements( resp, "Code" );
		if ( t.size() > 0 )
			errMessage = t.get(0).toString() + ": ";
		
		t = getElements( resp, "Message" );
		if ( t.size() > 0 )
			errMessage += t.get(0).toString();
		
		throw new Exception( errMessage );
	}
	
	
	/*
	 * Creates the standard TreeMap value of all the standard params we require
	 * for any particular request
	 */
	private TreeMap	createStandardParams( String action ){
		TreeMap	uriParams	= new TreeMap();
		uriParams.put( "AWSAccessKeyId",		id );
		uriParams.put( "Action", 						action );
		uriParams.put( "SignatureVersion", 	signatureVersion );
		uriParams.put( "SignatureMethod", 	signatureMethod );
		uriParams.put( "Version", 					apiVersion );
		uriParams.put( "Timestamp", 				httpDate() );
		return uriParams;
	}
	

	/*
	 * Given the current Params, we create the signature for this particular
	 * request
	 */
	private String getSignature( TreeMap	uriParams ) throws Exception {
		return getSignature( true, httpEndPoint, uriParams );
	}
		
	private String getSignature( boolean bGet, String http, TreeMap	uriParams ) throws Exception {
		StringBuilder	sb = new StringBuilder( 512 );

		if ( bGet )
			sb.append( "GET\n" );
		else
			sb.append( "POST\n" );
		
		if ( http.startsWith("http") ){
			http	= http.substring( http.indexOf("//")+2 );
			String uri	= http.substring( 0, http.indexOf("/") );
			sb.append( uri + "\n" );
			
			if ( bGet )
				sb.append( http.substring( http.indexOf("/") ) + "/\n" );
			else
				sb.append( http.substring( http.indexOf("/") ) + "\n" );
		}else{
			sb.append( http );
			sb.append( "\n/\n" );
		}
		

		Iterator it = uriParams.keySet().iterator();
		while ( it.hasNext() ){
			String key = (String)it.next();
			String val = (String)uriParams.get( key );

			sb.append( key );
			sb.append( "=" );
			sb.append( URLEncoder.encode( val, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E","~") );

			if ( it.hasNext() )
				sb.append( "&" );
		}

		return Base64.encodeBytes( hmacSha1( sb.toString() ) );
	}
	

	/*
	 * Creates the URL from the parameters to Amazon
	 */
	private URL getUrl( TreeMap	uriParams ) throws Exception {
		StringBuilder	sb = new StringBuilder( 512 );

		sb.append( "https://" );
		sb.append( httpEndPoint );
		sb.append( "/?" );
		
		Iterator it = uriParams.keySet().iterator();
		while ( it.hasNext() ){
			String key = (String)it.next();
			String val = (String)uriParams.get( key );

			sb.append( key );
			sb.append( "=" );
			sb.append( URLEncoder.encode( val, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E","~") );
			
			if ( it.hasNext() )
				sb.append( "&" );
		}
		
		return new URL( sb.toString() );
	}
	
	private byte[] hmacSha1(String str) {
		try {
			mac.init(secret);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return mac.doFinal(str.getBytes());
	}

	private String getString(InputStream is, String charEncoding) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte ba[] = new byte[8192];
			int read = is.read(ba);
			while (read > -1) {
				out.write(ba, 0, read);
				read = is.read(ba);
			}
			return out.toString(charEncoding);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e){}
		}
	}

	public Map<String,String>	postToConnection( URL endPoint, Map uriParams ) throws Exception {
		HttpURLConnection urlc = (HttpURLConnection)endPoint.openConnection();
		urlc.setRequestMethod("POST");
		
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		urlc.setRequestProperty("Host", endPoint.getHost() );
		urlc.setRequestProperty("Content-type", "application/x-www-form-urlencoded" );
		
		/* Send out the data */
		OutputStream out = urlc.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		
		try{
			Iterator it = uriParams.keySet().iterator();
			while ( it.hasNext() ){
				String key = (String)it.next();
				String val = (String)uriParams.get( key );
	
				writer.write( key );
				writer.write( "=" );
				writer.write( URLEncoder.encode( val, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E","~") );

				if ( it.hasNext() )
					writer.write( "&" );
			}
		}finally{
			writer.flush();
			writer.close();
		}

		/* Read in the data */
		Reader reader = new InputStreamReader( urlc.getInputStream() );
		StringBuilder	in	= new StringBuilder( 32000 );
		
		int c;
		while ( (c=reader.read()) != -1 ){
			in.append( (char)c );
		}
		urlc.disconnect();
		
		Map<String,String> results = new HashMap<String,String>();
		results.put("body", 	in.toString() );
		results.put("code", 	String.valueOf( urlc.getResponseCode() ) );

		return results;
	}
	
	private String httpDate() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    return df.format(new Date());
	}

	private List getElements(String text, String elem) {
		ArrayList<String> list = new ArrayList<String>();

		String sTag = "<" + elem + ">";
		String eTag = "</" + elem + ">";

		int c1 = text.indexOf(sTag);
		int c2 = text.indexOf(eTag);
		while (c1 != -1 && c2 != -1) {
			list.add(text.substring(c1 + sTag.length(), c2));
			c1 = text.indexOf(sTag, c2);
			if (c1 != -1)
				c2 = text.indexOf(eTag, c1);
		}
		return list;
	}
}