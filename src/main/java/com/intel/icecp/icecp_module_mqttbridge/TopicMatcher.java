package com.intel.icecp.icecp_module_mqttbridge;

/**
 * Java version of subscription topic checker roughly based on mosquitto topic
 * checker
 * 
 */
public class TopicMatcher {

	/* Does a topic match a subscription? */
	public static boolean isTopicSubscribed(String sub, String topic) {
		int spos, tpos;

		int slen = sub.length();
		int tlen = topic.length();

		if (slen != 0 && tlen != 0) {
			if ((sub.charAt(0) == '$' && topic.charAt(0) != '$') || (sub.charAt(0) != '$') && topic.charAt(0) == '$') {
				return false;
			}
		}

		for (spos = tpos = 0; spos < slen && tpos < tlen;) {
			if (sub.charAt(spos) != topic.charAt(tpos)) {
				if (sub.charAt(spos) == '+') {
					for (spos++; tpos < tlen && topic.charAt(tpos) != '/'; tpos++)
						;

					if (tpos == tlen && spos == slen) {
						return true;
					}
				} else if (sub.charAt(spos) == '#') {
					return (spos + 1 == slen);
				} else {
					return false;
				}
			} else {
				if (tpos + 1 == tlen) {
					/* Check for e.g. bar matching bar/# */
					if (spos == slen - 3 && sub.startsWith("/#", spos + 1)) {
						return true;
					}
				}
				spos++;
				tpos++;
				if (spos == slen && tpos == tlen) {
					return true;
				} else if (tpos == tlen && spos == slen - 1 && sub.charAt(spos) == '+') {
					return true;
				}
			}
		}
		return !(tpos < tlen || spos < slen);
	}
}
