/**
 * CommandLineEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import edu.sc.seis.sod.SimpleErrorHandler;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.TemplateFileLoader;



public class CommandLineEditor {

    public CommandLineEditor(String[] args) throws ParserConfigurationException, IOException, SAXException, ParserConfigurationException, SAXException, DOMException, IOException, TransformerException {
        this.args = args;
        props = new Properties();
        props.load(this.getClass().getClassLoader().getResourceAsStream(Start.DEFAULT_PROPS));
        if (props.containsKey("log4j.rootCategory")) {
            // assume if this property is there, then log4j can be configured
            // from the props
            PropertyConfigurator.configure(props);
        }
        processArgs();
    }

    public void start() throws ParserConfigurationException, TransformerException {
        //Document doc = doReplacements(args);
        //            BufferedWriter buf =
        //                new BufferedWriter(new OutputStreamWriter(System.out));
        //            Writer xmlWriter = new Writer();
        //            xmlWriter.setOutput(buf);
        //            xmlWriter.write(doc);
        testXPath(args);
    }

    public Properties getProperties() {
        return props;
    }

    void processArgs() throws DOMException, IOException, ParserConfigurationException, IOException, SAXException, ParserConfigurationException, TransformerException {
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            if (i < args.length-1 && args[i].equals("-f")) {
                setConfigFile(args[i+1]);
            } else if (i < args.length-1 && args[i].equals("-props")) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(args[i+1]));
                props.load(bis);
                bis.close();
            } else if (args[i].equals("-help")) {
                help = true;
            }
        }
        if (help) {
            printOptions(new DataOutputStream(System.out));
        }
    }

    Document doReplacements(String[] args) throws ParserConfigurationException, TransformerException {
        Document doc = getDocument();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f")) {
                i++;
            } else if (args[i].equals("-help")) {
                //skip help
            } else {
                logger.debug("Processing "+args[i]);
                String expr = args[i].substring(1, args[i].indexOf("="));
                if ( false && ! expr.endsWith("/text()")) {
                    expr += "/text()";
                }
                String value = args[i].substring(args[i].indexOf("="));
                Node node = XPathAPI.selectSingleNode(doc, expr, doc.getDocumentElement());
                if (node instanceof Text) {
                    ((Text)node).setData(value);
                } else if (node == null) {
                    logger.warn("Replacement of "+expr+" failed.");
                } else {
                    throw new TransformerException("Trying to set text for "+
                                                       expr+
                                                       " but did not get a Text node."+
                                                       node.getClass());
                }
            }
        }
        return doc;
    }

    void testXPath(String[] args) throws ParserConfigurationException, TransformerException {
        Document doc = getDocument();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f")) {
                i++;
            } else if (args[i].equals("-help")) {
                //skip help
            } else {
                logger.debug("Processing "+args[i]);
                String expr = args[i];
                NodeList list = XPathAPI.selectNodeList(doc.getDocumentElement(), expr, doc.getDocumentElement());
                System.out.println("node list is length "+list.getLength());
                for (int j = 0; j < list.getLength(); j++) {
                    Node node = list.item(j);
                    System.out.print(j+" ");
                    if (node instanceof Text) {
                        System.out.println(expr+"  "+((Text)node).getData());
                    } else if (node == null) {
                        System.out.println("Expr gave NULL "+expr);
                    } else if (node instanceof Element) {
                        Element e = (Element)node;
                        System.out.println(expr+
                                               " element getNodeName="+e.getNodeName()+
                                               " prefix="+e.getPrefix()+
                                               " localName="+e.getLocalName()+
                                               " nsuri="+e.getNamespaceURI()+
                                               " tagname="+e.getTagName()
                                          );
                    } else if (node instanceof Document) {
                        Document e = (Document)node;
                        System.out.println(expr+
                                               " document getNodeName="+e.getNodeName()+
                                               " prefix="+e.getPrefix()+
                                               " localName="+e.getLocalName()+
                                               " nsuri="+e.getNamespaceURI());
                    } else {
                        System.out.println(expr+
                                               " node class ="+
                                               node.getClass()+" "+node.getNodeType());
                    }
                }
            }
        }
    }

    private void initConfigFile() throws FileNotFoundException, ParserConfigurationException, IOException, SAXException {
        InputStream in ;
        if(getConfigFilename().startsWith("jar")){
            in = TemplateFileLoader.getUrl(getClass().getClassLoader(),
                                           getConfigFilename()).openStream();
        }else{
            File configFile = new File(getConfigFilename());
            if (configFile.exists()) {
                in = new BufferedInputStream(new FileInputStream(configFile));}
            else {
                throw new FileNotFoundException("Can't find "+configFilename);
            }
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        docBuilder.setErrorHandler(new SimpleErrorHandler());
        document =  docBuilder.parse(in);
    }

    public void printOptions(DataOutputStream out) throws DOMException, IOException {
        printOptions(getDocument().getDocumentElement(), out);
    }

    protected void printOptions(Element element, DataOutputStream out) throws DOMException, IOException {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n instanceof Element) {
                printOptions((Element)n, out);
            } else if (n instanceof Text) {
                Text textNode = (Text)n;
                if ( ! textNode.getNodeValue().trim().equals("")) {
                    out.writeBytes(getFullName((Element)textNode.getParentNode())+"="+textNode.getNodeValue()+"\n");
                } else {
                    // ignore whitespace
                }
            }
        }
    }

    protected String getFullName(Element e) {
        if (e.getParentNode() != null && e.getParentNode() instanceof Element) {
            return getFullName((Element)e.getParentNode())+"/"+e.getTagName();
        } else {
            return "/"+e.getTagName();
        }
    }

    public String getConfigFilename(){ return configFilename; }

    public void setConfigFile(String filename) throws ParserConfigurationException, IOException, SAXException{
        setConfigFileLocation(filename);
        initConfigFile();
    }

    public void setConfigFileLocation(String filename) {
        this.configFilename = filename;
    }

    public Document getDocument() { return document; }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        CommandLineEditor cle = new CommandLineEditor(args);
        cle.start();
        System.out.println("Done editing.");
    }

    private String[] args;

    private String configFilename;

    private Properties props;

    private Document document;

    private static Logger logger = Logger.getLogger(CommandLineEditor.class);

}

