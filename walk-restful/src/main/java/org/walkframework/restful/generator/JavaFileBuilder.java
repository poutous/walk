package org.walkframework.restful.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import org.walkframework.restful.exception.ConfigException;
import org.walkframework.restful.exception.ExcelDataException;
import org.walkframework.restful.exception.ExcelReadException;

/**
 * @author wangxin
 *
 */
public class JavaFileBuilder {

	public static Map<String, JavaFileMeta> translatorJavaFileMeta = new HashMap<String, JavaFileMeta>();

	public static StringBuilder ERROR = new StringBuilder();

	private int newFileCount = 0;

	private int errorCount = 0;

	private Config config;

	private JavaFileBuilder() {
	}

	private static JavaFileBuilder instance;

	public static synchronized JavaFileBuilder newInstance() {
		return instance == null ? new JavaFileBuilder() : instance;
	}

	@SuppressWarnings("finally")
	public JavaFileBuilder builder(Config config) {
		try {
			this.config = config.checkConfig();
			JavaFileType.init(config);
			File[] listFiles = new File(pathDeal(config.getExcelFileDir())).listFiles();
			for (File file : listFiles) {
				if (file.isDirectory()) {
					for (File subFile : file.listFiles()) {
						String appendPackageName = file.getName();
						if (!appendPackageName.matches("^[a-zA-z_][\\w_]*"))
							throw new ConfigException("请配置正确的二级文件夹名,可作为包名的一部分");
						JavaFileType.setAppendPackageName(appendPackageName);
						OneFilebuilder(subFile);
					}
				} else {
					JavaFileType.setAppendPackageName(null);
					OneFilebuilder(file);
				}
			}
		} catch (ConfigException e) {
			System.err.println((e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (errorCount > 0) {
				System.err.println(String.format("\n发现以下%d处错误:\n%s", errorCount, ERROR));
			}
			clear();
			return this;
		}
	}

	private void clear() {
		translatorJavaFileMeta.clear();
		ERROR.setLength(0);
		errorCount = 0;
	}

	/**
	 * 单Excel文件创建 .java
	 * 
	 * @param file
	 * @throws Exception
	 */
	private void OneFilebuilder(File file) {
		if (file.getName().toLowerCase().endsWith(".xls") || file.getName().toLowerCase().endsWith(".xlsx")) {
			infoLine(file.getName());
			// 生成请求报文
			infoLine("请求报文:");
			createJavaFile(file, JavaFileType.REQ);
			newFileCount = 0;
			// 生成返回报文
			infoLine("返回报文:");
			createJavaFile(file, JavaFileType.RSP);
			newFileCount = 0;
			if (translatorJavaFileMeta != null && translatorJavaFileMeta.size() > 0) {
				infoLine("属性翻译器:");
				createJavaFile(null, JavaFileType.TRANSLATOR);
			}
			newFileCount = 0;
			infoLine("");
		}
	}

	/**
	 * 生成class文件
	 * 
	 * @param javaFileTypeEnum
	 * @param file
	 * @throws Exception
	 */
	private void createJavaFile(File file, JavaFileType javaFileTypeEnum) {
		try {
			Map<String, JavaFileMeta> pageMetas = null;
			if (javaFileTypeEnum == JavaFileType.TRANSLATOR) {
				pageMetas = translatorJavaFileMeta;
			} else {
				pageMetas = new DataTransform(config).getClassMetas(file, javaFileTypeEnum);
			}
			if (pageMetas == null)
				return;
			for (Entry<String, JavaFileMeta> entry : pageMetas.entrySet()) {
				JavaFileMeta javaFileMeta = entry.getValue();
				String fileName = entry.getKey() + ".java";
				templeateParser(javaFileTypeEnum, fileName, javaFileMeta);
			}
		} catch (Exception e) {
			if (e instanceof ExcelDataException || e instanceof ExcelReadException) {
				String str = javaFileTypeEnum == JavaFileType.REQ ? "请求报文" : "返回报文 ";
				ERROR.append(String.format("\t%d. %s %s %s \n", ++errorCount, file.getPath(), str, e.getMessage()));
			} else {
				e.printStackTrace();
			}
			clearTranslatorjavaFileMeta(javaFileTypeEnum);
		}

	}

	// 当且仅当生成返回报文时报异常 才对translatorjavaFileMeta做出清理
	private void clearTranslatorjavaFileMeta(JavaFileType javaFileTypeEnum) {
		if (javaFileTypeEnum == JavaFileType.RSP)
			translatorJavaFileMeta.clear();
	}

	private void templeateParser(JavaFileType javaFileTypeEnum, String fileName, JavaFileMeta javaFileMeta) {
		String baseDirPath = pathDeal(config.getCodeDirPath()) + File.separator + javaFileTypeEnum.getPackageName().replace(".", File.separator);
		String longFileName = baseDirPath + File.separator + fileName;
		newFileCount++;
		if (!new File(longFileName).exists() || config.isOverride()) {
			if (new File(longFileName).exists()) {
				infoLine(String.format("\t%d. %s 已覆盖原文件", newFileCount, fileName));
			} else {
				infoLine(String.format("\t%d. %s 生成新文件", newFileCount, fileName));
			}
			String content = templateParser(config.getTemplateDirPath(), javaFileTypeEnum.getTemplateFileName(), javaFileMeta);
			File baseDir = new File(baseDirPath);
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			createFile(longFileName, content);
		} else {
			infoLine(String.format("\t%d. %s 已存在(覆盖原文件需配置config.setOverride(true))", newFileCount, fileName));
		}
	}

	/**
	 * 添加classPath的路径支持:classpath:xx 统一替换为 File.separator 路径分割符
	 * 
	 * @param filePath
	 * @return
	 */
	private String pathDeal(String filePath) {
		if (filePath.toLowerCase().startsWith("classpath:")) {
			filePath = (JavaFileBuilder.class.getResource("/").getFile() + filePath.substring(10)).substring(1).replace("//", File.separator);
			String canonicalPath = null;
			try {
				filePath = filePath.replace("/", File.separator).replace("\\", File.separator);
				canonicalPath = new File(filePath).getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return canonicalPath;
		} else {
			return filePath;
		}
	}

	/**
	 * 模板文件解析
	 * 
	 * @param templateDirPath
	 * @param templateFileName
	 * @param data
	 * @return
	 */
	private String templateParser(String templateDirPath, String templateFileName, Object data) {
		try {
			ResourceLoader resourceLoader = null;
			if (templateDirPath.toLowerCase().startsWith("classpath:")) {
				templateDirPath = templateDirPath.matches(".*/$|.*\\\\$") ? templateDirPath : templateDirPath + "/";
				resourceLoader = new ClasspathResourceLoader(this.getClass().getClassLoader(), templateDirPath.substring(10), config.getCharset());
			} else {
				resourceLoader = new FileResourceLoader(templateDirPath, config.getCharset());
			}
			Configuration cfg = Configuration.defaultConfiguration();
			GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
			Template t = gt.getTemplate(templateFileName);
			t.binding("root", data);
			String content = t.render();
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createFile(String savePath, String content) {
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(savePath)), this.config.getCharset()));
			output.write(content);
			output.flush();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void infoLine(String info) {
		System.out.println(info);
	}

}
