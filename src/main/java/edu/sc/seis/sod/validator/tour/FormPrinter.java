/**
 * FormPrinter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.validator.model.Attribute;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Data;
import edu.sc.seis.sod.validator.model.Empty;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.NotAllowed;
import edu.sc.seis.sod.validator.model.Text;
import edu.sc.seis.sod.validator.model.Value;

public class FormPrinter implements Tourist{
    public FormPrinter(){ this(2); }

    public FormPrinter(int indentLevel){
        for (int i = 0; i < indentLevel; i++){ this.indent += ' '; }
    }

    public void visit(Attribute attr) {
        String print = "@" + attr.getName() + " " + getCardinality(attr);
        printAndIncreaseDepth(print, attr);
    }

    public void visit(Choice choice) {
        printAndIncreaseDepth("Choice " + getCardinality(choice), choice);
    }

    private String getCardinality(Form choice) {
        return "Min: " + choice.getMin() + " Max: " + choice.getMax();
    }

    public void visit(Data d) {
        print("Data: " + d.getDatatype() + " " + getCardinality(d), d);
    }

    public void visit(Empty e) { print("Empty", e); }

    public void visit(Group g) {
        String print ="Group" + " " + getCardinality(g);
        printAndIncreaseDepth(print, g);
    }

    public void visit(Interleave i) {
        printAndIncreaseDepth("Interleave" + " " + getCardinality(i), i);
    }

    public void visit(NotAllowed na) { print("Not Allowed", na); }


    public void visit(NamedElement ne) {
        String print = ne.getName() + " " + getCardinality(ne);
        printAndIncreaseDepth(print, ne);
    }

    public void visit(Text t) { print("Text", t); }

    public void leave(Attribute attr) { depth--; }

    public void leave(Choice choice) { depth--; }

    public void leave(Group g) { depth--; }

    public void leave(Interleave i) { depth--; }

    public void leave(NamedElement ne) { depth--; }

    public void visit(Value v) {
        print("Value: " + v.getValue() + " " + v.getDatatype() + " " + getCardinality(v), v);
    }

    private void printAndIncreaseDepth(String s, Form f) {
        print(s, f);
        depth++;
    }
    private void print(String s, Form f){
        for (int i = 0; i < depth; i++) { System.out.print(indent); }
        System.out.println(s + " " + f.getAnnotation().getSummary());
    }

    private int depth = 0;
    private String indent = "";
}
