package com.jimprince99.gdpr;

import java.io.IOException;
import io.pkts.packet.sip.SipParseException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import io.pkts.PacketHandler;
import io.pkts.PcapOutputStream;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.TCPPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipMessage.Builder;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.impl.SipInitialLine;
import io.pkts.packet.sip.impl.SipRequestLine;
import io.pkts.packet.sip.impl.SipResponseLine;
import io.pkts.protocol.Protocol;
import io.pkts.packet.sip.address.URI;

public class GdprPacketHandler implements PacketHandler {
	private Logger logger = null;
	private boolean partial;
	private String resultString = "";
	private PcapOutputStream outputStream = null;
	private int packetCount = 0;

	/*
	 * Handle the different packet types
	 * 
	 * @see io.pkts.PacketHandler#nextPacket(io.pkts.packet.Packet)
	 */
	public boolean nextPacket(Packet packet) {
		packetCount++;
		Protocol protocol = getProtocol(packet);
		logger.info("packet " + packetCount + ", protocol=" + protocol.toString());

		switch (protocol) {
		case UDP:
			handleUDPPacket(packet);
			break;

		case TCP:
			handleTCPPacket(packet);
			break;

		default:
			writeWithException(packet);
			break;
		}
		return true;
	}

	private Protocol getProtocol(Packet packet) {

		try {
			if (packet.hasProtocol(Protocol.UDP)) {
				return Protocol.UDP;
			} else {
				if (packet.hasProtocol(Protocol.TCP)) {
					return Protocol.TCP;
				}
			}
		} catch (Exception e) {
			return Protocol.UNKNOWN;
		}
		return Protocol.UNKNOWN;
	}

	protected void handleTCPPacket(Packet packet) {
		try {
			if (packet.hasProtocol(Protocol.SIP)) {
				handleTCPSipPacket(packet);
			} else {
				// TODO handle other packet types here
				writeWithException(packet);
			}
		} catch (PacketParseException | IOException e) {
			warningLogger("Unable to get TCP packet" + packet.toString(), e);
			writeWithException(packet);
			return;
		}
		return;
	}

	protected void handleUDPPacket(Packet packet) {
		logger.info("UDP packet");
		try {
			if (packet.hasProtocol(Protocol.SIP)) {
				handleUDPSipPacket(packet);
			} else {
				// TODO handle other packet types here
				writeWithException(packet);
			}
		} catch (IOException e) {
			warningLogger("Unable to get UDP packet" + packet.toString(), e);
			writeWithException(packet);
			return;
		}
		return;
	}

	/*
	 * Get the UDP packet from the IP packet return the UDP packet, or null to show
	 * an error We write the original packet if we have an error
	 */
	private UDPPacket getUDPPacket(Packet ipPacket) {
		UDPPacket udpPacket = null;
		try {
			udpPacket = (UDPPacket) ipPacket.getPacket(Protocol.UDP);
		} catch (PacketParseException | IOException e1) {
			warningLogger("Failed to get UDP packet= " + ipPacket.toString(), e1);
			writeWithException(ipPacket);
			return null;
		}
		return udpPacket;
	}

	/*
	 * Get the TCP packet from the IP packet return the TCP packet, or null to show
	 * an error We write the original packet if we have an error
	 */
	private TCPPacket getTCPPacket(Packet ipPacket) {
		TCPPacket tcpPacket = null;
		try {
			tcpPacket = (TCPPacket) ipPacket.getPacket(Protocol.TCP);
		} catch (PacketParseException | IOException e1) {
			warningLogger("Failed to get TCP packet= " + ipPacket.toString(), e1);
			writeWithException(ipPacket);
			return null;
		}
		return tcpPacket;
	}

	private SipMessage getSipMessage(UDPPacket udpPacket, Packet ipPacket) {
		Buffer udpPayload = udpPacket.getPayload();
		logger.info("buffer = " + udpPayload.toString());

		SipMessage sipMessage = null;
		try {
			sipMessage = SipMessage.frame(udpPayload);
		} catch (SipParseException | IOException e) {
			warningLogger("Failed to get SipMessage packet= " + ipPacket.toString(), e);
			writeWithException(ipPacket);
			return null;
		}
		return sipMessage;
	}

	private SipMessage getSipMessage(TCPPacket tcpPacket, Packet ipPacket) {
		Buffer tcpPayload = tcpPacket.getPayload();
		logger.info("buffer = " + tcpPayload.toString());

		SipMessage sipMessage = null;
		try {
			sipMessage = SipMessage.frame(tcpPayload);
		} catch (SipParseException | IOException e) {
			warningLogger("Failed to get SipMessage packet= " + ipPacket.toString(), e);
			writeWithException(ipPacket);
			return null;
		}
		return sipMessage;
	}

