package lh.demo.it.request.api;

import java.util.List;

public record Paginated<T>(List<T> data, String bookmark) {}
