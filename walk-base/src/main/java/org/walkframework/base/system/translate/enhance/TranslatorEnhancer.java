package org.walkframework.base.system.translate.enhance;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.walkframework.base.system.exception.MultipleTranslatorException;
import org.walkframework.base.system.translate.SqlTranslator;
import org.walkframework.base.system.translate.EntityTranslator;
import org.walkframework.base.system.translate.TableTranslator;
import org.walkframework.base.system.translate.Translators;
import org.walkframework.base.tools.utils.PathClassLoader;
import org.walkframework.data.translate.SqlTranslate;
import org.walkframework.data.translate.EntityTranslate;
import org.walkframework.data.translate.StaticTranslate;
import org.walkframework.data.translate.TableTranslate;
import org.walkframework.data.translate.Translate;
import org.walkframework.data.translate.TranslateEnable;
import org.walkframework.data.translate.Translator;

/**
 * 翻译增强器
 * 
 * @author shf675
 * 
 */
public class TranslatorEnhancer implements InitializingBean {
	
	private final static Logger log = LoggerFactory.getLogger(TranslatorEnhancer.class);

	private static final int BUFFER_SIZE = 32 * 1024;

	private Translators translatorsInstance = Translators.getInstance();

	/**
	 * 翻译器
	 */
	private Map<String, Translator> translators;

	/**
	 * entity路径
	 */
	private Set<String> locations;

	/**
	 * 增强标识
	 */
	private final static String _ENHANCED = "_ENHANCED";

	/**
	 * translator方法
	 */
	private final static StringBuilder translator = new StringBuilder();

	static {
		translator.append(" private Object translator(String annotationClassName, String translatorName, String translatedField) {\n");
		translator.append("	 try {\n");
		translator.append("		Class translatorsClazz = Class.forName(\"").append(Translators.class.getName()).append("\");\n");
		translator.append("		Object translatorsInstance = translatorsClazz.getDeclaredMethod(\"getInstance\", null).invoke(null, null);\n");
		translator.append("		java.lang.reflect.Method translateMethod = translatorsClazz.getDeclaredMethod(\"translate\", new Class[]{Object.class, String.class, String.class, String.class});\n");
		translator.append("		return translateMethod.invoke(translatorsInstance, new Object[]{this, translatedField, annotationClassName, translatorName});\n");
		translator.append("	 } catch (Exception e) {throw new RuntimeException(e);}\n");
		translator.append("	 return null;\n");
		translator.append(" }\n");
	}

	/**
	 * 所有的属性被初始化后调用
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// 添加默认
		translatorsInstance.putTranslator(StaticTranslate.class.getName(), new EntityTranslator());
		translatorsInstance.putTranslator(TableTranslate.class.getName(), new TableTranslator());
		translatorsInstance.putTranslator(EntityTranslate.class.getName(), new EntityTranslator());
		translatorsInstance.putTranslator(SqlTranslate.class.getName(), new SqlTranslator());
		// Translate特殊，初始化一个空处理器
		translatorsInstance.putTranslator(Translate.class.getName(), Translators.EMPTY_TRANSLATOR);

		// 添加自定义
		if (this.translators != null) {
			translatorsInstance.putTranslators(translators);
		}

		load();
	}

	/**
	 * spring
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Set<String> locations = getLocations();
		if (locations != null) {
			// 1、遍历class文件，同时排重
			Map<String, Resource> noDuplicateResources = new HashMap<String, Resource>();
			for (String location : locations) {
				String path = "classpath*:" + location.replaceAll("\\.", "/") + "/*.class";
				Resource[] resources = resourcePatternResolver.getResources(path);
				for (Resource re : resources) {
					noDuplicateResources.put(re.getURL().getPath(), re);
				}
			}
			//2、开始加强
			Map<String, CtClass> inBootJarClass = new HashMap<String, CtClass>();
			for (Map.Entry<String, Resource> entry : noDuplicateResources.entrySet()) {
				Resource resource = entry.getValue();
				Class<?> clazz = loadClass(resource.getURL());
				if (clazz != null) {
					TranslateEnable translateEnable = clazz.getAnnotation(TranslateEnable.class);
					if (translateEnable != null) {
						boolean hasEnhanced = true;
						try {
							clazz.getDeclaredField(_ENHANCED);
						} catch (Exception e) {
							hasEnhanced = false;
						}
						if (!hasEnhanced) {
							startEnhance(resource.getURL(), inBootJarClass);
						}
					}
				}
			}

			//如果class文件在springboot方式打的jar中...
			if (!inBootJarClass.isEmpty()) {
				writeToBootJar(inBootJarClass);
			}
		}
	}

	/**
	 * 加载class
	 * 
	 * @param filePath
	 * @return
	 */
	private Class<?> loadClass(URL filePath) {
		Class<?> clazz = null;
		try {
			clazz = new PathClassLoader().loadClass(filePath.openStream());
		} catch (Exception e) {
			log.error("class[{}] load error.", filePath, e);
		}
		return clazz;
	}

