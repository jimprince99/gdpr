package com.jimprince99.gdpr;

import java.util.Random;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.AddressParametersHeader;
//import io.pkts.packet.sip.header.FromHeader;
//import io.pkts.packet.sip.header.FromHeader.Builder;
//import io.pkts.packet.sip.header.impl.FromHeaderImpl;


public interface PAssertedIdentityHeaderBuilder extends AddressParametersHeader {

	Buffer NAME = Buffers.wrap("P-Asserted-Identity");
    //Buffer COMPACT_NAME = Buffers.wrap("p");
    
    Buffer getTag() throws SipParseException;

    @Override
    PAssertedIdentityHeaderBuilder clone();

    default boolean isPAssertedIdentyHeader() {
        return true;
    }

    default PAssertedIdentityHeaderBuilder toPAssertedIdentityHeader() {
        return this;
    }
    
    /**
     * Frame the value as a {@link FromHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static PAssertedIdentityHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer original = buffer.slice();
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new PAssertedIdentityHeaderImpl(original, (Address) result[0], (Buffer) result[1]);
    }
    
    /**
     * Generate a new tag that can be used as a tag parameter for the {@link FromHeader}. A
     * tag-parameter only has to be unique within the same Call-ID space so therefore it doesn't
     * have to be cryptographically strong etc.
     * 
     * @return
     */
    static Buffer generateTag() {
        // TODO: fix this and move it to a better place.
        return Buffers.wrap(Integer.toHexString(new Random().nextInt()));
    }
    
    static AddressParametersHeader.Builder<PAssertedIdentityHeader> builder() {
        return new Builder();
    }

    static AddressParametersHeader.Builder<PAssertedIdentityHeader> withHost(final Buffer host) throws SipParseException {
        return builder().withHost(host);
    }

    static AddressParametersHeader.Builder<PAssertedIdentityHeader> withHost(final String host) throws SipParseException {
        return builder().withHost(host);
    }

    static AddressParametersHeader.Builder<PAssertedIdentityHeader> withAddress(final Address address) throws SipParseException {
        return builder().withAddress(address);
    }

    @Override
    AddressParametersHeader.Builder<PAssertedIdentityHeader> copy();

    class Builder extends AddressParametersHeader.Builder<PAssertedIdentityHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public PAssertedIdentityHeader internalBuild(final Buffer rawValue, final Address address, final Buffer params) throws SipParseException {
            return new PAssertedIdentityHeaderImpl(rawValue, address, params);
        }
    }

    
    

}
