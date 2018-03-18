package trng.jms.rcvr;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import trng.jms.pojo.Header;
import trng.jms.queue.XmlFileObject;

public class QueueReceive {
	// Defines the JNDI context factory.
	public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

	// Defines the JMS connection factory for the queue.
	public final static String JMS_FACTORY = "jms/TestConnectionFactory";

	// Defines the queue.
	public final static String QUEUE = "jms/TestJMSQueue";
	
	public static SessionFactory sessionFactoryObj;
	
	public static org.hibernate.Session sessionObj;
	
	 private static SessionFactory buildSessionFactory()
	   {
	      try
	      {
	         if (sessionFactoryObj == null)
	         {
	            Configuration configuration = new Configuration().configure(QueueReceive.class.getResource("/hibernate.cfg.xml"));
	            StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
	            serviceRegistryBuilder.applySettings(configuration.getProperties());
	            ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
	            sessionFactoryObj = configuration.buildSessionFactory(serviceRegistry);
	         }
	         return sessionFactoryObj;
	      } catch (Throwable ex)
	      {
	         System.err.println("Initial SessionFactory creation failed." + ex);
	         throw new ExceptionInInitializerError(ex);
	      }
	   }

	public static void main(String[] args) throws Exception {

		// get the initial context
		InitialContext ctx = getInitialContext("t3://localhost:7001");

		// lookup the queue object
		Queue queue = (Queue) ctx.lookup(QUEUE);

		// lookup the queue connection factory
		QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);

		// create a queue connection
		QueueConnection queueConn = connFactory.createQueueConnection();

		// create a queue session
		QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		// create a queue receiver
		QueueReceiver queueReceiver = queueSession.createReceiver(queue);

		// start the connection
		queueConn.start();
		
		
		//Hibernate connection init
		try {
			sessionObj = buildSessionFactory().openSession();
			sessionObj.beginTransaction();
	
		
		

		XmlFileObject em = null;

		Message message = queueReceiver.receive(1);

		if (message instanceof ObjectMessage) {

			em = (XmlFileObject) ((ObjectMessage) message).getObject();
	
			List<File> lf = em.getXmlfiles();

			for (File l : lf) {
				System.out.println("file is   " + l.getName());
			}

			for (File file : lf) {
			Header head=	parseXmlFile(file);
			sessionObj.save(head);

			}

		}
		sessionObj.getTransaction().commit();
		// close the queue connection
		queueConn.close();
	}// Committing The Transactions To The Database
		 catch(Exception sqlException) {
		/*if(null != sessionObj.getTransaction()) {
			System.out.println("\n.......Transaction Is Being Rolled Back.......");*/
			sessionObj.getTransaction().rollback();
		//}
		sqlException.printStackTrace();
	} finally {
		if(sessionObj != null) {
			sessionObj.close();
		}
	}
}

	private static Header parseXmlFile(File f) {
		
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		Header head=new Header();
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			System.out.println("parsexmlfile called");
			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(f);

			Element docEle = dom.getDocumentElement();
			
			NodeList nl = docEle.getElementsByTagName("*");
			StringBuffer arr=new StringBuffer();
			for (int temp = 0; temp < nl.getLength(); temp++) {
				Node nNode = nl.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					System.out.println(eElement.getTextContent());	
					arr.append(eElement.getTextContent());
					arr.append(",");
				}
			}
String temp=arr.toString();
String[] s=temp.split(",");

String para=s[0];
int id=Integer.parseInt(s[1]);
String name=s[2];
int age=Integer.parseInt(s[3]);

head.setAge(age);
head.setId(id);
head.setName(name);
head.setPara(para);
		
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return head;
	}

	private static InitialContext getInitialContext(String url) throws NamingException {
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, url);
		return new InitialContext(env);
	}
}