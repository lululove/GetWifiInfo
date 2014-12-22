package com.example.getwifiinfo;

public class WifiItem {

	private String ssid;
	private String bssid;
	private String pwd;
	private String level;
	private String encryption;
	private String type;

	public WifiItem() {

	}

	public WifiItem(String ssid, String bssid, String pwd, String level,
			String encryption) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.pwd = pwd;
		this.level = level;
		this.encryption = encryption;
	}

	public WifiItem(String ssid, String bssid, String pwd, String encryption) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.pwd = pwd;
		this.encryption = encryption;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getBssid() {
		return bssid;
	}

	public void setBssid(String bssid) {
		this.bssid = bssid;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_WPA2
	}

	public WifiCipherType getConnectType() {
		String info = encryption.toLowerCase();
		if (info.indexOf("wpa2-psk") != -1)
			return WifiCipherType.WIFICIPHER_WPA;
		if (info.indexOf("wpa-psk") != -1) {
			return WifiCipherType.WIFICIPHER_WPA2;
		}
		if (info.indexOf("wep") != -1) {
			return WifiCipherType.WIFICIPHER_WEP;
		}
		return WifiCipherType.WIFICIPHER_NOPASS;
	}

	public String getEntryInfo() {
		StringBuffer sb = new StringBuffer();
		String info = encryption.toLowerCase();
		if (info.indexOf("wpa-psk") != -1) {
			sb.append("WPA/");
		}
		if (info.indexOf("wpa2-psk") != -1) {
			sb.append("WPA2/");
		}
		if (info.indexOf("wep") != -1) {
			sb.append("WPA/");
		}
		if (sb.toString().trim().equals("")) {
			return new String("no entry");
		}
		if (sb.toString().lastIndexOf("/") == sb.length() - 1) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}

	}

}
