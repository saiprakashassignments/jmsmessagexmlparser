package trng.jms.queue;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XmlFileObject implements Serializable {
List<File> xmlfiles=new ArrayList<File>();

public List<File> getXmlfiles() {
	return xmlfiles;
}

public void setXmlfiles(List<File> xmlfiles) {
	this.xmlfiles = xmlfiles;
}
}
