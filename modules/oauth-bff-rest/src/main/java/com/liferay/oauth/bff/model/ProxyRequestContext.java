package com.liferay.oauth.bff.model;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

/**
 * @author Marcel Tanuri
 */
public class ProxyRequestContext {

	public InputStream getBody() {
		return _body;
	}

	public HttpHeaders getHeaders() {
		return _headers;
	}

	public String getMethod() {
		return _method;
	}

	public String getPath() {
		return _path;
	}

	public String getQueryString() {
		return _queryString;
	}

	public HttpServletRequest getServletRequest() {
		return _servletRequest;
	}

	public UriInfo getUriInfo() {
		return _uriInfo;
	}

	public static class Builder {

		public Builder body(InputStream body) {
			_body = body;

			return this;
		}

		public ProxyRequestContext build() {
			return new ProxyRequestContext(this);
		}

		public Builder headers(HttpHeaders headers) {
			_headers = headers;

			return this;
		}

		public Builder method(String method) {
			_method = method;

			return this;
		}

		public Builder path(String path) {
			_path = path;

			return this;
		}

		public Builder queryString(String queryString) {
			_queryString = queryString;

			return this;
		}

		public Builder servletRequest(HttpServletRequest servletRequest) {
			_servletRequest = servletRequest;

			return this;
		}

		public Builder uriInfo(UriInfo uriInfo) {
			_uriInfo = uriInfo;

			return this;
		}

		private InputStream _body;
		private HttpHeaders _headers;
		private String _method;
		private String _path;
		private String _queryString;
		private HttpServletRequest _servletRequest;
		private UriInfo _uriInfo;

	}

	private ProxyRequestContext(Builder builder) {
		_method = builder._method;
		_path = builder._path;
		_queryString = builder._queryString;
		_headers = builder._headers;
		_body = builder._body;
		_servletRequest = builder._servletRequest;
		_uriInfo = builder._uriInfo;
	}

	private final InputStream _body;
	private final HttpHeaders _headers;
	private final String _method;
	private final String _path;
	private final String _queryString;
	private final HttpServletRequest _servletRequest;

	// 🔨 Fluent Builder

	private final UriInfo _uriInfo;

}