/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrFTPD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DrFTPD; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.drftpd.commands.speedtest.net;

import org.apache.log4j.Logger;
import org.drftpd.master.RemoteSlave;
import org.drftpd.util.HttpUtils;
import org.tanesha.replacer.ReplacerEnvironment;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author scitz0
 */
public class SpeedTestUtils {
	private static final Logger logger = Logger.getLogger(SpeedTestUtils.class);

	private static final String[] _speedTestURLS = {
			"http://www.speedtest.net/speedtest-servers-static.php",
			"http://c.speedtest.net/speedtest-servers-static.php",
			"http://www.speedtest.net/speedtest-servers.php",
			"http://c.speedtest.net/speedtest-servers.php"};

	private static final String _freegeoip = "http://freegeoip.net/xml/";

	/**
	 * Determine the 5 closest speedtest.net servers based on geographic distance
	 */
	public static HashSet<SpeedTestServer> getClosetsServers() {

		HashSet<SpeedTestServer> serverList = new HashSet<SpeedTestServer>();

		// Get servers from speedtest.net
		for (String url : _speedTestURLS) {
			try {
				String data = HttpUtils.retrieveHttpAsString(url);
				serverList.addAll(parseXML(data));
			} catch (Exception e) {
				logger.warn("Failed to get data from " + url + " :: " + e.getMessage());
				return null;
			}
		}

		return serverList;
	}

	private static HashSet<SpeedTestServer> parseXML(String xmlString) {
		HashSet<SpeedTestServer> serverList = new HashSet<SpeedTestServer>();

		try {
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(
					new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
			while(xmlEventReader.hasNext()) {
				//Get next event.
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				//Check if event is the start element.
				if (xmlEvent.isStartElement()) {
					//Get event as start element.
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("server")) {
						SpeedTestServer server = new SpeedTestServer();
						//Iterate and process attributes.
						Iterator iterator = startElement.getAttributes();
						while (iterator.hasNext()) {
							Attribute attribute = (Attribute) iterator.next();
							String name = attribute.getName().getLocalPart();
							String value = attribute.getValue();
							if (name.equals("url")) {
								server.setUrl(value);
							} else if (name.equals("url2")) {
								server.setUrl2(value);
							} else if (name.equals("lat")) {
								server.setLatitude(Double.parseDouble(value));
							} else if (name.equals("lon")) {
								server.setLongitude(Double.parseDouble(value));
							} else if (name.equals("name")) {
								server.setName(value);
							} else if (name.equals("country")) {
								server.setCountry(value);
							} else if (name.equals("cc")) {
								server.setCc(value);
							} else if (name.equals("sponsor")) {
								server.setSponsor(value);
							} else if (name.equals("id")) {
								server.setId(Integer.parseInt(value));
							} else if (name.equals("host")) {
								server.setHost(value);
							}
						}
						serverList.add(server);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException, run speedtest with -refresh option to redownload server list: " + e.getMessage());
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException, run speedtest with -refresh option to redownload server list: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Something went wrong parsing speedtest.net server list: " + e.getMessage());
		}

		return serverList;
	}

	public static SlaveLocation getSlaveLocation(RemoteSlave rslave) {
		SlaveLocation slaveLocation = new SlaveLocation();

		try {
			String xmlString = HttpUtils.retrieveHttpAsString(_freegeoip + rslave.getPASVIP());
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(
					new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
			while (xmlEventReader.hasNext()) {
				//Get next event.
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				//Check if event is the start element.
				if (xmlEvent.isStartElement()) {
					//Get event as start element.
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("Latitude")) {
						xmlEvent = xmlEventReader.nextEvent();
						slaveLocation.setLatitude(Double.parseDouble(xmlEvent.asCharacters().getData()));
					} else if (startElement.getName().getLocalPart().equals("Longitude")) {
						xmlEvent = xmlEventReader.nextEvent();
						slaveLocation.setLongitude(Double.parseDouble(xmlEvent.asCharacters().getData()));
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException, failed parsing slave lat/lon: " + e.getMessage());
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException, failed parsing slave lat/lon: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Something went wrong getting slave location: " + e.getMessage());
		}

		return slaveLocation;
	}

	public static double getDistance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}

		return (dist);
	}
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	public static void addServerEnvVariables(SpeedTestServer server, ReplacerEnvironment env) {
		env.add("server.url", server.getUrl());
		env.add("server.name", server.getName());
		env.add("server.country", server.getCountry());
		env.add("server.cc", server.getCc());
		env.add("server.sponsor", server.getSponsor());
		env.add("server.id", server.getId());
		env.add("server.host", server.getHost());
		env.add("server.latency", server.getLatency());
		env.add("server.latitude", server.getLatitude());
		env.add("server.longitude", server.getLongitude());
	}
}
