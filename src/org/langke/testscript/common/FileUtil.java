package org.langke.testscript.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 公共文件处理类
 * @author langke
 * @since JDK1.6
 * @version 1.0
 *
 */
public class FileUtil {
	private  BufferedReader br;
	private  String line;
 
	/**
	 * 取文件内容
	 * @param fileName
	 * @return
	 */
	public StringBuffer getFile(String fileName){
		StringBuffer stringBuffer = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)),"UTF-8"));
			while ((line = br.readLine()) != null) {
				stringBuffer.append(line).append("\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuffer;
	}
	
/*	public Document getDocument(String filePath){
		SAXBuilder builder = new SAXBuilder();
		Document document = null;
		try {
			document = builder.build(new File(filePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document;
	}*/

	/**
	 * 判断文件编码
	 * @param file
	 * @return
	 */
	public static String getCharset( File file ) {   
        String charset = "GBK";   
        byte[] first3Bytes = new byte[3];   
        try {   
            boolean checked = false;   
            BufferedInputStream bis = new BufferedInputStream( new FileInputStream( file ) );   
            bis.mark( 0 );   
            int read = bis.read( first3Bytes, 0, 3 );   
            if ( read == -1 ) return charset;   
            if ( first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE ) {   
                charset = "UTF-16LE";   
                checked = true;   
            }   
            else if ( first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF ) {   
                charset = "UTF-16BE";   
                checked = true;   
            }   
            else if ( first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF ) {   
                charset = "UTF-8";   
                checked = true;   
            }   
            bis.reset();   
            if ( !checked ) {   
            //    int len = 0;   
             //   int loc = 0;   
  
                while ( (read = bis.read()) != -1 ) {   
                //    loc++;   
                    if ( read >= 0xF0 ) break;   
                    if ( 0x80 <= read && read <= 0xBF ) // 单独出现BF以下的，也算是GBK   
                    break;   
                    if ( 0xC0 <= read && read <= 0xDF ) {   
                        read = bis.read();   
                        if ( 0x80 <= read && read <= 0xBF ) // 双字节 (0xC0 - 0xDF) (0x80   
                                                                        // - 0xBF),也可能在GB编码内   
                        continue;   
                        else break;   
                    }else if ( 0xE0 <= read && read <= 0xEF ) {// 也有可能出错，但是几率较小   
                        read = bis.read();   
                        if ( 0x80 <= read && read <= 0xBF ) {   
                            read = bis.read();   
                            if ( 0x80 <= read && read <= 0xBF ) {   
                                charset = "UTF-8";   
                                break;   
                            }   
                            else break;   
                        }   
                        else break;   
                    }   
                }   
                //System.out.println( loc + " " + Integer.toHexString( read ) );   
            }   
  
            bis.close();   
        } catch ( Exception e ) {   
            e.printStackTrace();   
        }   
  
        return charset;   
    }   

	public static void deleteFile(File oldPath) {
		if (oldPath.isDirectory()) {
			File[] files = oldPath.listFiles();
			for (File file : files) {
				deleteFile(file);
			}
		} else {
			oldPath.delete();
		}
	}
	/**
	 * String转JDOM Element
	 * @param xmlString
	 * @return
	 */
/*	public static Element getJdomElement(String xmlString){
		xmlString = xmlString.replaceAll("UTF-8", "gb2312");//UTF-8无法识别中文
		InputStream inputStream;
		SAXBuilder builder = new SAXBuilder();
		Document document;
		Element element = null;
		try {
			inputStream = new ByteArrayInputStream(xmlString.getBytes());
			document = builder.build(inputStream);		      
		    element = document.getRootElement(); // 获得根节点
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return element;
	}*/
}
