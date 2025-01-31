package com.itdevcloud.japp.se.common.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CIDRAddress {

	private static final Logger logger = Logger.getLogger(CIDRAddress.class.getName());
	private String cidr = null;
	private String cidrIp =null;
	private int cidrMaskBiteNumber = -1;
	
	private InetAddress cidrInetAddress;
	private byte[] cidrAddressBytes;
	private String cidrInetAddressString;
	
	private InetAddress startInetAddress;
	private byte[] startAddressBytes;
	private String startInetAddressString;

	private InetAddress endInetAddress;
	private byte[] endAddressBytes;
	private String endInetAddressString;
	
	private double count = -1;
	
	public CIDRAddress(String cidr) {
		if(cidr == null || (cidr = cidr.trim()).isEmpty()) {
			throw new RuntimeException("CIDR Adress is null or empty.");
		}
		this.cidr = cidr;
		this.cidrIp = cidr;
		this.cidrMaskBiteNumber = -1;
		if (cidr.indexOf('/') > 0) {
			//there is mask
			String[] cidrArray = cidr.split("/");
			this.cidrIp = cidrArray[0];
			this.cidrMaskBiteNumber = Integer.parseInt(cidrArray[1]);
		}
		this.cidrInetAddress = parseInetAddress(cidrIp);
		if(this.cidrInetAddress == null || (this.cidrInetAddress.getAddress().length * 8 < this.cidrMaskBiteNumber )) {
			throw new RuntimeException("CIDR Adress is not valid. CIDR = " + cidr);
		}
		
		if(this.cidrMaskBiteNumber <= 0) {
			//no mask
			this.startInetAddress = this.cidrInetAddress;
			this.startAddressBytes =  this.startInetAddress.getAddress();
			this.startInetAddressString = cidrIp;
			
			this.endInetAddress = this.cidrInetAddress;
			this.endAddressBytes =  this.endInetAddress.getAddress();
			this.endInetAddressString = cidrIp;
			this.count = 1;
			return;
		}
		this.cidrAddressBytes = this.cidrInetAddress.getAddress();
		this.startAddressBytes = new byte[this.cidrAddressBytes.length];
		this.endAddressBytes = new byte[this.cidrAddressBytes.length];
		
		int countBitNumber = this.cidrAddressBytes.length * 8 - this.cidrMaskBiteNumber;
		this.count = Math.pow(2, countBitNumber);

		
		int maskFullByteNumber = this.cidrMaskBiteNumber / 8;
		
		//set full bytes as the first step
		for (int i = 0; i < maskFullByteNumber; i++) {
			this.startAddressBytes[i] = this.cidrAddressBytes[i];
			this.endAddressBytes[i] = this.cidrAddressBytes[i];
		}
		
		// set next byte after full bytes 
		// "& 0x07" equals to "% 8" (mod 8) to get how many "1" from left
		// to create lastMaskByte: the way below is the easiest) 
		//e.g. /27 the mask for the last check byte is 11100000 (three "1" from left)
		byte lastMaskByte = (byte) (0xFF00 >> (this.cidrMaskBiteNumber & 0x07));
		if (lastMaskByte != 0) {
			this.startAddressBytes[maskFullByteNumber] = (byte) (this.cidrAddressBytes[maskFullByteNumber] & lastMaskByte) ;
			this.endAddressBytes[maskFullByteNumber]  = (byte) (this.startAddressBytes[maskFullByteNumber] | ( ~lastMaskByte));
		}else {
			if(maskFullByteNumber <  this.cidrAddressBytes.length ) {
				this.startAddressBytes[maskFullByteNumber] = 0x0;
				this.endAddressBytes[maskFullByteNumber] = (byte) 0xff;
			}
		}
		for(int i = maskFullByteNumber + 1; i < this.cidrAddressBytes.length; i++ ) {
			this.startAddressBytes[i] = 0x0;
			this.endAddressBytes[i] = (byte) 0xff;
		}
		
		try {
			this.startInetAddress = InetAddress.getByAddress(this.startAddressBytes);
			this.startInetAddressString = this.startInetAddress.getHostAddress();

			this.endInetAddress = InetAddress.getByAddress(this.endAddressBytes);
			this.endInetAddressString = this.endInetAddress.getHostAddress();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public String getCidr() {
		return cidr;
	}

	public InetAddress getStartInetAddress() {
		return startInetAddress;
	}
	public String getStartInetAddressString() {
		return startInetAddressString;
	}
	public InetAddress getEndInetAddress() {
		return endInetAddress;
	}
	public String getEndInetAddressString() {
		return endInetAddressString;
	}

	public InetAddress getNextInetAddress(InetAddress currentInetAddress, boolean ascend, boolean inRange) {
		if(currentInetAddress == null) {
			return null;
		}
		byte[] nextAddrBytes = (ascend? plusOne(currentInetAddress.getAddress(), inRange):minusOne(currentInetAddress.getAddress(), inRange));
		try {
			InetAddress nextAddr = (nextAddrBytes == null?null:InetAddress.getByAddress(nextAddrBytes));
			return nextAddr;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String  getNextInetAddress(String currentIpString, boolean ascend, boolean inRange) {
		 InetAddress nextAddr = getNextInetAddress( parseInetAddress(currentIpString), ascend, inRange);
		 String ipString = (nextAddr==null?null: nextAddr.getHostAddress());
		return ipString;
	}


	public boolean isInRangeV2(InetAddress targetIpInetAddress) {
		if(targetIpInetAddress == null) {
			logger.fine("target IP Address is null.");
			return false;
		}
		//System.out.println("target IP = " + targetIpInetAddress.getHostAddress());
		if (this.cidrMaskBiteNumber < 0) {
			return cidrInetAddress.equals(targetIpInetAddress);
		}
		byte[] targetIpBytes = targetIpInetAddress.getAddress();
		if(compareUnsignedByteArray(targetIpBytes, this.startAddressBytes) >= 0 && compareUnsignedByteArray(targetIpBytes, this.endAddressBytes) <= 0) {
			return true;
		}else {
			return false; 
		}
	}
	public boolean isInRangeV1(String targetIp) {
		return isInRangeV1( parseInetAddress(targetIp));
	}
	
	public boolean isInRangeV2(String targetIp) {
		return isInRangeV2( parseInetAddress(targetIp));
	}

	/*
	 * this is an other approach
	 */
	public boolean isInRangeV1(InetAddress targetIpInetAddress) {
		if( targetIpInetAddress == null) {
			System.out.println("Tagret IP Adress is null");
			return false;
		}
		if (this.cidrMaskBiteNumber < 0) {
			return cidrInetAddress.equals(targetIpInetAddress);
		}
		byte[] targetIpAddrByte = targetIpInetAddress.getAddress();
		int maskFullByteNumber = this.cidrMaskBiteNumber / 8;
		//check full bytes as the first step
		for (int i = 0; i < maskFullByteNumber; i++) {
			if (targetIpAddrByte[i] != this.cidrAddressBytes[i]) {
				return false;
			}
		}
		// check next byte after full bytes - which is the last byte need to be checked, all bytes after this byte can be ignored
		//get mask for the next byte
		// "& 0x07" equals to "% 8" (mod 8) to get how many "1" from left (i.e. bits left to be checked) (i.e. mask for the last check byte)
		// to create mask for the last check byte: the way below is the easiest) 
		//e.g. /27 the mask for the last check byte is 11100000 (three "1" from left)
		// (byte) cast will remove extra "1" on the left beyond 1 byte
		byte finalCheckedByteMask = (byte) (0xFF00 >> (this.cidrMaskBiteNumber & 0x07));
		if (finalCheckedByteMask != 0) {
			return (targetIpAddrByte[maskFullByteNumber] & finalCheckedByteMask) == (this.cidrAddressBytes[maskFullByteNumber] & finalCheckedByteMask);
		}
		return true;
	}

    public String getCIDRInfo(boolean listAllIPs, boolean ascend) {
    	String str = "CIDR = " + this.cidr + ", Start IP = " + this.getStartInetAddressString() + ", End IP = " + this.endInetAddressString;
    	NumberFormat nf = NumberFormat.getNumberInstance();
    	nf.setMaximumFractionDigits(0);
    	String countString = nf.format(this.count);
    	str = str + ", IP count = " + countString;
    	if(listAllIPs) {
    		List<String> ipList = this.getIpList(ascend);
    		str = str + ", IP List:\n";
    		for(String ipStr: ipList) {
    			str = str + ipStr + "\n";
    		}
    	}
    	return str;
    }
    
    public List<String> getIpList(boolean ascend) {
    	List<String> ipList = new ArrayList<String>();
    	String tmpIp = (ascend?this.startInetAddressString:this.endInetAddressString);
    	while (tmpIp != null) {
    		ipList.add(tmpIp);
    		tmpIp = this.getNextInetAddress(tmpIp, ascend, true);
    	}
    	return ipList;
    }
    
	private InetAddress parseInetAddress(String address) {
		try {
			return InetAddress.getByName(address);
		}
		catch (Exception e) {
			logger.fine("can not parse this address string to an InetAddress: '" + address + "'\n" + e);
			return null;
		}
	}

	private byte[] minusOne(byte[] bytes, boolean inRange) {
		if(bytes == null) {
			return null;
		}
		if(inRange &&  compareUnsignedByteArray(bytes, this.startAddressBytes) <= 0 ) {
			return null;
		}
		for (int i = bytes.length -1 ; i >= 0 ; i--) {
			if(bytes[i] == 0) {
				if( i == 0) {
					return null;
				}else {
					bytes[i] = (byte) 0xFF;
					continue;
				}
			}else {
				bytes[i] = (byte) (bytes[i] - 1) ;
				break;
			}
		}
		if(inRange && compareUnsignedByteArray(bytes, this.endAddressBytes) > 0) {
			return null;
		}else {
			return bytes;
		}
	}

	private byte[] plusOne(byte[] bytes, boolean inRange) {
		if(bytes == null) {
			return null;
		}
		if(inRange &&  compareUnsignedByteArray(bytes, this.endAddressBytes) >= 0) {
			return null;
		}
		for (int i = bytes.length -1 ; i >= 0 ; i--) {
			if(bytes[i] == -1) {
				if( i == 0) {
					return null;
				}else {
					bytes[i] = 0 ;
					continue;
				}
			}else {
				bytes[i] = (byte) (bytes[i] + 1);
				int k = bytes[i] & 0xFF;
				break; 
			}
		}
		if(inRange &&  compareUnsignedByteArray(bytes, this.startAddressBytes) < 0) {
			return null;
		}else {
			return bytes;
		}
	}
	private int compareUnsignedByteArray(byte[] bytes1, byte[] bytes2) {
		if(bytes1 == null && bytes2 == null) {
			return 0;
		}else if(bytes1 == null) {
			return -1;
		}else if (bytes2 == null) {
			return 1;
		}else if(bytes1.length > bytes2.length) {
			return 1;
		}else if(bytes1.length < bytes2.length) {
			return -1;
		}else {
			for (int i = 0; i < bytes1.length; i++) {
				int v1 = bytes1[i] & 0xFF;
				int v2 = bytes2[i] & 0xFF;
				if( v1 > v2) {
					return 1;
				}else if (v1 < v2) {
					return -1;
				}else {
					continue;
				}
			}
		}
		return 0;
	}


	
	public static void main(String[] args) {
		CommonLogger.initJavaUtilLogger(null);
		
		CIDRAddress cIDRAddress = null;
		String targetIP = null;
		
		//test 1
		cIDRAddress = new CIDRAddress("192.168.2.1/28");
		targetIP = "192.168.2.10";
		
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV1(targetIP));
		//test 2
		cIDRAddress = new CIDRAddress("192.168.2.1/27");
		targetIP = "192.168.2.50";
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV1(targetIP));

		//test 3
		cIDRAddress = new CIDRAddress("192.168.2.1/24");
		targetIP = "192.168.2.0";
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV1(targetIP));

		//test 4
		cIDRAddress = new CIDRAddress("192.168.2.1/23");
		targetIP = "192.168.1.15";
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, false) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV1(targetIP));

		//test 5
		cIDRAddress = new CIDRAddress("192.168.2.1/32");
		targetIP = "192.168.1.15";
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV1(targetIP));
		
		//test 6
		cIDRAddress = new CIDRAddress("192.168.2.19/32");
		targetIP = "192.168.2.19";
		logger.info("isInRange() Result: CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", result = " + cIDRAddress.isInRangeV2(targetIP));
		
		//test 7
		cIDRAddress = new CIDRAddress("192.168.2.1/27");
		targetIP = "192.168.2.31";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, true, true));

		targetIP = "192.168.1.254";
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, true, true));

		targetIP = "192.168.1.255";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, true, true));

		targetIP = "192.168.2.50";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, true, true));

		targetIP = "192.168.2.32";
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next des ip = " + cIDRAddress.getNextInetAddress(targetIP, false, false));
		logger.info(" CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next des in range ip = " + cIDRAddress.getNextInetAddress(targetIP, false, true));

		targetIP = "192.168.2.33";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next des ip = " + cIDRAddress.getNextInetAddress(targetIP, false, false));
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next des in range ip = " + cIDRAddress.getNextInetAddress(targetIP, false, true));

		targetIP = "192.168.2.0";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, false, true));
		
		targetIP = "192.168.1.255";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, true, false));
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, false, true));

		targetIP = "192.168.2.50";
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc ip = " + cIDRAddress.getNextInetAddress(targetIP, false, false));
		logger.info("CIDR = " + cIDRAddress.getCIDRInfo(false, true) + ", targetIP = " +  targetIP + ", next asc in range ip = " + cIDRAddress.getNextInetAddress(targetIP, false, true));

	}

}