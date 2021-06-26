package com.mobiversa.ezy2pay.ui.ezyWire;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

class TLVParser {
	
	protected static String toHexString(byte b) {
		return Integer.toString( ( b & 0xFF ) + 0x100, 16).substring( 1 );
	}
	
	protected static String toHexString(byte[] b) {
		if(b == null) {
			return "null";
		}
		String result = "";
		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xFF ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
	
	protected static byte[] hexToByteArray(String s) {
		if(s == null) {
			s = "";
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		for(int i = 0; i < s.length() - 1; i += 2) {
			String data = s.substring(i, i + 2);
			bout.write(Integer.parseInt(data, 16));
		}
		return bout.toByteArray();
	}
	
	protected static Hashtable<String, String> parseToHashtable(String tlv) {
		Hashtable<String, String> decodeData = new Hashtable<String, String>();
		List<TLV> tlvList = parse(tlv);
		for(int i = 0; i < tlvList.size(); ++i) {
			TLV temp = tlvList.get(i);
			decodeData.put(temp.tag, temp.value);
			String tag = "";
			if(temp.tag.equalsIgnoreCase("C0")) {
				tag = "onlineMessageKsn";
			} else if(temp.tag.equalsIgnoreCase("C1")) {
				tag = "onlinePinKsn";
			} else if(temp.tag.equalsIgnoreCase("C2")) {
				tag = "encOnlineMessage";
			} else if(temp.tag.equalsIgnoreCase("C3")) {
				if(searchTLV(tlvList, "C5") != null) {
					tag = "batchKsn";
				} else if(searchTLV(tlvList, "C6") != null) {
					tag = "reversalKsn";
				} else {
					continue;
				}
			} else if(temp.tag.equalsIgnoreCase("C4") || temp.tag.equalsIgnoreCase("DF03")) {
				tag = "maskedPAN";
				while(temp.value.endsWith("f") || temp.value.endsWith("F")) {
					temp.value = temp.value.substring(0, temp.value.length() - 1);
				}
			} else if(temp.tag.equalsIgnoreCase("C5")) {
				tag = "encBatchMessage";
			} else if(temp.tag.equalsIgnoreCase("C6")) {
				tag = "encReversalMessage";
			} else if(temp.tag.equalsIgnoreCase("C7")) {
				tag = "track2EqKsn";
			} else if(temp.tag.equalsIgnoreCase("C8")) {
				tag = "encTrack2Eq";
			} else if(temp.tag.equalsIgnoreCase("C9")) {
				tag = "encPAN";
			} else if(temp.tag.equalsIgnoreCase("CA")) {
				tag = "encTrack2EqRandomNumber";
			} else if(temp.tag.equalsIgnoreCase("CB") || temp.tag.equalsIgnoreCase("DF41")) {
				tag = "finalMessage";
			} else if(temp.tag.equalsIgnoreCase("CC")) {
				tag = "serviceCode";
				temp.value = temp.value.substring(0, 3);
			} else if(temp.tag.equalsIgnoreCase("CD")) {
				tag = "mac";
			} else if(temp.tag.equalsIgnoreCase("CE")) {
				tag = "macKsn";
			} else if(temp.tag.equalsIgnoreCase("DF8203")) {
				tag = "encWorkingKey";
			} else if(temp.tag.equalsIgnoreCase("DF8204")) {
				tag = "accountSelected";
			} else if (temp.tag.equalsIgnoreCase("DF8321")) {
				tag = "epbRandomNumber";
			} else if (temp.tag.equalsIgnoreCase("DF8315")) {
				tag = "data";
			} else if (temp.tag.equalsIgnoreCase("DF826E")) {
				tag = "serialNumber";
			} else if (temp.tag.equalsIgnoreCase("DF834F")) {
				tag = "bID";
			} else if (temp.tag.equalsIgnoreCase("9F1F")) {
				tag = "track1DiscretionaryData";
			} else if (temp.tag.equalsIgnoreCase("57")) {
				tag = "track2Equivalent";
			} else if (temp.tag.equalsIgnoreCase("9F20")) {
				tag = "track2DiscretionaryData";
			} else if (temp.tag.equalsIgnoreCase("5F30")) {
				tag = "serviceCode";
			} else if (temp.tag.equalsIgnoreCase("5F24")) {
				tag = "applicationExpirationDate";
			} else if (temp.tag.equalsIgnoreCase("5F20")) {
				tag = "cardholderName";
			} else if (temp.tag.equalsIgnoreCase("9F0B")) {
				tag = "carholderNameExtended";
			} else if (temp.tag.equalsIgnoreCase("5A")) {
				tag = "PAN";
			} else if (temp.tag.equalsIgnoreCase("5F34")) {
				tag = "PANSequenceNumber";
			} else if (temp.tag.equalsIgnoreCase("42")) {
				tag = "IIN";
			} else if (temp.tag.equalsIgnoreCase("56")) {
				tag = "track1MasterCard";
			} else if (temp.tag.equalsIgnoreCase("9F6B")) {
				tag = "track2Data";
			} else if (temp.tag.equalsIgnoreCase("DF812A")) {
				tag = "DDCardTrack1";
			} else if (temp.tag.equalsIgnoreCase("DF812B")) {
				tag = "DDCardTrack2";
			} else if (temp.tag.equalsIgnoreCase("5F21")) {
				tag = "track1";
			} else if (temp.tag.equalsIgnoreCase("5F22")) {
				tag = "track2";
			} else if (temp.tag.equalsIgnoreCase("5F23")) {
				tag = "track3";
			} else if (temp.tag.equalsIgnoreCase("58")) {
				tag = "track3EquivalentData";
			} else if (temp.tag.equalsIgnoreCase("59")) {
				tag = "cardExpirationDate";
			} else {
				continue;
			}
			decodeData.put(tag, temp.value);
		}
		return decodeData;
	}
	
	protected static List<TLV> parse(String tlv) {
		try {
			return getTLVList(hexToByteArray(tlv));
		} catch(Exception e) {
			return null;
		}
	}
	
	private static List<TLV> getTLVList(byte[] data) {
		int index = 0;
		
		ArrayList<TLV> tlvList = new ArrayList<TLV>();
		
		while(index < data.length) {
		
			byte[] tag;
			byte[] length;
			byte[] value;
			
			boolean isNested;
			if((data[index] & (byte)0x20) == (byte)(0x20)) {
				isNested = true;
			} else {
				isNested = false;
			}
			
			if((data[index] & (byte)0x1F) == (byte)(0x1F)) {
				int lastByte = index + 1;
				while((data[lastByte] & (byte)0x80) == (byte)0x80) {
					++lastByte;
				}
				tag = new byte[lastByte - index + 1];
				System.arraycopy(data, index, tag, 0, tag.length);
				index += tag.length;
			} else {
				tag = new byte[1];
				tag[0] = data[index];
				++index;
				
				if(tag[0] == 0x00) {
					break;
				}
			}
			
			if((data[index] & (byte)0x80) == (byte)(0x80)) {
				int n = (data[index] & (byte)0x7F) + 1;
				length = new byte[n];
				System.arraycopy(data, index, length, 0, length.length);
				index += length.length;
			} else {
				length = new byte[1];
				length[0] = data[index];
				++index;
			}
			
			int n = getLengthInt(length);
			value = new byte[n];
			System.arraycopy(data, index, value, 0, value.length);
			index += value.length;
			
			TLV tlv = new TLV();
			tlv.tag = toHexString(tag);
			tlv.length = toHexString(length);
			tlv.value = toHexString(value);
			tlv.isNested = isNested;
			
			if(isNested) {
				tlv.tlvList = getTLVList(value);
			}
			
			tlvList.add(tlv);
		}
		
		Collections.sort(tlvList);
		return tlvList;
	}
	
	public static List<TLV> parseWithoutValue(String tlv) {
		try {
			return getTLVListWithoutValue(hexToByteArray(tlv));
		} catch(Exception e) {
			return null;
		}
	}
	
	private static List<TLV> getTLVListWithoutValue(byte[] data) {
		int index = 0;
		
		ArrayList<TLV> tlvList = new ArrayList<TLV>();
		
		while(index < data.length) {
		
			byte[] tag;
			byte[] length;
			
			boolean isNested;
			if((data[index] & (byte)0x20) == (byte)(0x20)) {
				isNested = true;
			} else {
				isNested = false;
			}
			
			if((data[index] & (byte)0x1F) == (byte)(0x1F)) {
				int lastByte = index + 1;
				while((data[lastByte] & (byte)0x80) == (byte)0x80) {
					++lastByte;
				}
				tag = new byte[lastByte - index + 1];
				System.arraycopy(data, index, tag, 0, tag.length);
				index += tag.length;
			} else {
				tag = new byte[1];
				tag[0] = data[index];
				++index;
				
				if(tag[0] == 0x00) {
					break;
				}
			}
			
			if((data[index] & (byte)0x80) == (byte)(0x80)) {
				int n = (data[index] & (byte)0x7F) + 1;
				length = new byte[n];
				System.arraycopy(data, index, length, 0, length.length);
				index += length.length;
			} else {
				length = new byte[1];
				length[0] = data[index];
				++index;
			}
			
			TLV tlv = new TLV();
			tlv.tag = toHexString(tag);
			tlv.length = toHexString(length);
			tlv.isNested = isNested;
			
			tlvList.add(tlv);
		}
		return tlvList;
	}
	
	private static int getLengthInt(byte[] data) {
		if((data[0] & (byte)0x80) == (byte)(0x80)) {
			int n = data[0] & (byte)0x7F;
			int length = 0;
			for(int i = 1; i < n + 1; ++i) {
				length <<= 8; 
				length |= (data[i] & 0xFF);
			}
			return length;
		} else {
			return data[0] & 0xFF;
		}
	}
	
	protected static TLV binarySearchTLV(List<TLV> tlvList, String targetTag) {
		TLV tlv = new TLV();
		tlv.tag = targetTag;
		int index = Collections.binarySearch(tlvList, tlv);
		if(index >= 0) {
			tlv = tlvList.get(index);
		} else {
			tlv = new TLV();
		}
		return tlv;
	}
	
	protected static TLV searchTLV(List<TLV> tlvList, String targetTag) {
		for(int i = 0; i < tlvList.size(); ++i) {
			TLV tlv = tlvList.get(i);
			if(tlv.tag.equalsIgnoreCase(targetTag)) {
				return tlv;
			} else if(tlv.isNested) {
				TLV searchChild = searchTLV(tlv.tlvList, targetTag);
				if(searchChild != null) {
					return searchChild;
				}
			}
		}
		return null;
	}
	
	public static String getValue(List<TLV> tlvList, String targetTag) {
		if(tlvList == null) {
			return "";
		}
		TLV temp = searchTLV(tlvList, targetTag);
		if (temp != null) {
			return temp.value.toUpperCase();
		} else {
			return "";			
		}
	}
}
