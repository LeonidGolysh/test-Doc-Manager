package org.example.test_task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentManagerTest {
    DocumentManager manager;
    String id;

    @BeforeEach
    void setUp() {
        manager = new DocumentManager();
        id = UUID.randomUUID().toString();
    }

    @Test
    void save_generateIdForNewDocumentTest() {
        DocumentManager.Document document = createDocument(null, "Title 1", "Content 1", null ,null);

        DocumentManager.Document savedDocument = manager.save(document);

        assertThat(savedDocument.getId()).isNotNull();
        assertThat(manager.findById(savedDocument.getId()).orElse(null)).isEqualTo(savedDocument);
    }

    @Test
    void save_updateExistingDocumentTest() {
        DocumentManager.Document original = createDocument(id, "Create Title", "Create Content", "author1" ,"John Doe");
        manager.save(original);

        DocumentManager.Document updated = createDocument(id, "Update Title", "Update Content", "author2", "Bob");

        DocumentManager.Document result = manager.save(updated);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Update Title");
        assertThat(result.getContent()).isEqualTo("Update Content");
    }

    @Test
    void save_notOverrideCreatedFieldWhenUpdatingTest() {
        Instant originalCreate = Instant.now();
        DocumentManager.Document original = createDocument(id, "Create Title", "Create Content", "author1", "John Doe");
        original.setCreated(originalCreate);
        manager.save(original);

        DocumentManager.Document updated = createDocument(id, "Update Title", "Update Content", "author2", "Bob");

        DocumentManager.Document result = manager.save(updated);

        assertThat(result.getCreated()).isEqualTo(originalCreate);
    }

    @Test
    void save_handleEmptyIdGracefullyTest() {
        DocumentManager.Document document = createDocument("", "Title", "Content", "author1" ,"John Doe");

        DocumentManager.Document savedDocument = manager.save(document);

        assertThat(savedDocument.getId()).isNotEmpty();
        assertThat(manager.findById(savedDocument.getId()).orElse(null)).isEqualTo(savedDocument);
    }

    private DocumentManager.Document createDocument(String id, String title, String content, String idAuthor, String name) {
        return DocumentManager.Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(createAuthor(idAuthor, name))
                .created(Instant.now())
                .build();
    }

    private DocumentManager.Author createAuthor(String idAuthor, String name) {
        return DocumentManager.Author.builder()
                .id(idAuthor)
                .name(name)
                .build();
    }

    @Test
    void findByIdTest() {
        DocumentManager.Document document = createDocument(id, "Title", "Content", "author1" ,"John Doe");
        manager.save(document);

        Optional<DocumentManager.Document> found = manager.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Title");
    }

    @Test
    void searchByTitlePrefixesTest() {
        DocumentManager.Document doc1 = createDocument("doc1", "Java Title", "Content 1", "author1" ,"John Doe");
        DocumentManager.Document doc2 = createDocument("doc2", "Py Title", "Content 2", "author2" ,"Bob");

        manager.save(doc1);
        manager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Title");
    }

    @Test
    void searchByContentContainsTest() {
        DocumentManager.Document doc1 = createDocument("doc1", "Doc1", "Content 1", "author1" ,"John Doe");
        DocumentManager.Document doc2 = createDocument("doc2", "Doc2", "Content 2", "author2" ,"Bob");

        manager.save(doc1);
        manager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("Content 1"))
                .build();

        List<DocumentManager.Document> results = manager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).isEqualTo("Content 1");
    }

    @Test
    void searchByCreatedRangeTest() {
        Instant now = Instant.now();
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .id("doc1")
                .title("Doc 1")
                .content("Content 1")
                .author(new DocumentManager.Author("author1", "John Doe"))
                .created(now.minusSeconds(3600))
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .id("doc2")
                .title("Doc 2")
                .content("Content 2")
                .author(new DocumentManager.Author("author2", "Bob"))
                .created(now.minusSeconds(7200))
                .build();

        manager.save(doc1);
        manager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(3600))
                .build();

        List<DocumentManager.Document> results = manager.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCreated()).isEqualTo(now.minusSeconds(3600));
    }
}
