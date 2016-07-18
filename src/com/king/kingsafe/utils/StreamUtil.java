package com.king.kingsafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
	/**
	 * 流转换成字符串
	 * @param is 流对象
	 * @return	返回null代表异常
	 */

	public static String stream2String(InputStream is) {
		//1.在读取过程中 , 将读取的内容进行缓存 , 然后一次性转换成字符串返回
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//2.一次读取1024
		byte[] buffer = new byte[1024];
		//3.记录读取内容的临时变量
		int temp = -1;
		try {
			//4.读流操作 , 读到没有为止(循环)
			while((temp = is.read(buffer)) != -1){
				bos.write(buffer, 0, temp);
			}
			//5.将读取的流转换成字符串返回
			return bos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try{
				//6.关闭流 先读后写
				is.close();
				bos.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}

}
