package com.etterna.services.opensearch;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.PutIndicesSettingsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.client.transport.httpclient5.ResponseException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpenSearchService {
	
	private OpenSearchClient client = null;

	private OpenSearchClient client() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		if (this.client != null) return this.client;
		//System.setProperty("javax.net.ssl.trustStore", "/full/path/to/keystore");
		//System.setProperty("javax.net.ssl.trustStorePassword", "password-to-keystore");

		final HttpHost host = new HttpHost("http", "localhost", 9200);
		final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		//credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials("admin", "admin".toCharArray()));

		final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);
		builder.setHttpClientConfigCallback(httpClientBuilder -> {
			try {
				final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
						.setSslContext(SSLContextBuilder.create().build())
						// See https://issues.apache.org/jira/browse/HTTPCLIENT-2219
						.setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
							@Override
							public TlsDetails create(final SSLEngine sslEngine) {
								return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
							}
						})
						.build();
	
				final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
						.create()
						.setTlsStrategy(tlsStrategy)
						.build();
	
				return httpClientBuilder
						.setDefaultCredentialsProvider(credentialsProvider)
						.setConnectionManager(connectionManager);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		});

		final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
		OpenSearchClient client = new OpenSearchClient(transport);
		this.client = client;
		return client;
	}

	/**
	 * HIGHLY cringe that I have to do this
	 */
	private static boolean contains429(ResponseException e) {
		return e.getMessage().contains(" 429 Too Many Requests");
	}
	
	/**
	 * HIGHLY cringe also
	 */
	private static void wait1sec() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}
	
	public void createIndex(CreateIndexRequest req, PutIndicesSettingsRequest settings) {
		try {
			if (!client().indices().exists(f -> f.index(req.index())).value()) {
				client().indices().create(req);
				if (settings != null) {
					client().indices().putSettings(settings);
				}
			}
		} catch (KeyManagementException | OpenSearchException | NoSuchAlgorithmException | KeyStoreException
				| IOException e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	public <T> boolean saveToIndex(IndexRequest<T> req) {
		try {
			return client().index(req).result() == Result.Created;
		} catch (KeyManagementException | OpenSearchException | NoSuchAlgorithmException | KeyStoreException
				| IOException e) {
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public boolean saveToIndex(BulkRequest req) {
		try {
			return client().bulk(req).errors() == false;
		} catch (KeyManagementException | OpenSearchException | NoSuchAlgorithmException | KeyStoreException
				| IOException e) {
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public <T> SearchResponse<T> search(SearchRequest req, Class<T> clazz) {
		try {
			return client().search(req, clazz);
		} catch (ResponseException e) {
			if (contains429(e)) {
				wait1sec();
				return search(req, clazz);
			} else {
				m_logger.error(e.getMessage(), e);
				return new SearchResponse.Builder<T>().build();
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return new SearchResponse.Builder<T>().build();
		}
	}
	
	public <T> ScrollResponse<T> searchScroll(ScrollRequest req, Class<T> clazz) {
		try {
			return client().scroll(req, clazz);
		} catch (ResponseException e) {
			if (contains429(e)) {
				wait1sec();
				return searchScroll(req, clazz);
			} else {
				m_logger.error(e.getMessage(), e);
				return new ScrollResponse.Builder<T>().build();
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return new ScrollResponse.Builder<T>().build();
		}
	}
	
	public boolean exitScroll(String scrollId) {
		try {
			return client().clearScroll(r -> r.scrollId(scrollId)).succeeded();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public long count(CountRequest req) {
		try {
			return client().count(req).count();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return 0;
		}
	}
	
	public boolean delete(DeleteRequest req) {
		try {
			return client().delete(req).result() == Result.Deleted;
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public int delete(BulkRequest req) {
		try {
			return client().bulk(req).items().size();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return 0;
		}
	}
	
	public long deleteByQuery(DeleteByQueryRequest req) {
		try {
			return client().deleteByQuery(req).total();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return 0;
		}
	}

}
