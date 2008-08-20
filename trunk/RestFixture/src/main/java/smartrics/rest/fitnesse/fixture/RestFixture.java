package smartrics.rest.fitnesse.fixture;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import smartrics.rest.client.RestClient;
import smartrics.rest.client.RestClientImpl;
import smartrics.rest.client.RestRequest;
import smartrics.rest.client.RestResponse;
import smartrics.rest.client.RestData.Header;
import smartrics.rest.fitnesse.fixture.support.BodyTypeAdapter;
import smartrics.rest.fitnesse.fixture.support.HeadersTypeAdapter;
import smartrics.rest.fitnesse.fixture.support.RestDataTypeAdapter;
import smartrics.rest.fitnesse.fixture.support.StatusCodeTypeAdapter;
import smartrics.rest.fitnesse.fixture.support.StringTypeAdapter;
import smartrics.rest.fitnesse.fixture.support.Tools;
import smartrics.rest.fitnesse.fixture.support.Url;
import smartrics.rest.fitnesse.fixture.support.Variables;
import fit.ActionFixture;
import fit.Parse;
import fit.TypeAdapter;
import fit.exception.FitFailureException;

public class RestFixture extends ActionFixture {

	private RestResponse lastResponse;

	private String requestBody;
	private Map<String, String> requestHeaders;

	private RestClient client;

	private boolean displayActualOnRight;

	protected static final Map<String, String> DEF_REQUEST_HEADERS = new HashMap<String, String>();
	private static final Pattern FIND_VARS_PATTERN = Pattern.compile("\\%([a-zA-Z0-9]+)\\%");
	private static Log LOG = LogFactory.getLog(RestFixture.class);
	private Variables variables = new Variables();

	public RestFixture(){
		displayActualOnRight = true;
	}

	public void setRestClient(RestClient rClient){
		client = rClient;
	}

	public boolean isDisplayActualOnRight() {
		return displayActualOnRight;
	}

	public void setDisplayActualOnRight(boolean displayActualOnRight) {
		this.displayActualOnRight = displayActualOnRight;
	}

	public RestClient getRestClient(){
		if(client==null){
			// TODO: provide config for HttpClient (wikiwidget)
			HttpClient httpClient = new HttpClient();
			setRestClient(new RestClientImpl(httpClient));
		}
		return client;
	}

	public void doCells(Parse parse) {
		cells = parse;
		if (args.length != 1) {
			throw new FitFailureException(
					"You must specify a base url in the |start|, after the fixture to start");
		}
		// parses the baseUrl to
		Url baseUrl = new Url(args[0]);
		getRestClient().setBaseUrl(baseUrl.getBaseUrl());
		try {
			Method method1 = getClass().getMethod(parse.text());
			method1.invoke(this);
		} catch (Exception exception) {
			exception(parse, exception);
		}
	}

	/**
	 * <code>| setBody | body text goes here |</code> <p/> body text can
	 * either be a kvp or a xml. The <code>ClientHelper</code> will figure it
	 * out
	 */
	public void setBody() {
		if (cells.more == null)
			throw new FitFailureException("You must pass a body to set");
		body(variables.substitute(cells.more.text()));
	}

	/**
	 * <code>| setHeader | http headers go here as nvp |</code> <p/> header
	 * text must be nvp. name and value must be separated by ':' and each header
	 * is in its own line
	 */
	public void setHeader() {
		if (cells.more == null)
			throw new FitFailureException("You must pass a header map to set");
		String header = variables.substitute(cells.more.text());
		headers(header);
	}

	/**
	 * <code> | PUT | uri | ?ret | ?headers | ?body |</code> <p/> executes a
	 * PUT on the uri and checks the return (a string repr the operation return
	 * code), the http response headers and the http response body
	 *
	 * uri is resolved by replacing vars previously defined with
	 * <code>let()</code>
	 *
	 * the http request headers can be set via <code>setHeaders()</code>. If
	 * not set, the list of default headers will be set. See
	 * <code>DEF_REQUEST_HEADERS</code>
	 */
	public void PUT() {
		debugMethodCallStart();
		doMethod(requestBody, "Put");
		debugMethodCallEnd();
	}

	/**
	 * <code> | GET | uri | ?ret | ?headers | ?body |</code> <p/> executes a
	 * GET on the uri and checks the return (a string repr the operation return
	 * code), the http response headers and the http response body
	 *
	 * uri is resolved by replacing vars previously defined with
	 * <code>let()</code>
	 *
	 * the http request headers can be set via <code>setHeaders()</code>. If
	 * not set, the list of default headers will be set. See
	 * <code>DEF_REQUEST_HEADERS</code>
	 */
	public void GET() {
		debugMethodCallStart();
		doMethod("Get");
		debugMethodCallEnd();
	}

