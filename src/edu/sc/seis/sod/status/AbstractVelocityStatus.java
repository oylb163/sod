/**
 * AbstractVelocityStatus.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramTemplateGenerator;
import edu.sc.seis.sod.status.networkArm.NetworkArmContext;
import edu.sc.seis.sod.status.networkArm.NetworkArmMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractVelocityStatus  implements WaveformArmMonitor, NetworkArmMonitor {

    public AbstractVelocityStatus(Element config) throws SQLException, MalformedURLException, IOException {
        this(getFileDir(config), getTemplateName(config));
    }

    public AbstractVelocityStatus(String fileDir, String templateName) throws  SQLException, IOException {
        this.fileDir = fileDir;
        this.templateName = templateName;
        networkArmContext = new NetworkArmContext(CookieJar.getCommonContext());
        template = loadTemplate(templateName);
        try {
            Element menuEl = TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                            MenuTemplate.TEMPLATE_LOC);
            MenuTemplate menu = new MenuTemplate(menuEl, createExamplePath(),
                                                 fileDir);
            renderedMenu = menu.getResult();
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Trouble creating menuTemplate for Velocity based status",
                                          e);
        }
    }

    public static String getFileDir(Element config){
        String fileDir = getNestedTextForElement("fileDir", config);
        if(fileDir != null){ return fileDir; }
        return  FileWritingTemplate.getBaseDirectoryName();
    }

    public static String getTemplateName(Element config) throws MalformedURLException{
        String templateName = getNestedTextForElement("template", config);
        if(templateName != null){ return templateName; }
        throw new MalformedURLException("template config param is null");
    }

    public static String getNestedTextForElement(String elementName, Element config){
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element element = (Element)n;
                if (element.getTagName().equals(elementName)){
                    return SodUtil.getNestedText(element);
                }
            }
        }
        return null;
    }


    //creates a path with the same number of directories as the actual location
    //will for use in menu creationw
    private String createExamplePath(){
        String path = fileDir + '/';
        for (int i = 0; i < getNumDirDeep(); i++) { path += "dir/"; }
        return path + "file.html";
    }

    /**
     * Method getNumDirDeep returns how many directories below the base status
     * directory this template will write
     */
    public abstract int getNumDirDeep();

    /** loads the default template, given by the <template> tag in the config. */
    protected String loadTemplate() throws IOException {
        return loadTemplate(templateName);
    }

    protected String loadTemplate(String templateName) throws IOException {
        URL templateURL = TemplateFileLoader.getUrl(this.getClass().getClassLoader(), templateName);
        BufferedReader read = new BufferedReader(new InputStreamReader(templateURL.openStream()));
        String line, outTemplate="";
        while ((line = read.readLine()) != null) {
            outTemplate += line+System.getProperty("line.separator");
        }
        read.close();
        return outTemplate;
    }

    /** Schedules the default template (from the <template> element in the config,
     * for output. */
    public void scheduleOutput(final String filename, final Context context) {
        scheduleOutput(filename, context, template);
    }

    public void scheduleOutput(final String filename, final Context context, final String template) {
        if ( ! runnableMap.containsKey(filename)) {
            Runnable runner = new Runnable() {
                public void run() {
                    runnableMap.remove(filename);
                    StringWriter out = new StringWriter();
                    try {
                        VelocityEngine engine = LocalSeismogramTemplateGenerator.getVelocity();
                        synchronized (engine) {
                            // the new VeocityContext "wrapper" is to help with a possible memory leak
                            // due to velocity gathering introspection information,
                            // see http://jakarta.apache.org/velocity/developer-guide.html#Other%20Context%20Issues
                            Context tempContext = new VelocityContext(context);
                            tempContext.put("menu", renderedMenu);
                            engine.evaluate(tempContext, out, filename, template);
                        }
                        FileWritingTemplate.write(fileDir+"/"+filename,
                                                  out.getBuffer().toString());
                    } catch (Exception e) {
                        GlobalExceptionHandler.handle(e);
                    }
                }
            };
            runnableMap.put(filename, runner);
            OutputScheduler.getDefault().schedule(runner);
        }
    }

    protected NetworkArmContext networkArmContext;
    private String renderedMenu, fileDir, templateName, template = "";
    protected HashMap runnableMap = new HashMap();
}
