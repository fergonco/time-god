package co.geomati.timegod.ui;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import co.geomati.timegod.ui.callbacks.ReportTaskTimesCallback;
import co.geomati.websocketBus.WebsocketBus;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@WebServlet("/report-times")
public class TimeReportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BufferedReader reader = req.getReader();
		String json = IOUtils.toString(reader);
		JsonElement payload;
		try {
			payload = new JsonParser().parse(json);
			new ReportTaskTimesCallback().messageReceived(null,
					WebsocketBus.INSTANCE, ReportTaskTimesCallback.EVENT_NAME,
					payload);
			resp.setStatus(201);
		} catch (JsonSyntaxException e) {
			resp.sendError(400);
		} catch (RuntimeException e) {
			resp.sendError(400);
		}
	}
}