	/**
	 * 增强类
	 * 
	 * @param is
	 * @param resource
	 * @return
	 */
	public void startEnhance(URL url, Map<String, CtClass> inBootJarClass) {
		CtClass ctClass = null;
		String classAbsolutePath = url.getPath();
		try {
			ClassPool pool = ClassPool.getDefault();
			ctClass = pool.makeClass(url.openStream(), false);

			boolean hasAddEnhance = false;
			CtField[] ctFields = ctClass.getDeclaredFields();
			for (CtField ctField : ctFields) {
				String translateAnnotationName = getTranslatorAnnotationName(ctField.getAnnotations());
				if (translateAnnotationName != null) {
					String translatorName = "";
					if (Translate.class.getName().equals(translateAnnotationName)) {
						Translate translate = (Translate) ctField.getAnnotation(Translate.class);
						translatorName = translate.translator() != null && !Translator.class.equals(translate.translator()) ? translate.translator().getName() : translate.translatorClassName();
					}
					String fieldName = ctField.getName();
					String methodName = new StringBuilder().append("get").append(Character.toUpperCase(fieldName.charAt(0))).append(fieldName.substring(1)).toString();
					CtMethod getMethod = ctClass.getDeclaredMethod(methodName);
					if (getMethod != null) {
						if (!hasAddEnhance) {
							// 1、添加全局变量标识是否已增强
							ctClass.addField(CtField.make(" private final boolean " + _ENHANCED + " = true;\n", ctClass));
							//2、所有值
							ctClass.addField(CtField.make(" private final java.util.Map _FIELD_SET_VALUE_SIGN = new java.util.HashMap();\n", ctClass));
							// 2、添加translator方法
							ctClass.addMethod(CtMethod.make(translator.toString(), ctClass));
						}
						// 3、get方法增强

						String fieldType = Descriptor.toClassName(ctField.getFieldInfo2().getDescriptor());
						pool.makeClass(fieldType);
						StringBuilder src = new StringBuilder();
						src.append("if(_FIELD_SET_VALUE_SIGN.get(\"").append(fieldName).append("\") == null){ \n");
						src.append(fieldName).append(" = (").append(fieldType).append(")translator(\"").append(translateAnnotationName).append("\",\"").append(translatorName).append("\",\"").append(fieldName).append("\");\n");
						src.append("_FIELD_SET_VALUE_SIGN.put(\"" + fieldName + "\",\"1\");\n");
						src.append("}\n");
						getMethod.insertBefore(src.toString());

						// 4、标记
						hasAddEnhance = true;
					}
				}
			}
			// 5、写入文件
			if (hasAddEnhance) {
				if (isSpringBootBuildJar()) {
					String entryName = getClassPathInBootJar(classAbsolutePath);
					if(entryName.indexOf(".jar!") > -1){
						entryName = "BOOT-INF/classes" + entryName.substring(entryName.indexOf("jar!") + 4, entryName.length());
						//判断BOOT-INF/lib下jar中class文件是否已经在BOOT-INF/classes下生成增强类
						if(new JarFile(getBootJarPath()).getEntry(entryName) == null){
							inBootJarClass.put(entryName, ctClass);
						}
					} else if(entryName.indexOf(".war!") > -1){
						entryName = "WEB-INF/classes" + entryName.substring(entryName.indexOf("war!") + 4, entryName.length());
						if(new JarFile(getBootJarPath()).getEntry(entryName) == null){
							inBootJarClass.put(entryName, ctClass);
						}
					} else {
						inBootJarClass.put(entryName, ctClass);
					}
				} else {
					String classPath = this.getClass().getClassLoader().getResource("").getPath();
					ctClass.writeFile(classPath);
					log.info("enhance classFile[{}] success!", classAbsolutePath);
				}
			}
		} catch (Exception e) {
			log.error("enhance classFile[{}] failure!", classAbsolutePath);
			e.printStackTrace();
		}
	}

