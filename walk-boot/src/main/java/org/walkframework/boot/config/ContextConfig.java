package org.walkframework.boot.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextRuleSet;
import org.apache.catalina.startup.NamingRuleSet;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class ContextConfig {
	
	private static final Log log = LogFactory.getLog(ContextConfig.class);
	
	protected static final StringManager sm = StringManager.getManager(Constants.Package);
	
	public void processContextConfig(Context ctx, URL contextXml) {
		Digester digester = createContextDigester();
		digester.getParser();
		
		if (log.isDebugEnabled()) {
			log.debug("Processing context [" + ctx.getName() + "] configuration file [" + contextXml + "]");
		}

		InputSource source = null;
		InputStream stream = null;

		try {
			source = new InputSource(contextXml.toString());
			URLConnection xmlConn = contextXml.openConnection();
			xmlConn.setUseCaches(false);
			stream = xmlConn.getInputStream();
		} catch (Exception e) {
			log.error(sm.getString("contextConfig.contextMissing", contextXml), e);
		}

		if (source == null) {
			return;
		}
		try {
			source.setByteStream(stream);
			digester.setClassLoader(this.getClass().getClassLoader());
			digester.setUseContextClassLoader(false);
			digester.push(ctx.getParent());
			digester.push(ctx);
			XmlErrorHandler errorHandler = new XmlErrorHandler();
			digester.setErrorHandler(errorHandler);
			digester.parse(source);
			if (errorHandler.getWarnings().size() > 0 || errorHandler.getErrors().size() > 0) {
				errorHandler.logFindings(log, contextXml.toString());
			}
			if (log.isDebugEnabled()) {
				log.debug("Successfully processed context [" + ctx.getName() + "] configuration file [" + contextXml + "]");
			}
		} catch (SAXParseException e) {
			log.error(sm.getString("contextConfig.contextParse", ctx.getName()), e);
			log.error(sm.getString("contextConfig.defaultPosition", "" + e.getLineNumber(), "" + e.getColumnNumber()));
		} catch (Exception e) {
			log.error(sm.getString("contextConfig.contextParse", ctx.getName()), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				log.error(sm.getString("contextConfig.contextClose"), e);
			}
		}
	}
	
	public Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRulesValidation(true);
        HashMap<Class<?>, List<String>> fakeAttributes = new HashMap<Class<?>, List<String>>();
        ArrayList<String> attrs = new ArrayList<String>();
        attrs.add("className");
        fakeAttributes.put(Object.class, attrs);
        digester.setFakeAttributes(fakeAttributes);
        RuleSet contextRuleSet = new ContextRuleSet("", false);
        digester.addRuleSet(contextRuleSet);
        RuleSet namingRuleSet = new NamingRuleSet("Context/");
        digester.addRuleSet(namingRuleSet);
        return digester;
    }
}
