package toolkits.utils.file;

import java.io.File;
import java.io.FileWriter;

/**
 * @author Moqi
 * �����ļ���Utils
 */
public class FileUtils {
	
	public void savePT(StringBuffer buffer, String fileName) throws Exception {
		
		String filePath = "D:\\experiments\\pt\\" 
	                       + fileName + ".dot";
		// 1.ɾ�������ļ�
		File traceFile = new File(filePath);
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.д�������ļ�
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void saveLTS(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\lts\\" 
	                       + fileName + ".dot";
		File traceFile = new File(filePath);
		// 1.ɾ�������ļ�
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.д�������ļ�
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void saveProNet(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\pe\\" 
	                       + fileName + ".dot";
		File traceFile = new File(filePath);
		// 1.ɾ�������ļ�
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.д�������ļ�
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	public void savePetrify(StringBuffer buffer, String fileName) throws Exception {
		String filePath = "D:\\experiments\\py\\" 
	                      + fileName + ".g";
		File traceFile = new File(filePath);
		// 1.ɾ�������ļ�
    	if(traceFile.exists()){
    		System.out.println("Existing file is deleted");
    	    traceFile.delete();
    	}
    	// 2.д�������ļ�
    	FileWriter fw = new FileWriter(filePath);
    	fw.write(buffer.toString());
        System.out.println("writing complete");
        fw.close();
	}
	
	
	//ɾ��ָ���ļ��м������������ļ�
	public void delFolder(String folderPath) {
	     try {
	        clearFile(folderPath); //ɾ����������������
	        String filePath = folderPath;
	        filePath = filePath.toString();
	        java.io.File myFilePath = new java.io.File(filePath);
	        myFilePath.delete(); //ɾ�����ļ���
	     } catch (Exception e) {
	       e.printStackTrace(); 
	     }
	}
	
	//ɾ��ָ���ļ����������ļ�,����pathΪ�ļ�����������·��
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
	             clearFile(path + "/" + tempList[i]);//��ɾ���ļ���������ļ�
	             delFolder(path + "/" + tempList[i]);//��ɾ�����ļ���
	             flag = true;
	          }
	       }
	       return flag;
	     }
	     
}
