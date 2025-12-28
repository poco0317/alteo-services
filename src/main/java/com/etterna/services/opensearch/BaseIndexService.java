package com.etterna.services.opensearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.SlicedScroll;
import org.opensearch.client.opensearch._types.query_dsl.IdsQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.DeleteOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.etterna.services.model.IOpenSearchModel;
import com.etterna.util.LogRuntime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseIndexService<T extends IOpenSearchModel> implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	protected OpenSearchService search;
	
	protected static final int REQUEST_CHUNK_SIZE = 10000; // max
	protected static final int PARALLEL_SCROLL_REQ = 3; // multiplied by REQUEST_CHUNK_SIZE for record count to do threaded parallel slice search
	protected static final String SCROLL_TIME = "2s";
	protected static final String PARALLEL_SCROLL_TIME = "10s";
	
	/**
	 * Specify the index name
	 */
	public abstract String INDEX_NAME();
	
	/**
	 * Specify the generic class
	 */
	public abstract Class<T> getClazz();
	
	/**
	 * Basic search given a built SearchRequest. Limit of 10000 results.
	 */
	protected SearchResponse<T> searchInternal(SearchRequest req) {
		long t1 = System.currentTimeMillis();
		SearchResponse<T> o = search.search(req, getClazz());
		long t2 = System.currentTimeMillis();
		m_logger.info(" - search result took {}ms", t2-t1);
		return o;
	}
	
	/**
	 * Internal search method for repetitive scrolling searches, 10000 records at a time.
	 */
	protected SearchResponse<T> searchInternal(SearchRequest.Builder builder) {
		return searchInternal(builder, SCROLL_TIME);
	}
	
	/**
	 * Internal search method for repetitive scrolling searches, 10000 records at a time.
	 */
	protected SearchResponse<T> searchInternal(SearchRequest.Builder builder, String scrollTime) {
		SearchRequest.Builder req = builder.index(INDEX_NAME()).size(REQUEST_CHUNK_SIZE);
		if (scrollTime != null) {
			req.scroll(t->t.time(scrollTime));
		}
		return searchInternal(req.build());
	}
	
	/**
	 * Dump the results of a search of up to 10000 results
	 */
	protected List<T> searchDocuments(SearchRequest req) {
		return hits(searchInternal(req));
	}
	
	/**
	 * Dump the results of a search of arbitrary size.
	 * Forced no parallel searching
	 */
	protected List<T> searchDocuments(Supplier<SearchRequest.Builder> builder) {
		return searchDocuments(builder, SCROLL_TIME, false);
	}
	
	/**
	 * Dump the results of a search of arbitrary size
	 */
	protected List<T> searchDocuments(Supplier<SearchRequest.Builder> builder, boolean parallel) {
		return searchDocuments(builder, SCROLL_TIME, parallel);
	}
	
	/**
	 * Dump the results of a search of arbitrary size
	 */
	@LogRuntime
	protected List<T> searchDocuments(Supplier<SearchRequest.Builder> bgetter, String scrollTime, boolean parallel) {
		
		if (parallel) {
			m_logger.info(" + Executing parallel search on {}", INDEX_NAME());
			
			long count = count();
			if (count < 100) {
				return hits(searchInternal(bgetter.get()));
			}
			
			int PARALLEL_SEARCHES_COUNT = (int)(count / 1000L) + 1;
			
			long a1 = System.currentTimeMillis();
			
			ExecutorService executor = Executors.newWorkStealingPool();
			List<Future<List<T>>> futs = new ArrayList<>();
			for (int i = 0; i < PARALLEL_SEARCHES_COUNT; i++) {
				final int j = i;
				futs.add(executor.submit(
					new Callable<List<T>>() {
						@Override
						public List<T> call() throws Exception {
							SearchRequest.Builder b = bgetter.get().slice(
										new SlicedScroll.Builder()
										.id(j)
										.max(PARALLEL_SEARCHES_COUNT)
										.build()
										)
									.scroll(t->t.time(PARALLEL_SCROLL_TIME))
									.index(INDEX_NAME())
									.size(REQUEST_CHUNK_SIZE);
							
							return hits(searchInternal(b));
						}
					}
				));
			}
			
			executor.shutdown();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				m_logger.error(e.getMessage(), e);
			}
			
			@SuppressWarnings("unchecked")
			List<T> o = (List<T>) futs.stream().map((fut) -> {
				try {
					return fut.get();
				} catch (InterruptedException | ExecutionException e) {
					m_logger.error(e.getMessage(), e);
					return new ArrayList<>();
				}
			}).flatMap((l) -> 
				l.stream()
			).toList();
			
			long a2 = System.currentTimeMillis();
			m_logger.info(" - parallelscroll took {}ms", a2-a1);
			
			return o;
		}
		else {
			SearchResponse<T> resp = searchInternal(bgetter.get());
			if (resp.hits().total().value() > REQUEST_CHUNK_SIZE) {
				List<T> o = hits(resp);
				while (resp.hits().hits().size() >= REQUEST_CHUNK_SIZE) {
					long t1 = System.currentTimeMillis();
					ScrollRequest reqq = new ScrollRequest.Builder().scrollId(resp.scrollId()).scroll(t -> t.time(scrollTime)).build();
					resp = search.searchScroll(reqq, getClazz());
					long t2 = System.currentTimeMillis();
					o.addAll(hits(resp));
					m_logger.info(" -  scrollsearch took {}ms", t2-t1);
				}
				
				if (resp.scrollId() != null) {
					search.exitScroll(resp.scrollId());
				}
				return o;
			} else {
				if (resp.scrollId() != null) {
					search.exitScroll(resp.scrollId());
				}
				return hits(resp);
			}
		}
	}
	
	/**
	 * This is cringe
	 */
	protected List<T> hits(SearchResponse<T> resp) {
		return resp.hits().hits().stream().map(h -> h.source()).collect(Collectors.toList());
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		m_logger.info("Maintaining index {}", INDEX_NAME());
		CreateIndexRequest req = new CreateIndexRequest.Builder().index(INDEX_NAME()).build();
		//PutIndicesSettingsRequest settings = new PutIndicesSettingsRequest.Builder().settings(ix -> ix.)
		search.createIndex(req, null);
	}
	
	public long count() {
		CountRequest req = new CountRequest.Builder().index(INDEX_NAME()).build();
		return search.count(req);
	}
	
	/**
	 * Returns false if the document failed to save or was just updated instead of created
	 */
	public boolean save(T document, Refresh refresh) {
		m_logger.info("Saving {} {} - index {}", getClazz().getSimpleName(), document.openSearchId(), INDEX_NAME());
		IndexRequest<T> req = new IndexRequest.Builder<T>().index(INDEX_NAME()).id(document.openSearchId()).document(document).refresh(refresh).build();
		return search.saveToIndex(req);
	}
	
	/**
	 * Returns false if the document failed to save or was just updated instead of created
	 */
	public boolean saveBulk(Collection<T> documents, Refresh refresh) {
		if (documents.isEmpty()) return true;
		m_logger.info("Saving bulk {} (count {}) - index {}", documents.iterator().next().getClass().getSimpleName(), documents.size(), INDEX_NAME());
		List<BulkOperation> ops = documents.stream().map(doc -> {
			IndexOperation<T> op = new IndexOperation.Builder<T>().index(INDEX_NAME()).id(doc.openSearchId()).document(doc).build();
			return new BulkOperation.Builder().index(op).build();
		}).collect(Collectors.toList());
		BulkRequest req = new BulkRequest.Builder().index(INDEX_NAME()).operations(ops).refresh(refresh).build();
		return search.saveToIndex(req);
	}
	
	/**
	 * Get a single object by ID. Returns null if nonexistent
	 */
	public T findById(String id) {
		SearchRequest req = new SearchRequest.Builder().index(INDEX_NAME()).query(Query.of(a -> a.ids(IdsQuery.of(z -> z.values(id))))).build();
		HitsMetadata<T> hits = search.search(req, getClazz()).hits();
		if (hits.hits().size() == 0) {
			return null;
		}
		return hits.hits().get(0).source();
	}
	
	/**
	 * Get a single object by ID example, Returns null if nonexistent.
	 */
	public T findById(T example) {
		return findById(example.openSearchId());
	}
	
	/**
	 * Get everything in the index
	 */
	public List<T> findAll() {
		SearchRequest.Builder req = new SearchRequest.Builder().index(INDEX_NAME());
		return searchDocuments(() -> req, true);
	}
	
	/**
	 * Delete the given thing. Return false if nothing happened
	 */
	public boolean delete(T document, Refresh refresh) {
		return deleteById(document.openSearchId(), refresh);
	}
	
	/**
	 * Delete the given thing. Return false if nothing happened
	 */
	public boolean deleteById(String id, Refresh refresh) {
		m_logger.info("Deleting {} {} - index {}", getClazz().getSimpleName(), id, INDEX_NAME());
		DeleteRequest req = new DeleteRequest.Builder().index(INDEX_NAME()).id(id).refresh(refresh).build();
		return search.delete(req);
	}
	
	/**
	 * Return the amount of things deleted in the given list
	 */
	public int deleteBulk(Collection<T> documents, Refresh refresh) {
		if (documents.isEmpty()) return 0;
		m_logger.info("Deleting {} documents - index {}", documents.size(), INDEX_NAME());
		List<BulkOperation> ops = documents.stream().map(doc -> {
			DeleteOperation op = new DeleteOperation.Builder().index(INDEX_NAME()).id(doc.openSearchId()).build();
			return new BulkOperation.Builder().delete(op).build();
		}).collect(Collectors.toList());
		BulkRequest req = new BulkRequest.Builder().index(INDEX_NAME()).operations(ops).refresh(refresh).build();
		return search.delete(req);
	}

}
