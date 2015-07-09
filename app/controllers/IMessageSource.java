package controllers;

import com.fasterxml.jackson.databind.JsonNode;

public interface IMessageSource {
    JsonNode getJsonData();
    String getMessageFromData(JsonNode data);
    String getSenderFromData(JsonNode data);
}
