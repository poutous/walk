package org.walkframework.shiro.web.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解决request.getInputStream()只能读取一次的问题
 * 
 * @see https://blog.csdn.net/qq_24698193/article/details/76725424
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger log = LoggerFactory.getLogger(BodyReaderHttpServletRequestWrapper.class);

	private static final Charset CHARSET = Charset.forName("UTF-8");

	private final byte[] body;

	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		body = getBodyString(request).getBytes(CHARSET);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(body);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return bais.read();
			}
		};
	}

	public String getBodyString(ServletRequest request) {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = request.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return sb.toString();
	}
}
