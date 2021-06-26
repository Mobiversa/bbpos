package com.mobiversa.ezy2pay.ui.ezyWire;

import java.util.List;

class TLV implements Comparable<TLV> {
	
	public String tag;
	public String length;
	public String value;
	
	public boolean isNested;
	public List<TLV> tlvList;
	
	@Override
	public int compareTo(TLV tlv) {
		return tag.compareToIgnoreCase(tlv.tag);
	}
	
}