	/**
	 * 向springboot打的jar中写入增强后的class
	 * 
	 * @param inBootJarClass
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void writeToBootJar(Map<String, CtClass> inBootJarClass) throws Exception {
		String bootJarPath = getBootJarPath();
		JarFile jarFile = new JarFile(bootJarPath);
		TreeMap<String, byte[]> entries = new TreeMap<String, byte[]>();
		Enumeration es = jarFile.entries();
		while (es.hasMoreElements()) {
			JarEntry entry = (JarEntry) es.nextElement();
			entries.put(entry.getName(), toBytes(jarFile.getInputStream(entry)));
		}

		JarOutputStream jos = new JarOutputStream(new FileOutputStream(bootJarPath));
		try {
			for (Map.Entry<String, byte[]> item : entries.entrySet()) {
				String entryName = item.getKey();
				byte[] entryOriginalBytes = item.getValue();
				byte[] entryBytes = entryOriginalBytes;

				JarEntry entry = new JarEntry(entryName);
				boolean inBootJar = false;
				if (inBootJarClass.containsKey(entryName)) {
					entryBytes = inBootJarClass.get(entryName).toBytecode();
					
					//处理完移除
					inBootJarClass.remove(entryName);
					inBootJar = true;
				}
				writeEntry(jos, entry, entryBytes);
				
				if(inBootJar){
					log.info("enhance classFile[{}] in boot jar success!", entryName);
				}
			}
			
			//处理在lib目录下jar中的class文件，直接写入BOOT-INF/classes目录下
			if(!inBootJarClass.isEmpty()){
				for (Map.Entry<String, CtClass> item : inBootJarClass.entrySet()) {
					writeEntry(jos, new JarEntry(item.getKey()), item.getValue().toBytecode());
					log.info("enhance classFile[{}] in boot jar include jar success!", item.getKey());
				}
			}
		} finally {
			jos.close();
		}
	}

	/**
	 * 写入
	 * 
	 * @param jos
	 * @param entry
	 * @param bytes
	 * @throws IOException
	 */
	public void writeEntry(JarOutputStream jos, JarEntry entry, byte[] bytes) throws IOException {
		CRC32 crc = new CRC32();
		crc.update(bytes);

		entry.setSize(bytes.length);
		entry.setCompressedSize(bytes.length);
		entry.setCrc(crc.getValue());
		entry.setMethod(ZipEntry.STORED);

		jos.putNextEntry(entry);
		jos.write(bytes, 0, bytes.length);
		jos.flush();
		jos.closeEntry();
	}

	/** 
	 * 字节流转字节数组
	 *  
	 * @param inStream 
	 * @return 字节数组 
	 * @throws Exception 
	 */
	public byte[] toBytes(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int len = -1;
			while ((len = inStream.read(buffer)) != -1) {
				outSteam.write(buffer, 0, len);
			}
		} finally {
			outSteam.close();
			inStream.close();
		}
		return outSteam.toByteArray();
	}

	/**
	 * 判断是否是springboot打的jar
	 * 
	 * @return
	 */
	public boolean isSpringBootBuildJar() {
		String classPath = this.getClass().getClassLoader().getResource("").getPath();
		return classPath.startsWith("file:") && (classPath.indexOf(".jar!/BOOT-INF/") > 0 || classPath.indexOf(".war!") > 0);
	}

	/**
	 * 获取springboot打的jar文件路径
	 * 
	 * @return
	 */
	private String getBootJarPath() {
		String classPath = this.getClass().getClassLoader().getResource("").getPath();
		int end = classPath.indexOf("jar!");
		end = end == -1 ? classPath.indexOf("war!") : end;
		return classPath.substring(classPath.indexOf("file:") + 5, end + 3);
	}

	/**
	 * 获取class在jar中的路径
	 * 
	 * @param classAbsolutePath
	 * @return
	 */
	private String getClassPathInBootJar(String classAbsolutePath) {
		if(classAbsolutePath.indexOf(".jar!/BOOT-INF/") > -1){
			return classAbsolutePath.substring(classAbsolutePath.indexOf("/BOOT-INF") + 1, classAbsolutePath.length()).replaceFirst("classes!", "classes");
		} else if (classAbsolutePath.indexOf(".war!") > -1){
			return classAbsolutePath.substring(classAbsolutePath.indexOf("/WEB-INF") + 1, classAbsolutePath.length()).replaceFirst("classes!", "classes");
		}
		return classAbsolutePath;
	}

	/**
	 * 获取翻译器类型
	 * 
	 * 一个属性不允许有多个翻译器
	 */
	protected String getTranslatorAnnotationName(Object[] annotations) {
		String annotationName = null;
		if (annotations != null && annotations.length > 0) {
			Map<String, Translator> translators = translatorsInstance.getTranslators();
			int count = 0;
			for (Object annotation : annotations) {
				String currentAnnotationName = ((Annotation) annotation).annotationType().getName();
				if (translators.get(currentAnnotationName) != null) {
					count++;
					if (count > 1) {
						throw new MultipleTranslatorException();
					}
					annotationName = currentAnnotationName;
				}
			}
		}
		return annotationName;
	}

	public Set<String> getLocations() {
		return locations;
	}

	public void setLocations(Set<String> locations) {
		this.locations = locations;
	}

	public void setTranslators(Map<String, Translator> translators) {
		this.translators = translators;
	}
}
