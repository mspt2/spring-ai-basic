package com.samsungsds.springai.service;

import com.samsungsds.springai.reader.CSVReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class DataServiceImpl implements DataService {

    private final VectorStore vectorStore;
    private final CSVReader csvReader;
    private final ExecutorService executorService;

    private static final int BATCH_SIZE = 5;
    private static final int THREAD_POOL_SIZE = 4;

    public DataServiceImpl(VectorStore vectorStore, CSVReader csvReader) {
        this.vectorStore = vectorStore;
        this.csvReader = csvReader;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    @Async
    public void storeData() {
        List<Document> documents = csvReader.loadCsvToDocument("src/main/resources/bbc_news.csv");

        List<List<Document>> batches = createBatches(documents);

        List<CompletableFuture<Void>> futures = batches.stream()
                .map(batch -> CompletableFuture.runAsync(() -> processAndStoreBatch(batch), executorService))
                .toList();

        // 모든 배치 처리가 완료될 때까지 기다린다.
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public List<Document> searchData(String searchQuery) {
        return vectorStore.similaritySearch(SearchRequest.query(searchQuery).withTopK(5));
    }

    private List<List<Document>> createBatches(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += BATCH_SIZE) {
            batches.add(documents.subList(i, Math.min(i + BATCH_SIZE, documents.size())));
        }
        return batches;
    }

    private void processAndStoreBatch(List<Document> batch) {
        try {
            vectorStore.add(batch);
            Thread.sleep(1000); // 요청 간 1초 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }



}
