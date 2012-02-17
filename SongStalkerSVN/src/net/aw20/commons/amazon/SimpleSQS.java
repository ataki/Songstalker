/*
 * Public Domain License
 * 
 * Maintained and released by aw2.0 Ltd 
 *    http://www.aw20.co.uk/
 * 
 * Full Support for the Amazon SQS 
 *  http://docs.amazonwebservices.com/AWSSimpleQueueService/2009-02-01/SQSDeveloperGuide/
 *
 * Completely stand alone POJO; no external library requirements
 * 
 * Released: September 2009
 * 
 * Changes:
 *   - Improved error reporting
 *   - Removed dependency on Sun's Base64 implementation
 * 
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
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SimpleSQS extends Object {
	private static String signatureMethod		= "HmacSHA1";
	private static String	httpEndPoint 			= "queue.amazonaws.com";
	private static String	apiVersion 				= "2009-02-01";
	private static String signatureVersion	= "2";

	static Mac mac;
	static {
		try {
			mac = Mac.getInstance(signatureMethod);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final String id;
	private final SecretKeySpec secret;

	public SimpleSQS(String awsId, String secretKey) {
		this.id 		= awsId;
		this.secret = new SecretKeySpec( secretKey.getBytes(), signatureMethod);
	}

	public SimpleSQS(SimpleSQS original) {
		this.id 		= original.id;
		this.secret = original.secret;
	}

	
	/*
	 * The SetQueueAttributes action sets an attribute of a queue
	 * 
	 * http://docs.amazonwebservices.com/AWSSimpleQueueService/2009-02-01/SQSDeveloperGuide/AboutVT.html
	 */
	public void setQueueAttributesVisibilityTimeout( String QueueUrl, int VisibilityTimeout ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "SetQueueAttributes" );
		uriParams.put( "Attribute.1.Name",	"VisibilityTimeout" );
		uriParams.put( "Attribute.1.Value",	String.valueOf(VisibilityTimeout) );
		uriParams.put( "Signature",					getSignature( QueueUrl, uriParams ) );

		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}
	
	
	
	/*
	 * The AddPermission action adds a permission to a queue for a specific principal. This allows for 
	 * sharing access to the queue.
	 * 
	 * When you create a queue, you have full control access rights for the queue. Only you (as owner 
	 * of the queue) can grant or deny permissions to the queue. For more information about these 
	 * permissions, see Shared Queues.
	 */
	public void addPermission( String QueueUrl, String Label, String AWSAccountId, String ActionName ) throws Exception {
		addPermission( QueueUrl, Label, new String[]{AWSAccountId}, new String[]{ActionName} );
	}
	
	public void addPermission( String QueueUrl, String Label, String[] AWSAccountId, String[] ActionName ) throws Exception {
		if ( AWSAccountId.length != ActionName.length )
			throw new Exception("control arrays need to be the same length");
		
		TreeMap	uriParams	= createStandardParams( "AddPermission" );
		uriParams.put( "Label",	Label );
		
		for ( int x=0; x < AWSAccountId.length; x++ ){
			uriParams.put( "AWSAccountId." + (x+1),	AWSAccountId[x] );
			uriParams.put( "ActionName." + (x+1),		ActionName[x] );
		}
		
		uriParams.put( "Signature",			getSignature( QueueUrl, uriParams ) );

		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}
	
	
	/*
	 * The GetQueueAttributes action gets one or all attributes of a queue. Queues 
	 * currently have five attributes you can get:
	 * 
	 * ApproximateNumberOfMessages / VisibilityTimeout / CreatedTimestamp / LastModifiedTimestamp / Policy
	 */
	public Map<String,String>	getQueueAttributes(String QueueUrl) throws Exception {
		TreeMap	uriParams	= createStandardParams( "GetQueueAttributes" );
		uriParams.put( "AttributeName",	"All" );
		uriParams.put( "Signature",			getSignature( QueueUrl, uriParams ) );

		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		
		return getAttributes( getString(con.getInputStream(), "utf-8") );
	}

	
	
	/*
	 * The ReceiveMessage action retrieves one or more messages from the specified queue.
	 * 
	 * You may only receive up to 10 messages at once.
	 */
	public List<Map>	receiveMessage(String QueueUrl) throws Exception {
		return receiveMessage( QueueUrl, 1, -1 );
	}
	
	public List<Map>	receiveMessage(String QueueUrl, int MaxNumberOfMessages) throws Exception {
		return receiveMessage( QueueUrl, MaxNumberOfMessages, -1 );
	}
	
	public List<Map>	receiveMessage( String QueueUrl, int MaxNumberOfMessages, int VisibilityTimeout ) throws Exception {
		if ( MaxNumberOfMessages > 10 )
			MaxNumberOfMessages = 10;
		
		TreeMap	uriParams	= createStandardParams( "ReceiveMessage" );
		uriParams.put( "MaxNumberOfMessages",	String.valueOf(MaxNumberOfMessages) );
		
		if ( VisibilityTimeout > 0 )
			uriParams.put( "VisibilityTimeout",	String.valueOf(VisibilityTimeout) );
			
		uriParams.put( "Signature",	getSignature( QueueUrl, uriParams ) );
		
		
		/* Make the call */
		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		String	resp	= getString(con.getInputStream(), "utf-8");
		
		/* Parse out the parameters */
		List<Map>	messages	= new ArrayList<Map>();
		
		List<String> messageList = getElements(resp, "Message");
		for ( int x=0; x < messageList.size(); x++ ){
			HashMap<String, String> mess = new HashMap<String, String>();
			
			mess.put( "MessageId",			(String)getElements( messageList.get(x), "MessageId" ).get(0) );
			mess.put( "ReceiptHandle",	(String)getElements( messageList.get(x), "ReceiptHandle" ).get(0) );
			mess.put( "MD5OfBody",			(String)getElements( messageList.get(x), "MD5OfBody" ).get(0) );
			
			StringBuilder body = new StringBuilder( 8192 );
			body.append( getElements( messageList.get(x), "Body" ).get(0) );
			unescape( body, "&lt;", 	"<" );
			unescape( body, "&gt;", 	">" );
			unescape( body, "&amp;", 	"&" );
			unescape( body, "&quot;",	"\"" );
			unescape( body, "&nbsp;",	" " );
			mess.put( "Body",	body.toString() );
			
			messages.add( mess );
		}

		return messages;
	}

	/*
	 * The SendMessage action delivers a message to the specified queue. 
	 * The maximum allowed message size is 8 KB.
	 */
	public String sendMessage(String QueueUrl, String message ) throws Exception {
		if ( message.length() > 8192 )
			throw new Exception( "message is too large (<8KB)" );
		
		TreeMap	uriParams	= createStandardParams( "SendMessage" );
		uriParams.put( "MessageBody",	message );
		uriParams.put( "Signature",	getSignature( false, QueueUrl, uriParams ) );
		
		Map<String,String>	results	= postToConnection( new URL(QueueUrl), uriParams);
		if ( !results.get("code").equals("200") ){
			handleErrorAndThrow( results.get("body") );
		}
		
		return (String)getElements( results.get("body"), "MessageId" ).get(0);
	}
	
	
	/*
	 * The RemovePermission action revokes any permissions in the queue policy 
	 * that matches the Label parameter. Only the owner of the queue can remove 
	 * permissions.
	 */
	public void removePermission(String QueueUrl, String Label ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "RemovePermission" );
		uriParams.put( "Label", 		Label);
		uriParams.put( "Signature",	getSignature( QueueUrl, uriParams ) );
		
		/* Make the call */
		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}

	
	
	/*
	 * The ChangeMessageVisibility action changes the visibility timeout 
	 * of a specified message in a queue to a new value. The maximum 
	 * allowed timeout value you can set the value to is 12 hours.
	 */
	public void changeMessageVisibility(String QueueUrl, String ReceiptHandle, int VisibilityTimeout) throws Exception {
		TreeMap	uriParams	= createStandardParams( "ChangeMessageVisibility" );
		uriParams.put( "ReceiptHandle", 		ReceiptHandle);
		uriParams.put( "VisibilityTimeout", String.valueOf(VisibilityTimeout) );
		uriParams.put( "Signature", 				getSignature( QueueUrl, uriParams ) );
		
		/* Make the call */
		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}

	

	/*
	 * The DeleteMessage action deletes the specified message from the specified 
	 * queue. You specify the message by using the message's receipt handle and 
	 * not the message ID you received when you sent the message
	 */
	public void deleteMessage(String QueueUrl, String ReceiptHandle) throws Exception {
		TreeMap	uriParams	= createStandardParams( "DeleteMessage" );
		uriParams.put( "ReceiptHandle", ReceiptHandle);
		uriParams.put( "Signature", 		getSignature( QueueUrl, uriParams ) );
		
		/* Make the call */
		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}

	
	
	/*
	 * The DeleteQueue action deletes the queue specified by the queue URL, 
	 * regardless of whether the queue is empty. If the specified queue 
	 * does not exist, SQS returns a successful response.
	 */
	public void deleteQueue(String QueueUrl) throws Exception {
		TreeMap	uriParams	= createStandardParams( "DeleteQueue" );
		uriParams.put( "Signature", 	getSignature( QueueUrl, uriParams ) );
		
		/* Make the call */
		URL url = getUrl( QueueUrl, uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
	}
	
	
	/*
	 * The ListQueues action returns a list of your queues. The maximum number
	 * of queues that can be returned is 1000. If you specify a value for the 
	 * optional QueueNamePrefix parameter, only queues with a name beginning 
	 * with the specified value are returned
	 * 
	 * return a list of QueueUrl's
	 */
	public List<String> listQueues() throws Exception {
		return listQueues( null );
	}
	
	public List<String> listQueues(String prefix ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "ListQueues" );
		if (prefix != null)
			uriParams.put( "QueueNamePrefix", prefix );

		uriParams.put( "Signature", getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		
		String	resp	= getString(con.getInputStream(), "utf-8");
		return getElements( resp, "QueueUrl" );
	}

	
	
	/*
	 * The CreateQueue action creates a new queue. When you request CreateQueue, you provide 
	 * a name for the queue. To successfully create a new queue, you must provide a name that 
	 * is unique within the scope of your own queues
	 * 
	 * Returns the QueueUrl that was created
	 */
	public String createQueue(String QueueName) throws Exception {
		return createQueue( QueueName, 30 );
	}
	
	public String createQueue(String QueueName, int DefaultVisibilityTimeout ) throws Exception {
		TreeMap	uriParams	= createStandardParams( "CreateQueue" );
		uriParams.put( "QueueName", 								QueueName );
		uriParams.put( "DefaultVisibilityTimeout", 	String.valueOf(DefaultVisibilityTimeout) );
		uriParams.put( "Signature", 								getSignature( uriParams ) );
		
		/* Make the call */
		URL url = getUrl( uriParams );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		if (con.getResponseCode() >= 300) {
			handleErrorAndThrow( getString(con.getErrorStream(), "utf-8") );
		}
		
		String	resp	= getString(con.getInputStream(), "utf-8");
		List<String> q = getElements( resp, "QueueUrl" );
		if ( q.size() == 0 )
			throw new Exception( "no queue created" );
		
		return q.get(0);
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
	private String getSignature( String http, TreeMap	uriParams ) throws Exception {
		return getSignature( true, http, uriParams );
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
		return getUrl( httpEndPoint, uriParams );
	}
	
	private URL getUrl( String http, TreeMap	uriParams ) throws Exception {
		StringBuilder	sb = new StringBuilder( 512 );

		if ( http.indexOf("http") == -1 )
			sb.append( "https://" );
		
		sb.append( http );
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
			} catch (final Exception e) {}
		}
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

	private void unescape( StringBuilder body, String from, String to ){
		int c1 = body.indexOf(from);
		while (c1 != -1) {
			body.replace(c1, c1 + from.length(), to );
		  c1 = body.indexOf(from);
		}
	}

	private Map getAttributes( String resp ){
		Map	results =	new HashMap();
		List atts	= getElements( resp, "Attribute" );
		Iterator<String> it = atts.iterator();
		while ( it.hasNext() ){
			String body = it.next();
			
			String name		= (String)getElements( body, "Name").get(0);
			String value	= (String)getElements( body, "Value").get(0);
			
			results.put( name, value );
		}
		return results;
	}
}