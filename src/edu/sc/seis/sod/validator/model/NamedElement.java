/**
 * NamedElement.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;

public class NamedElement extends AbstractGenitorForm{
    public NamedElement(int min, int max, String name){
        this(min, max, name, null);
    }

    public NamedElement(int min, int max, String name, Form parent){
        super(min, max, parent);
        this.name = name;
    }

    public String getXPath() {
        try{
            if(getParent() != null){
                return getParent().getXPath() + "/" + getName();
            }else{ return getName(); }
        }catch(NullPointerException e){
            e.printStackTrace();
            throw e;
        }
    }

    public Attribute[] getAttributes(){
        if(getChild() instanceof MultigenitorForm){
            return ((MultigenitorForm)getChild()).getAttributes();
        }else if(getChild() instanceof Attribute){
            return new Attribute[]{(Attribute)getChild()};
        }
        return null;
    }

    public NamedElement[] getElementalChildren(){
        if(getChild() instanceof MultigenitorForm){
            return ((MultigenitorForm)getChild()).getElementalChildren();
        }else if(getChild() instanceof NamedElement){
            return new NamedElement[]{(NamedElement)getChild()};
        }
        return null;
    }

    public String getName(){ return name; }

    public FormProvider copyWithNewParent(Form newParent) {
        NamedElement copy = new NamedElement(getMin(), getMax(), getName(), newParent);
        copyChildToNewParent(copy);
        copy.setAnnotation(getAnnotation());
        return copy;
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof NamedElement){
            NamedElement otherNamed = (NamedElement)o;
            return otherNamed.getName().equals(getName()) && super.equals(o);
        }
        return false;
    }

    public String toString(){
        return getName();
    }

    public void accept(FormVisitor v){
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }

    private String name;
}
