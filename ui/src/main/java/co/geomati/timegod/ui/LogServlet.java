package co.geomati.timegod.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import co.geomati.timegod.jpa.LogEvent;

@WebServlet("/log")
public class LogServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String sql = "SELECT e FROM LogEvent e";

		int dayCount = 5;
		try {
			dayCount = Integer.parseInt(req.getParameter("dayCount"));
		} catch (NumberFormatException e) {
		}
		long now = new Date().getTime();
		sql += " WHERE e.timestamp > " + (now - 24 * 60 * 60 * 1000 * dayCount);
		sql += " ORDER BY e.timestamp DESC";

		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<LogEvent> query = em.createQuery(sql, LogEvent.class);
		List<LogEvent> list = query.getResultList();

		resp.setContentType("text/plain");
		PrintWriter writer = resp.getWriter();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		for (LogEvent logEvent : list) {
			Date date = new Date(logEvent.getTimestamp());
			StringBuilder builder = new StringBuilder();
			builder.append(sdf.format(date));
			builder.append(" - ");
			builder.append(logEvent.getDeveloper().getName());
			builder.append(" - ");
			builder.append(logEvent.getEventName());
			writer.println(builder.toString());
			writer.println(logEvent.getPayload());
			writer.println();
		}
	}
}
