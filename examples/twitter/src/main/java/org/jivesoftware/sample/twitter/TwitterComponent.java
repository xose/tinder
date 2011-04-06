package org.jivesoftware.sample.twitter;

import java.util.List;

import org.xmpp.component.AbstractComponent;
import org.xmpp.packet.Message;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterComponent extends AbstractComponent {

	private final Twitter twitter;

	public TwitterComponent() {
		this.twitter = new TwitterFactory().getInstance();
	}

	@Override
	public String getName() {
		return "Twitter";
	}

	@Override
	public String getDescription() {
		return "Twitter sample component";
	}

	@Override
	protected void handleMessage(Message message) {
		log.debug("Received message:" + message.toXML());

		Message reply = createReply(message, null);

		try {
			String screenName = message.getBody();
			List<Status> timeline = twitter.getUserTimeline(screenName);

			StringBuilder sb = new StringBuilder();
			sb.append("Timeline for " + screenName);

			for (Status status : timeline) {
				sb.append("\n- " + status.getText());
			}

			reply.setBody(sb.toString());
		} catch (TwitterException e) {
			if (e.resourceNotFound()) {
				reply.setBody("Screen name not found");
			} else if (e.exceededRateLimitation()) {
				reply.setBody("Too many queries. Please try again in " + e.getRetryAfter() + " seconds");
			} else if (e.isCausedByNetworkIssue()) {
				log.warn("Twitter network error!", e);
				reply.setBody("Network error. Please try again later");
			} else {
				log.warn("Unknown Twitter error!", e);
				reply.setBody("Unknown error " + e.getStatusCode());
			}
		}

		send(reply);
	}

	private Message createReply(Message message, String replyBody) {
		Message reply = new Message();

		reply.setTo(message.getFrom());
		reply.setFrom(message.getTo());
		reply.setType(message.getType());
		reply.setThread(message.getThread());

		if (replyBody != null) {
			reply.setBody(replyBody);
		}

		return reply;
	}

}
