package co.geomati.timegod.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;

import co.geomati.timegod.jpa.Developer;
import co.geomati.timegod.jpa.Estimation;
import co.geomati.timegod.jpa.Task;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TaskMapping implements JsonSerializer<Task>,
		JsonDeserializer<Task> {

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
		t.setCommonEstimation(getAsInteger(jsonTask.get("commonEstimation")));
		ArrayList<Estimation> estimations = new ArrayList<Estimation>();
		t.setEstimations(estimations);
		JsonObject jsonEstimations = jsonTask.get("estimations")
				.getAsJsonObject();
		Set<Entry<String, JsonElement>> entries = jsonEstimations.entrySet();
		for (Entry<String, JsonElement> entry : entries) {
			Estimation estimation = new Estimation();
			estimation.setValue(getAsInteger(entry.getValue()));
			estimation.setDeveloper(em.find(Developer.class, entry.getKey()));
			estimations.add(estimation);
		}
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
		ret.addProperty("commonEstimation", src.getCommonEstimation());
		JsonObject jsonEstimations = new JsonObject();
		ret.add("estimations", jsonEstimations);
		ArrayList<Estimation> estimations = src.getEstimations();
		for (Estimation estimation : estimations) {
			jsonEstimations.addProperty(estimation.getDeveloper().getName(),
					estimation.getValue());
		}

		return ret;
	}

}
