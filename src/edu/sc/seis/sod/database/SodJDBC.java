/**
 * SodJDBC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import java.io.IOException;

public class SodJDBC{
    static{
        ConnMgr.addPropsLocation("edu/sc/seis/sod/database/props/");
    }
}

