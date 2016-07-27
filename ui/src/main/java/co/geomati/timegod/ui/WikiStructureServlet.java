package co.geomati.timegod.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import co.geomati.timegod.jpa.Poker;
import co.geomati.timegod.jpa.Task;

@WebServlet("/wiki-commands")
public class WikiStructureServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String sql = "SELECT e FROM Poker e";

		EntityManager em = DBUtils.getEntityManager();
		TypedQuery<Poker> query = em.createQuery(sql, Poker.class);
		List<Poker> list = query.getResultList();

		resp.setContentType("text/plain");
		PrintWriter writer = resp.getWriter();
		for (Poker poker : list) {
			writer.println("mkdir " + poker.getName());
			ArrayList<Task> tasks = poker.getTasks();
			for (Task task : tasks) {
				writer.println("touch " + urlize(poker.getName()) + "/"
						+ urlize(task.getName()) + ".md");
			}
		}
	}

	private String urlize(String text) {
		return latinize(text).replaceAll("\\s", "_");
	}

	public String latinize(String s) {
		String strTemp = Normalizer.normalize(s, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(strTemp).replaceAll("");
	}

}
