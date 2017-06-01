package gws;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import beans.VisitedSite;
import commons.MyException;
import commons.Utils;
import database.DbException;
import gpc.GPCDatabase;
import net.GMail;
import net.Result;
import net.WSResponse;

/**
 * Service for the 'gpc' database.
 * 
 * @author Fabrizio Mazzurana
 *
 */
@Path("/gpc")
public class Gpc {

	final static Logger logger = LogManager.getLogger(Gpc.class);
	final static String repDir = "reports\\";

	@GET
	@Path("/report/{day}")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response reportByDay(@PathParam("day") String day, @Context HttpServletRequest reqContext) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[1];
		logger.info("{} -> {}: day = {}", reqContext.getRemoteAddr(), myName, day);
		FileWriter writer = null;
		
		try {
			// checks for day validity
			//GPCDatabase db = new GPCDatabase();
			GPCDatabase db = new GPCDatabase("fmazzurana.noip.me", 3306, "giant", "Eir3annach", null);
			if (!db.calendarCheckScannedDay(day))
				return WSResponse.Error(myName, Status.BAD_REQUEST, String.format("Day not scanned: %s", day));
			
			if (!Utils.createDirectory(repDir))
				return WSResponse.ServerError(myName, "Unable to create reports dir");
			
			// opens the output file
			String repFile = repDir + day + ".log";
			writer = new FileWriter(repFile);
			
			// gets the messages (and writes them to file)
			List<String> messages = db.messagesList(day);
			writeToFile(writer, "MESSAGES", messages);

			// gets the domains list (and writes it to file)
			List<String> domains = db.sitesListDomains(day);
			writeToFile(writer, "DOMAINS", domains);
			
			// gets the sites list (and writes it to file)
			List<VisitedSite> sites = db.sitesList(day);
			if (sites != null && sites.size() > 0) {
				String dashes = Utils.stringOfChar('-', 80);
				List<String> siteLines = new ArrayList<String>();
				for (VisitedSite site : sites) {
					siteLines.add(dashes);
					String s = String.format("  Date:  %s   Typed: %d   Browser: %s", site.getLastVisit(), site.getTyped(), site.getBrowser());
					String profile = site.getProfile();
					if (!Utils.isEmptyString(profile))
						s += String.format(" (%s)", profile);
					siteLines.add(s);
					siteLines.add("  Url:   "+site.getUrl());
					siteLines.add("  Title: "+site.getTitle());
				}
				siteLines.add(dashes);
				writeToFile(writer, "SITES", siteLines);
			}
			
			writer.close();
			writer = null;

			// gets the mail parameters
			String toAddr = db.getParamAsString("EMAIL_TO");
			
			// sends the email
			GMail gmail = new GMail(Conf.mailSenderUsr, Conf.mailSenderPwd);
			gmail.Send(toAddr, null, day+" GPC Report", "See the attached file", repFile);

			return WSResponse.OK(new Result("OK"));
		} catch (DbException | IOException | MyException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private void writeToFile(FileWriter writer, String header, List<String> lines) {
		try {
			boolean finalEmptyLine = false;
			if (!Utils.isEmptyString(header)) {
				writer.write(header+"\n");
				writer.write(Utils.stringOfChar('-', header.length())+"\n");
				finalEmptyLine = true;
			}
			if (lines != null && !lines.isEmpty()) {
				finalEmptyLine = true;
				for (String line : lines) {
					writer.write(line+"\n");
				}
			}
			if (finalEmptyLine)
				writer.write("\n");
		} catch (IOException e) {
			logger.error("writeToFile: {}", e.getMessage());
		}
	}
}

//
//RestClient rc = new RestClient();
//try {
//	//String url = String.format(sendUrl, toAddr, URLEncoder.encode("GPC Report for "+day, "UTF-8"), URLEncoder.encode("See the attached file", "UTF-8"), URLEncoder.encode(repFile, "UTF-8"));
//	String url = String.format(sendUrl, toAddr, URLEncoder.encode("GPC Report for "+day, "UTF-8"), URLEncoder.encode("See the attached file", "UTF-8"));
//	String result = rc.get(url);
//	System.out.println(result);
//} catch (MyException | UnsupportedEncodingException e) {
//	e.printStackTrace();
//}

