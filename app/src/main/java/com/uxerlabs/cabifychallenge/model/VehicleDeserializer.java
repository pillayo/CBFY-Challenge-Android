package com.uxerlabs.cabifychallenge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * GSON Deserializer for Vehicle/Service
 * @author Francisco Cuenca on 18/10/16.
 */

public class VehicleDeserializer implements JsonDeserializer<Vehicle> {

    @Override
    public Vehicle deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        String price = jsonObject.get("price_formatted").getAsString();
        final JsonObject vehicleType = jsonObject.getAsJsonObject("vehicle_type");
        String name = vehicleType.get("name").getAsString();
        String id = vehicleType.get("_id").getAsString();
        final JsonObject icon = vehicleType.getAsJsonObject("icons");
        String URLIcon = icon.get("regular").getAsString();

        final Vehicle vehicle = new Vehicle(id, name, URLIcon, price);

        return vehicle;
    }
}