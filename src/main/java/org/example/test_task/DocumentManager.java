package org.example.test_task;

import lombok.Builder;
import lombok.Data;

import javax.print.Doc;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */

    //Document storage
    private final Map<String, Document> storage = new HashMap<>();

    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        Document existing = storage.get(document.getId());
        if (existing != null) {
            existing.setTitle(document.getTitle());
            existing.setContent(document.getContent());
            existing.setAuthor(document.getAuthor());
        } else {
            storage.get(document.getId());
        }

        return storage.get(document.getId());
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {

        if (request == null) {
            return new ArrayList<>(storage.values());
        }

        return storage.values().stream()
                .filter(doc -> matchesTitlePrefix(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContainsContests(doc, request.getContainsContents()))
                .filter(doc -> matchesAuthorIds(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    private boolean matchesTitlePrefix(Document doc, List<String> titlePrefix) {
        if (titlePrefix == null || titlePrefix.isEmpty()) {
            return true;
        }
        return titlePrefix.stream().anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    private boolean matchesContainsContests(Document doc, List<String> containsContents) {
        if (containsContents == null || containsContents.isEmpty()) {
            return true;
        }
        return containsContents.stream().anyMatch(content -> doc.getContent().startsWith(content));
    }

    private boolean matchesAuthorIds(Document doc, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        return authorIds.contains(doc.getAuthor().getId());
    }

    private boolean matchesCreatedRange(Document doc, Instant createdFrom, Instant createdTo) {
        Instant created = doc.getCreated();
        if (createdFrom != null && created.isBefore(createdFrom)) {
            return false;   //Document created earlier than createdFrom
        }

        if (createdTo != null && created.isBefore(createdTo)) {
            return false;   //Document created earlier than createdTo
        }
        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return Optional.empty();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}