	SipHeader updateUnknownHeader(SipHeader header) {
		Buffer name = header.getName();
		Buffer value = header.getValue();
		logger.info("name=" + name + ", value=" + value);

		Object[] frame = null;
		try {
			frame = AddressParametersHeader.frame(value);
		} catch (SipParseException e) {
			logger.info("Returning original header as this one does not parse");
			return header;
		} catch (IndexOutOfBoundsException e) {
			logger.info("Returning original header as this one does not parse.");
			return header;
		}
		logger.info("Header key=" + frame[0] + ", value=" + frame[1]);

		Address address = (Address) frame[0];
		Address newAddress = updateAddress(address);
		logger.info("New address is " + newAddress.toString());

		SipHeader newHeader = SipHeader.create(name.toString(), newAddress.toString());
		logger.info("Returning new Header=" + newHeader.toString());
		return newHeader;
	}
	
	Predicate<Address> hasDisplayName = (Address a) ->{
		return  ((a.getDisplayName() != null) && (a.getDisplayName().toString() != ""));
	};

	/*
	 * Generate an address with a new, anonymous MSISDN
	 */
	private Address updateAddress(Address address) {
		URI uri = address.getURI();

		SipURI sipUri = uri.toSipURI();
		Optional<Buffer> userBufferOptional = sipUri.getUser();
		Buffer userBuffer = null;
		if (userBufferOptional.isPresent()) {
			try {
				userBuffer = userBufferOptional.get();
			} catch (java.util.NoSuchElementException e) {
				return address;
			}
		} else
			return address;
		String user = userBuffer.toString();
		logger.info("Found userPart=" + user);
		
		// if this is too short to be an MSISDN
		if (user.length() < 9)
			return address;
		
		if (!user.matches("\\d*")) {
			logger.info("Returning as user is not all digits");
			return address;

		}

		// Time to make the new From Header
		Msisdn msisdn = Msisdn.getInstance();
		String newUserPart = msisdn.getMsisdn(user, partial);
		logger.info("Matching orig=" + user + " with new=" + newUserPart);

		io.pkts.packet.sip.address.Address.Builder newAddressBuilder = address.copy();
		newAddressBuilder.withUser(newUserPart);
		if (hasDisplayName.test(address)) {
			newAddressBuilder.withDisplayName(newUserPart);
		}

		return newAddressBuilder.build();
	}

	Predicate<SipHeader> hasNoMsisdn = header -> header.isViaHeader() || header.isRecordRouteHeader()
			|| header.isCallIdHeader() || header.isCSeqHeader() || header.isContentTypeHeader()
			|| header.isContentLengthHeader();

	protected void handleTCPSipPacket(Packet ipPacket) {
		if (isIrrelevantMethods(ipPacket))
			return;

		TCPPacket tcpPacket = getTCPPacket(ipPacket);
		if (tcpPacket == null)
			return;

		SipMessage sipMessage = getSipMessage(tcpPacket, ipPacket);
		if (sipMessage == null)
			return;

		handleSipMessage(sipMessage, ipPacket, tcpPacket);
	}

	/*
	 * Update the packet be removing any MSISDNs
	 */
	protected void handleUDPSipPacket(Packet ipPacket) {

		logger.info("handleUDPSIPPacket");
		if (isIrrelevantMethods(ipPacket))
			return;

		UDPPacket udpPacket = getUDPPacket(ipPacket);
		if (udpPacket == null)
			return;

		SipMessage sipMessage = getSipMessage(udpPacket, ipPacket);
		if (sipMessage == null)
			return;

		handleSipMessage(sipMessage, ipPacket, udpPacket);
	}

