package simpleefa.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author patrick
 *
 */
public class Simpleefa {

	//corrections for the coordinates
	protected static long COORD_X_CORRECTION = -3000060;
	protected static long COORD_Y_CORRECTION = 6158420;

	//TODO: change to a working EFA installation
	//the EFA-URL.
	protected static String EFA_URL = "http://an.efa.url";

	//a list of hosts the connection limit is not applied to
	protected static String[] whitelist = {"127.0.0.1","192.168.0.1","0.0.0.1","0:0:0:0:0:0:0:1"};

	//max connections per hour, -1 means unlimited, 0 means none.
	private static int maxConnectionsPerHour = 0;


	protected static boolean checkLimit(HttpServletRequest request, ServletContext c) {				
		String host = request.getRemoteAddr();
		return checkAccess(host,c);				
	}

	private static boolean checkAccess(String host, ServletContext c) {

		if (maxConnectionsPerHour == -1 || Arrays.asList(whitelist).contains(host)) return true;
		if (maxConnectionsPerHour == 0) return false;		

		long curTime = (new Date()).getTime();

		if (c.getAttribute("acces-time-" + host) == null) {
			c.setAttribute("acces-time-" + host, curTime);
		}

		if (c.getAttribute("acces-count-" + host) == null) {
			c.setAttribute("acces-count-" + host, 1);
		}

		int accesses = (Integer) c.getAttribute("acces-count-" + host);
		long time = (Long) c.getAttribute("acces-time-" + host);	

		if (curTime-time > 3600000) {
			c.setAttribute("acces-count-" + host, 1);
			c.setAttribute("acces-time-" + host, curTime);
			return true;
		}		

		if (accesses < maxConnectionsPerHour) {
			c.setAttribute("acces-count-" + host, accesses+1);
			return true;			
		}else{
			return false;			
		}

	}

	protected static void limitReached(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();

		out.println("<?xml version=\"1.0\"?>");
		out.println("<error>Access denied for host " + request.getRemoteAddr() + ", sorry.</error>");
	}
}