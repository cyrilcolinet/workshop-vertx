package eu.epitech.dashboard.api.controllers.objects;

import io.vertx.core.json.JsonArray;

public class ErrorResponse {

    private Integer statusCode;
    private String message;
    private JsonArray extra = new JsonArray();

    public ErrorResponse(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setExtra(JsonArray extra) {
        this.extra = extra;
    }
}
