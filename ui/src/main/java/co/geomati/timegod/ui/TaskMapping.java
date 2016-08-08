package co.geomati.timegod.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Task;
import co.geomati.timegod.jpa.TimeSegment;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class TaskMapping implements JsonSerializer<Task>,
		JsonDeserializer<Task> {

	private static final Gson DEFAULT_GSON = new Gson();

	public Task deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		EntityManager em = DBUtils.getEntityManager();
		JsonObject jsonTask = null;
		try {
			jsonTask = json.getAsJsonObject();
		} catch (IllegalStateException e) {
			throw new JsonParseException("Task object expected", e);
		}
		Task t = new Task();
		t.setName(jsonTask.get("name").getAsString());
		t.setCreationTime(jsonTask.get("creationTime").getAsLong());
		t.setKeywords(DEFAULT_GSON.fromJson(jsonTask.get("keywords"),
				String[].class));
		t.setCommonEstimation(getAsInteger(jsonTask.get("commonEstimation")));
		ArrayList<Estimation> estimations = new ArrayList<Estimation>();
		t.setEstimations(estimations);
		t.setIssues(DEFAULT_GSON.fromJson(jsonTask.get("issues"), int[].class));

		JsonObject jsonEstimations = jsonTask.get("estimations")
				.getAsJsonObject();
		Set<Entry<String, JsonElement>> entries = jsonEstimations.entrySet();
		for (Entry<String, JsonElement> entry : entries) {
			Estimation estimation = new Estimation();
			estimation.setValue(getAsInteger(entry.getValue()));
			estimation.setDeveloper(em.find(Developer.class, entry.getKey()));
			estimations.add(estimation);
		}

		ArrayList<TimeSegment> timeSegments = DEFAULT_GSON.fromJson(
				jsonTask.get("timeSegments"),
				new TypeToken<ArrayList<TimeSegment>>() {
				}.getType());
		t.setTimeSegments(timeSegments);
		return t;
	}

	private Integer getAsInteger(JsonElement jsonElement) {
		return jsonElement.isJsonNull() ? null : jsonElement.getAsInt();
	}

	public JsonElement serialize(Task src, Type typeOfSrc,
			JsonSerializationContext context) {
		JsonObject ret = new JsonObject();
		ret.addProperty("id", src.getId());
		ret.addProperty("name", src.getName());
		ret.addProperty("creationTime", src.getCreationTime());
		ret.add("keywords", DEFAULT_GSON.toJsonTree(src.getKeywords()));
		ret.add("issues", DEFAULT_GSON.toJsonTree(src.getIssues()));
		ret.addProperty("commonEstimation", src.getCommonEstimation());
		JsonObject jsonEstimations = new JsonObject();
		ret.add("estimations", jsonEstimations);
		ArrayList<Estimation> estimations = src.getEstimations();
		for (Estimation estimation : estimations) {
			jsonEstimations.addProperty(estimation.getDeveloper().getName(),
					estimation.getValue());
		}
		ret.add("timeSegments", DEFAULT_GSON.toJsonTree(src.getTimeSegments()));
		return ret;
	}

}
