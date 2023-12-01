package lh.demo.it.request.api;

import java.util.List;

public record Paginated<T>(Long totalRecords, Long totalPages, List<T> data) {}
