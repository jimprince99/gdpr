package com.jimprince99.gdpr;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.impl.AddressParametersHeaderImpl;
//import io.pkts.packet.sip.header.FromHeader;


/**
 * @author jim prince
 */
public class PAssertedIdentityHeaderImpl extends AddressParametersHeaderImpl implements PAssertedIdentityHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public PAssertedIdentityHeaderImpl(final Buffer value, final Address address, final Buffer params) {
        super(PAssertedIdentityHeader.NAME, value, address, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    @Override
    public PAssertedIdentityHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new PAssertedIdentityHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public AddressParametersHeader.Builder<PAssertedIdentityHeader> copy() {
        return PAssertedIdentityHeader.withAddress(getAddress()).withParameters(getRawParams().slice());
    }

    public PAssertedIdentityHeader ensure() {
        return this;
    }

    
    public boolean isAddressParametersHeader() {
    	return true;
    }
}
