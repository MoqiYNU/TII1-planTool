package toolkits.utils.block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QiMo 
 * 定义Petri网中基本块
 */
public class Block {
	
	//定义基本块的入口和出口
	private String entry;
	private String exit;
	
	//定义入口后面关联的库所或变迁集
	private List<String> entryPost;
	
	//定义出口前面关联的库所或变迁集
	private List<String> exitPre;
	
	//定义顺序块中变迁集
	private List<String> seqActs;
	
	//定义块的类型
	private String type;
	
	public Block() {
		entryPost = new ArrayList<>();
		exitPre = new ArrayList<>();
		seqActs = new ArrayList<>();
	}
	
	public void addEntryPost(String act) {
		if (!entryPost.contains(act)) {
			entryPost.add(act);
		}
	}
	
	public void addExitPre(String act) {
		if (!exitPre.contains(act)) {
			exitPre.add(act);
		}
	}
	
	public void addSeqActs(String act) {
		if (!seqActs.contains(act)) {
			seqActs.add(act);
		}
	}
	
	public String getEntry() {
		return entry;
	}
	public void setEntry(String entry) {
		this.entry = entry;
	}
	public String getExit() {
		return exit;
	}
	public void setExit(String exit) {
		this.exit = exit;
	}
	public List<String> getEntryPost() {
		return entryPost;
	}
	public void setEntryPost(List<String> entryActs) {
		this.entryPost = entryActs;
	}
	public List<String> getExitPre() {
		return exitPre;
	}
	public void setExitPre(List<String> exitActs) {
		this.exitPre = exitActs;
	}
	public List<String> getSeqActs() {
		return seqActs;
	}
	public void setSeqActs(List<String> seqActs) {
		this.seqActs = seqActs;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
