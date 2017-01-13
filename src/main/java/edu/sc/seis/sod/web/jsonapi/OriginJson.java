package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;


public class OriginJson extends AbstractJsonApiData {

    public OriginJson(OriginImpl origin, String baseUrl) {
        super(baseUrl);
        this.origin = origin;
    }
    
    @Override
    public String getType() {
        return "origin";
    }

    @Override
    public String getId() {
        return ""+origin.getDbid();
    }
    
    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        Location loc = origin.getLocation();
        out.key("time").value(ISOTime.getISOString(origin.getTime()))
        .key("latitude").value(loc.latitude)
        .key("longitude").value(loc.longitude)
        .key("elevation").value(((QuantityImpl)loc.elevation).getValue(UnitImpl.METER))
        .key("depth").value(((QuantityImpl)loc.depth).getValue(UnitImpl.KILOMETER))
        .key("contributor").value(origin.getContributor())
        .key("catalog").value(origin.getCatalog());
    }

    OriginImpl origin;
}