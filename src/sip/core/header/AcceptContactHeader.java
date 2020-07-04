
package sip.core.header;

/** 
 * SIP Header AcceptContact
 * 
 * Added by mandrajg for Sipdroid open source project.
 * Used with MMTel/IMS. 
 */

public class AcceptContactHeader extends ParametricHeader {
	public AcceptContactHeader(String icsi) {
		super(SipHeaders.ACCEPT_CONTACT, "*");
		if (icsi != null)
			this.setParameter("+g.3gpp.icsi-ref", icsi);
	}

	public AcceptContactHeader() {
		super(SipHeaders.ACCEPT_CONTACT, "*");
	}

	public AcceptContactHeader(Header hd) {
		super(hd);
	}


}
