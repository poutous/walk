package org.walkframework.base.system.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.walkframework.base.system.common.Common;
import org.walkframework.data.util.DataMap;
import org.walkframework.data.util.IData;


public class TEXTConfig implements IConfig{
	
	private File file ;
	
	public TEXTConfig(String file) throws Exception {
		String url = Common.getInstance().getClassResource(file).getPath();
		this.file = new File(url);
	}
	
	public String getProperty(String prop) throws Exception {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
			String temp = null;
			
			do{
				temp = br.readLine();
				if (temp == null) break;
				if (temp.startsWith("#")) continue;
				int index = temp.indexOf("=");
				if (index == -1) continue;
				String key = temp.substring(0,index).trim();
				String value = temp.substring(index+1).trim();
				if (key.equals(prop)) {
					br.close();
					return value;			
				}
			}
			while(temp != null);
			br.close();
			return null;
		} catch (Exception e) {
			// TODO: handle exception
			if (br != null) br.close();
			throw e;
		}
		
	}
	
	public List getLineList() throws Exception{
		BufferedReader br = null;
		ArrayList list = new ArrayList();
		try {
			br = new BufferedReader(new FileReader(file));
			String temp = br.readLine();
			while(temp != null){
				list.add(temp);
				temp = br.readLine();
			}
			return list;
		} catch (Exception e) {
			// TODO: handle exception
			if (br != null) br.close();
			throw e;
		}
	}
	
	public IData getProperties(String prop) throws Exception {
		return new DataMap();
	}

}
