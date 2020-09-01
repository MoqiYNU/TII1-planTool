package toolkits.utils.file;

import java.io.File;
import java.io.FileWriter;

/**
 * @author Moqi
 * 定义文件的Utils
 */
public class FileUtils {
	
	public void savePT(StringBuffer buffer, String fileName) throws Exception {
		
		String filePath = "D:\\experiments\\pt\\" 
	                       + fileName + ".dot";
		// 1.删除已有文件
		File traceFile = new File(filePath);
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.写入生成文件
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void saveLTS(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\lts\\" 
	                       + fileName + ".dot";
		File traceFile = new File(filePath);
		// 1.删除已有文件
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.写入生成文件
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void saveProNet(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\pe\\" 
	                       + fileName + ".dot";
		File traceFile = new File(filePath);
		// 1.删除已有文件
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.写入生成文件
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void savePetrify(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\py\\" 
	                      + fileName + ".g";
		File traceFile = new File(filePath);
		// 1.删除已有文件
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.写入生成文件
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	
	//删除指定文件夹及其下面所有文件
	public void delFolder(String folderPath) {
	     try {
	        clearFile(folderPath); //删除完里面所有内容
	        String filePath = folderPath;
	        filePath = filePath.toString();
	        java.io.File myFilePath = new java.io.File(filePath);
	        myFilePath.delete(); //删除空文件夹
	     } catch (Exception e) {
	       e.printStackTrace(); 
	     }
	}
	
	//删除指定文件夹下所有文件,其中path为文件夹完整绝对路径
	public boolean clearFile(String path) {
	       boolean flag = false;
	       File file = new File(path);
	       if (!file.exists()) {
	         return flag;
	       }
	       if (!file.isDirectory()) {
	         return flag;
	       }
	       String[] tempList = file.list();
	       File temp = null;
	       for (int i = 0; i < tempList.length; i++) {
	          if (path.endsWith(File.separator)) {
	             temp = new File(path + tempList[i]);
	          } else {
	              temp = new File(path + File.separator + tempList[i]);
	          }
	          if (temp.isFile()) {
	             temp.delete();
	          }
	          if (temp.isDirectory()) {
	             clearFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
	             delFolder(path + "/" + tempList[i]);//再删除空文件夹
	             flag = true;
	          }
	       }
	       return flag;
	     }
	     
}
