/**
 * Data.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public class Data extends AbstractForm {

    public Data(int min, int max, ModelDatatype datatype) {
        this(min, max, datatype, null);
    }

    public Data(int min, int max, ModelDatatype datatype, Form parent) {
        super(min, max, parent);
        this.datatype = datatype;
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Data d = new Data(getMin(), getMax(), getDatatype(), newParent);
        super.copyGutsOver(d);
        return d;
    }

    public ModelDatatype getDatatype() {
        return datatype;
    }

    public String toString() {
        return "Data of type " + getDatatype();
    }

    public void accept(Tourist v) {
        v.visit(this);
    }

    private ModelDatatype datatype;
}