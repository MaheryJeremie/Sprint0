package util;

public class Mapping {
    String className;
    String methodeName;
    String verb;
    public Mapping(String className, String methodeName) {
        this.className = className;
        this.methodeName = methodeName;
    }
    
    public Mapping(String className, String methodeName, String verb) {
		this.className = className;
		this.methodeName = methodeName;
		this.verb = verb;
	}

	public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodeName() {
        return methodeName;
    }
    public void setMethodeName(String methodeName) {
        this.methodeName = methodeName;
    }
	public String getVerb() {
		return verb;
	}
	public void setVerb(String verb) {
		this.verb = verb;
	}
    
    
}