	protected void handleSipMessage(SipMessage sipMessage, Packet ipPacket, Packet p) {
		logger.info("got SipMessage=" + sipMessage.toString());
		SipInitialLine sipInitialLine = sipMessage.initialLine();
		Buffer method = null;
		String protocol = null;
		SipURI newSipUri = null;
		StringBuilder sb = new StringBuilder();
		
		if (sipInitialLine.isResponseLine()) {
			// nothing to correct here			
			logger.info("Got SIP Response message. Nothing to correct in the Request URI");
			sb.append(sipInitialLine.toString() + "\r\n\r\n");
		} else {
			// replace any MSISDN in the Request URL
			SipRequestLine sipRequestLine = sipInitialLine.toRequestLine();
			logger.info("sipRequestLine=" + sipRequestLine);

			method = sipRequestLine.getMethod();
			URI requestURL = sipRequestLine.getRequestUri();
			logger.info("requestURL=" + requestURL.toString());
			
			// extract the protocol "SIP/2.0"
			// but there's no method to get this field
			Buffer wholeLine = sipRequestLine.getBuffer();
			String wholeLineString = wholeLine.toString();
			String[] parts = wholeLineString.split(" ");
			if (parts.length == 3) {
				protocol = parts[2];
				logger.info("protocol=" + protocol);
			}
			SipURI sipUri = requestURL.toSipURI();
			
			Optional<Buffer> userBufferOptional = sipUri.getUser();
			Buffer userBuffer = null;
			boolean failed = false;
			logger.info("userBufferOptional=" + userBufferOptional.toString());

			if (userBufferOptional.isPresent()) {
				try {
					userBuffer = userBufferOptional.get();
				} catch (java.util.NoSuchElementException e) {
					failed = true;
				}
				if (failed == true) {
					logger.info("userBufferOptional.get() failed");
					sb.append(sipInitialLine.toString() + "\r\n\r\n");
				} else {

					String user = userBuffer.toString();
					logger.info("Found userPart=" + user + " from R-URI");

					// if this is too short to be an MSISDN
					if (user.length() < 9) {
						sb.append(sipInitialLine.toString());
					} else {
						// Time to make the new From Header
						Msisdn msisdn = Msisdn.getInstance();
						
						// if this is too short to be an MSISDN
						String newUserPart = null;
						if ( (!user.matches("\\d*"))
						||   (user.length() < 9   ) )
						{
							logger.info("Returning as user " + user + " is not all digits, or too short");
							newUserPart = user;
						} else {
							newUserPart = msisdn.getMsisdn(user);
							logger.info("Matching orig=" + user + " with new=" + newUserPart);
						}

						io.pkts.packet.sip.address.SipURI.Builder newRequestURIBuilder = sipUri.copy();
						newRequestURIBuilder.withUser(newUserPart);
						newSipUri = newRequestURIBuilder.build();

						sb.append(method + " ");
						sb.append(newSipUri + " ");
						sb.append(protocol + "\r\n\r\n");
						logger.info("Created new URI part=" + sb.toString());
					}
				}
			} else {// else no User part
				logger.info("userBufferOptional is not present");
				sb.append(sipInitialLine.toString() + "\r\n\r\n");
			}
		}
				
		SipMessage tempSipMessage = null;
		try {
			tempSipMessage = SipMessage.frame(sb.toString());
		} catch (SipParseException | IOException e1) {
			warningLogger("Unable to create a new SipMessage from " + sb.toString(), e1);
			writeWithException(ipPacket);
			return;
		}

		Builder<? extends SipMessage> newSipMessageBuilder = tempSipMessage.copy();
		newSipMessageBuilder.withBody(sipMessage.getContent());

		List<SipHeader> sipHeaders = sipMessage.getAllHeaders();
		SipHeader newSipHeader = null;

		for (SipHeader header : sipHeaders) {
			// skip over the headers we know will not hold MSISDNs
			if (hasNoMsisdn.test(header)) {
				newSipHeader = header;
			} else {
				newSipHeader = updateUnknownHeader(header);
			}
			newSipMessageBuilder.withHeader(newSipHeader);
		}
		SipMessage newSipMessage = newSipMessageBuilder.build();

		try {
			p.write((OutputStream) outputStream, newSipMessage.toBuffer());
		} catch (IOException e) {
			warningLogger("Failed to write packet= " + ipPacket.toString(), e);
			return;
		}
		return;
	}

	protected void setOutputStream(PcapOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	protected void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	protected void setPartial(boolean partial) {
		this.partial = partial;
	}

	protected PcapOutputStream getOutputStream() {
		return this.outputStream;
	}

	/*
	 * Handle any Sip Method that does not contain an MSISDN We just forward as is
	 * the SIP Packet
	 */
	private boolean isIrrelevantMethods(Packet ipPacket) {
		SipPacket sipPacket = null;
		try {
			sipPacket = (SipPacket) ipPacket.getPacket(Protocol.SIP);
		} catch (PacketParseException | IOException e) {
			warningLogger("Failed to get SipPacket from " + ipPacket.toString(), e);
			writeWithException(ipPacket);
			return true;
		}

		Buffer methodBuffer = sipPacket.getMethod();
		String method = methodBuffer.toString();
		logger.info("Method =" + method);

		switch (method.toLowerCase()) {
		case "notify":
		case "options":
		case "register":
			writeWithException(ipPacket);
			return true;

		default:
			return false;
		}
	}

	private void writeWithException(Packet p) {
		try {
			outputStream.write(p);
		} catch (IOException e) {
			warningLogger("Failed to write packet= " + p.toString(), e);
		}
	}

	private void warningLogger(String message, Exception e) {
		this.resultString = message;
		logger.warning(message + " " + e);
	}

	public String getResultString() {
		return this.resultString;
	}

}
