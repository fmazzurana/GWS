package gws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commons.MyException;
import net.GMail;
import net.Result;
import net.WSResponse;

@Path("/mail")
public class Mail {

	// --------------------------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------------------------
	final static Logger logger = LogManager.getLogger(Mail.class);
	private static final String senderUsr = "myberry.giant@gmail.com";
	private static final String senderPwd = "Eir3annach";

	@GET
	@Path("/send")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// call exemple: mail/send?to=<text>&subject=<text>&text=<text>
	public Response Send(@Context HttpServletRequest reqContext,
			@QueryParam("to") String toAddr,
			@QueryParam("subject") String subject,
			@QueryParam("text") String text) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: to = {}, subject: {}, text: {}", reqContext.getRemoteAddr(), myName, toAddr, subject, text);

		GMail gmail = new GMail(senderUsr, senderPwd);
		try {
			gmail.Send(toAddr, subject, text);
			return WSResponse.OK(new Result("OK"));
		} catch (MyException ex) {
			return WSResponse.ServerError(myName, ex.getMessage());
		}
	}
}
