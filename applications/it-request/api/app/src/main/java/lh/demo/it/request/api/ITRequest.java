package lh.demo.it.request.api;

public record ITRequest(String id, Status status, String requesterEmail, String description, String comments) {}