	/**
	 * <code> | DELETE | uri | ?ret | ?headers | ?body |</code> <p/> executes a
	 * DELETE on the uri and checks the return (a string repr the operation
	 * return code), the http response headers and the http response body
	 *
	 * uri is resolved by replacing vars previously defined with
	 * <code>let()</code>
	 *
	 * the http request headers can be set via <code>setHeaders()</code>. If
	 * not set, the list of default headers will be set. See
	 * <code>DEF_REQUEST_HEADERS</code>
	 */
	public void DELETE() {
		debugMethodCallStart();
		doMethod("Delete");
		debugMethodCallEnd();
	}

	/**
	 * <code> | POST | uri | ?ret | ?headers | ?body |</code> <p/> executes a
	 * POST on the uri and checks the return (a string repr the operation return
	 * code), the http response headers and the http response body
	 *
	 * uri is resolved by replacing vars previously defined with
	 * <code>let()</code>
	 *
	 * post requires a body that can be set via <code>setBody()</code>.
	 *
	 * the http request headers can be set via <code>setHeaders()</code>. If
	 * not set, the list of default headers will be set. See
	 * <code>DEF_REQUEST_HEADERS</code>
	 */
	public void POST() {
		debugMethodCallStart();
		doMethod(requestBody, "Post");
		debugMethodCallEnd();
	}

	/**
	 * <code> | let | label | type | loc | expr |</code> <p/> allows to
	 * associate a value to a label. values are extracted from the body of the
	 * last successful http response.
	 * <ul>
	 * <li/><code>label</code> is the label identifier
	 *
	 * <li/><code>type</code> is the type of operation to perform on the last
	 * http response. At the moment only XPaths and Regexes are supported. In
	 * case of regular expressions, the expression must contain only one group
	 * match, if multiple groups are matched the label will be assigned to the
	 * first found <code>type</code> only allowed values are
	 * <code>xpath</code> and <code>regex</code>
	 *
	 * <li/><code>loc</code> where to apply the <code>expr</code> of the
	 * given <code>type</code>. Currently only <code>header</code> and
	 * <code>body</code> are supported. If type is <code>xpath</code> by
	 * default the expression is matched against the body and the value in loc
	 * is ignored.
	 *
	 * <li/><code>expr</code> is the expression of type <code>type</code>
	 * to be executed on the last http response to extract the content to be
	 * associated to the label.
	 * </ul>
	 * <p/> <code>label</code>s can be retrieved after they have been defined
	 * and their scope is the fixture instance under execution. They are stored
	 * in a map so multiple calls to <code>let()</code> with the same label
	 * will override the current value of that label. <p/> Labels are resolved
	 * in <code>uri</code>s, <code>header</code>s and <code>body</code>es.
	 * <p/> In order to be resolved a label must be between <code>%</code>,
	 * e.g. <code>%id%</code>. <p/> The test row must have an empy cell at
	 * the end that will display the value extracted and assigned to the label.
	 * <p/> Example: <br/> <code>| GET | /services | 200 | | |</code><br/>
	 * <code>| let | id |  body | /services/id[0]/text() | |</code><br/>
	 * <code>| GET | /services/%id% | 200 | | |</code><p/> or<p/>
	 * <code>| POST | /services | 201 | | |</code><br/>
	 * <code>| let  | id | header | /services/([.]+) | |</code><br/>
	 * <code>| GET  | /services/%id% | 200 | | |</code>
	 */
	public void let() {
		debugMethodCallStart();
		String label = cells.more.text().trim();
		String loc = cells.more.more.text();
		String expr = cells.more.more.more.text();
		Parse valueCell = cells.more.more.more.more;
		String sValue = null;
		try{
			if ("header".equals(loc)) {
				sValue = handleRegexExpression(label, loc, expr);
			} else if ("body".equals(loc)) {
				sValue = handleXpathExpression(label, expr);
			} else {
				throw new FitFailureException("let handles 'xpath' in body or 'regex' in headers.");
			}
			if (valueCell != null) {
				StringTypeAdapter adapter = new StringTypeAdapter();
				try{
					adapter.set(sValue);
					super.check(valueCell, adapter);
				} catch(Exception e){
					exception(valueCell, e);
				}
			}

		} catch(RuntimeException e){
			exception(cells.more.more.more, e);
		} finally {
			debugMethodCallEnd();
		}
	}

