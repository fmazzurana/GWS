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

	@GET
	@Path("/send")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// call exemple: mail/send?to=<text>&cc=<text>&subject=<text>&text=<text>&attach=<text>
	public Response Send(@Context HttpServletRequest reqContext,
			@QueryParam("to") String toAddr,
			@QueryParam("cc") String ccAddr,
			@QueryParam("subject") String subject,
			@QueryParam("text") String text,
			@QueryParam("attach") String attach) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: to = {}, cc = {}, subject: {}, text: {}, attach: {}", reqContext.getRemoteAddr(), myName, toAddr, ccAddr, subject, text, attach);

		GMail gmail = new GMail(Conf.mailSenderUsr, Conf.mailSenderPwd);
		try {
			gmail.Send(toAddr, ccAddr, subject, text, attach);
			return WSResponse.OK(new Result("OK"));
		} catch (MyException ex) {
			return WSResponse.ServerError(myName, ex.getMessage());
		}
	}
}
