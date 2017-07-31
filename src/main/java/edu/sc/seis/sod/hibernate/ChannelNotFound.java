// **********************************************************************
//
// Generated by the ORBacus IDL to Java Translator
//
// Copyright (c) 2000
// Object Oriented Concepts, Inc.
// Billerica, MA, USA
//
// All Rights Reserved
//
// **********************************************************************

// Version: 4.0.5

package edu.sc.seis.sod.hibernate;

import edu.sc.seis.sod.model.station.ChannelId;

//
// IDL:iris.edu/Fissures/IfNetwork/ChannelNotFound:1.0
//
/***/

final public class ChannelNotFound extends org.omg.CORBA.UserException
{
    public
    ChannelNotFound()
    {
    }

    public
    ChannelNotFound(ChannelId channel)
    {
        this.channel = channel;
    }

    public
    ChannelNotFound(String reason,
                    ChannelId channel)
    {
        super(reason);
        this.channel = channel;
    }

    public ChannelId channel;
}