	private String handleRegexExpression(String label, String loc, String expression) {
		List<String> content = new ArrayList<String>();
		if ("header".equals(loc)) {
			if (lastResponse.getHeaders() != null) {
				for (Header e : lastResponse.getHeaders()) {
					String string = Tools.convertEntryToString(e.getName(), e.getValue(), ":");
					content.add(string);
				}
			}
		} else {
			content.add(lastResponse.getBody());
		}
		String value = null;
		if (content.size() > 0) {
			Pattern p = Pattern.compile(expression);
			for (String c : content) {
				Matcher m = p.matcher(c);
				if (m.find()) {
					int cc = m.groupCount();
					value = m.group(cc);
					assignVariable(label, value);
					break;
				}
			}
		}
		return value;
	}

	private String handleXpathExpression(String label, String expr) {
		// def. match only last response body
		NodeList list = Tools.extractXPath(expr, lastResponse.getBody());
		Node item = list.item(0);
		String val = null;
		if (item != null) {
			val = item.getTextContent();
		}
		if(val!=null)
			assignVariable(label, val);
		return val;
	}

	private void assignVariable(String label, String val) {
		variables.put(label, val);
	}

	private Map<String, String> getHeaders() {
		Map<String, String> headers = null;
		if (requestHeaders != null) {
			headers = requestHeaders;
		} else {
			headers = DEF_REQUEST_HEADERS;
		}
		return headers;
	}

	private void doMethod(String m) {
		doMethod(null, m);
	}

	private void doMethod(String body, String method) {
		String resUrl = resolve(FIND_VARS_PATTERN, cells.more.text());
		RestRequest request = new RestRequest();
		request.setMethod(RestRequest.Method.valueOf(method));
		request.addHeaders(getHeaders());
		request.setResource(resUrl);
		if("Post".equals(method) || "Put".equals(method)){
			String rBody = resolve(FIND_VARS_PATTERN, body);
			request.setBody(rBody);
		}
		lastResponse = getRestClient().execute(request);
		completeHttpMethodExecution();
	}

	private void completeHttpMethodExecution() {
		String u = getRestClient().getBaseUrl() + lastResponse.getResource();
		cells.more.body = "<a href='" + u + "'>" + lastResponse.getResource() +"</a>";
		process(cells.more.more, lastResponse.getStatusCode().toString(), new StatusCodeTypeAdapter());
		process(cells.more.more.more, lastResponse.getHeaders(), new HeadersTypeAdapter());
		process(cells.more.more.more.more, lastResponse.getBody(), new BodyTypeAdapter());
	}

	private void process(Parse expected, Object actual, RestDataTypeAdapter ta) {
		ta.set(actual);
		boolean ignore = "".equals(expected.text().trim());
		if (ignore) {
			expected.addToBody(gray(ta.toString()));
		} else {
			boolean success = false;
			try {
				success = ta.equals(ta.parse(expected.text()), actual);
			} catch (Exception e) {
				exception(expected, e);
				return;
			}
			if (success) {
				right(expected, ta);
			} else {
				wrong(expected, ta);
			}
		}
	}

	private void right(Parse expected, RestDataTypeAdapter ta) {
		super.right(expected);
		if(isDisplayActualOnRight() && !expected.text().equals(ta.toString())){
			expected.addToBody(label("expected") + "<hr>" + ta.toString() + label("actual"));
		}
	}

	private void wrong(Parse expected, RestDataTypeAdapter ta) {
		super.wrong(expected);
		StringBuffer sb = new StringBuffer();
		for(String e: ta.getErrors()){
			sb.append(e).append(System.getProperty("line.separator"));
		}
		expected.addToBody(label("expected") + "<hr>" + ta.toString() + label("actual") + "<hr>" + Tools.toHtml(sb.toString()) + label("errors"));
	}

	private void debugMethodCallStart() {
		debugMethodCall("=> ");
	}

	private void debugMethodCallEnd() {
		debugMethodCall("<= ");
	}

	private void debugMethodCall(String h) {
		StackTraceElement el = Thread.currentThread().getStackTrace()[4];
		LOG.debug(h + el.getMethodName());
	}


	private String resolve(Pattern pattern, String text) {
		Matcher m = pattern.matcher(text);
		Map<String, String> replacements = new HashMap<String, String>();
		while(m.find()){
			int gc = m.groupCount();
			if(gc==1){
				String g0 = m.group(0);
				String g1 = m.group(1);
				replacements.put(g0, variables.get(g1));
			}
		}
		String newText = text;
		for(String k : replacements.keySet()){
			String replacement = replacements.get(k);
			if(replacement!=null)
				newText = newText.replace(k, replacement);
		}
		return newText;
	}

	void body(String string) {
		requestBody = string;
	}

	void headers(String header){
		requestHeaders = Tools.convertStringToMap(header, ":", System.getProperty("line.separator"));
	}
}
