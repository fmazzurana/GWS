package gws;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import beans.ChannelBean;
import beans.EventsSpecialBean;
import beans.GenreBean;
import beans.MovieBean;
import beans.StringBean;
import commons.Utils;
import database.DbException;
import database.EpgDatabase;
import net.WSResponse;

/**
 * Service for the 'epg' database.
 * 
 * The calling path is:
 * 	epg/<tablename/<operation>[/<parameter> | <queryparam>]
 * 
 * @author Fabrizio Mazzurana
 *
 */
@Path("/epg")
public class Epg {

	final static Logger logger = LogManager.getLogger(Epg.class);
	final static String PropertiesFile = "gws.properties";

	// --------------------------------------------------------------------------------------------
	// Methods on CHANNELS
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/channels/list")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response channelsList(@Context HttpServletRequest reqContext) {
		return channels_list(reqContext.getRemoteAddr(), null);
	}	

	@GET
	@Path("/channels/list/{genreId}")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response channelsListByGenre(@PathParam("genreId") String genreId, @Context HttpServletRequest reqContext) {
		return channels_list(reqContext.getRemoteAddr(), genreId);
	}	

	// --------------------------------------------------------------------------------------------
	// Methods on CONTROLS
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/controls/list")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response controlsList(@Context HttpServletRequest reqContext) {
		return controls_list(reqContext.getRemoteAddr());
	}

	// --------------------------------------------------------------------------------------------
	// Methods on EVENTS
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/events/listspecials")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response eventsListSpecials(@Context HttpServletRequest reqContext) {
		return events_list_specials(reqContext.getRemoteAddr());
	}

	@GET
	@Path("/events/listmovies")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response eventsListmovies(@Context HttpServletRequest reqContext) {
		return events_list_movies(reqContext.getRemoteAddr(), null);
	}

	@GET
	@Path("/events/listmovies/{genre}")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response eventsListmoviesByGenre(@Context HttpServletRequest reqContext,
			@PathParam("genre") String genre) {
		return events_list_movies(reqContext.getRemoteAddr(), genre);
	}

	// --------------------------------------------------------------------------------------------
	// Methods on GENRES
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/genres/list")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response genresList(@Context HttpServletRequest reqContext) {
		return genres_list(reqContext.getRemoteAddr(), null);
	}	

	@GET
	@Path("/genres/list/{genreId}")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response genresListById(@Context HttpServletRequest reqContext,
			@PathParam("genreId") String genreId) {
		return genres_list(reqContext.getRemoteAddr(), genreId);
	}	

	// --------------------------------------------------------------------------------------------
	// Methods on LOG
	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	// Methods on MOVIES
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/movies/list")
	@Produces(MediaType.APPLICATION_JSON) 
	// call exemple: movies?title=<text>&genre=<text>&fulldescr=<text>&special=<text>&ctrl=<text>
	public Response moviesList(@Context HttpServletRequest reqContext,
			@QueryParam("title") String title,
			@QueryParam("genre") String genre,
			@QueryParam("fulldescr") String fulldescr,
			@QueryParam("special") String special,
			@QueryParam("ctrl") String ctrl) {
		return movies_list(reqContext.getRemoteAddr(), title, genre, fulldescr, special, ctrl);
	}

	@GET
	@Path("/movies/listgenres")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response moviesListGenres(@Context HttpServletRequest reqContext) {
		return movies_list_genres(reqContext.getRemoteAddr());
	}

	@GET
	@Path("/movies/updctrl/{movieId}/{ctrl}")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response moviesUpdateCtrl(@Context HttpServletRequest reqContext,
			@PathParam("movieId") int movieId,
			@PathParam("ctrl") String ctrl) {
		return movies_updateCtrl(reqContext.getRemoteAddr(), movieId, ctrl);
	}

	// --------------------------------------------------------------------------------------------
	// Methods on PARAMS
	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	// Methods on SCHEDULES
	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	// Methods on SKIPCHANNELS
	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	// Methods on SPECIALS
	// --------------------------------------------------------------------------------------------
	@GET
	@Path("/specials/listattributes")
	@Produces(MediaType.APPLICATION_JSON) 
	public Response specialsListAttributes(@Context HttpServletRequest reqContext) {
		return specials_list_attributes(reqContext.getRemoteAddr());
	}

	
	// --------------------------------------------------------------------------------------------
	// PRIVATES
	// --------------------------------------------------------------------------------------------
	private Response channels_list(String remoteIp, String genreId) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: genreId = {}", remoteIp, myName, genreId);
		int id = (int) Utils.isValidId(genreId);
		if (id == -1)
			return WSResponse.Error(myName, Status.BAD_REQUEST, String.format("Invalid genre id: {}", genreId));

		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<ChannelBean> channels = db.channelsList(id);
			if (channels.isEmpty())
				return WSResponse.Error(myName, Status.NOT_FOUND, String.format("No channels found for genreId: {}", id));
			else
				return WSResponse.OK(channels);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}
	
	private Response controls_list(String remoteIp) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}", remoteIp, myName);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<String> controls =  db.controlsList();
			return WSResponse.OK(controls);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}
	
	private Response events_list_specials(String remoteIp) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}", remoteIp, myName);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<EventsSpecialBean> specials =  db.eventsListSpecials();
			return WSResponse.OK(specials);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}
	
	private Response events_list_movies(String remoteIp, String genre) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: genre = {}", remoteIp, myName, genre);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<MovieBean> movies =  db.eventsListMovies(genre);
			return WSResponse.OK(movies);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}

	private Response genres_list(String remoteIp, String genreId) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: genreId = {}", remoteIp, myName, genreId);
		int id = (int) Utils.isValidId(genreId);
		if (id == -1)
			return WSResponse.Error(myName, Status.BAD_REQUEST, String.format("Invalid genre id: {}", genreId));

		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<GenreBean> genres =  db.genresList(EpgDatabase.GenresEnabled.ALL, id);
			if (genres.isEmpty())
				return WSResponse.Error(myName, Status.NOT_FOUND, String.format("Genre not found: {}", id));
			else
				return WSResponse.OK(genres);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}

	private Response movies_list(String remoteIp, String title, String genre, String fulldescr, String special, String ctrl) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: title = {}, genre = {}, fulldescr = {}, special = {}, ctrl = {}", remoteIp, myName, title, genre, fulldescr, special, ctrl);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<MovieBean> movies =  db.moviesList(title, genre, fulldescr, special, ctrl);
			return WSResponse.OK(movies);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}

	private Response movies_list_genres(String remoteIp) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}", remoteIp, myName);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<StringBean> genres =  db.moviesListGenres();
			return WSResponse.OK(genres);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}

	private Response movies_updateCtrl(String remoteIp, int movieId, String ctrl) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}: movieId = {}, ctrl = {}", remoteIp, myName, movieId, ctrl);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			db.moviesUpdateCtrl(movieId, ctrl);
			return WSResponse.OK(null);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}

	private Response specials_list_attributes(String remoteIp) {
		StackTraceElement myName = Thread.currentThread().getStackTrace()[2];
		logger.info("{} -> {}", remoteIp, myName);
		try {
			EpgDatabase db = new EpgDatabase(PropertiesFile);
			List<String> specialLabels =  db.specialsListAttributes();
			return WSResponse.OK(specialLabels);
		} catch (DbException e) {
			return WSResponse.ServerError(myName, e.getMessage());
		}
	}
}
