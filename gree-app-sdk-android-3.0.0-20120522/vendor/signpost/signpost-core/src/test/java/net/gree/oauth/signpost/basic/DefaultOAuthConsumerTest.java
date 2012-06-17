package net.gree.oauth.signpost.basic;

import net.gree.oauth.signpost.OAuthConsumer;
import net.gree.oauth.signpost.OAuthConsumerTest;
import net.gree.oauth.signpost.signature.OAuthMessageSigner;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnit44Runner;

@RunWith(MockitoJUnit44Runner.class)
public class DefaultOAuthConsumerTest extends OAuthConsumerTest {

    @Before
    public void prepare() {
        consumer = buildConsumer(CONSUMER_KEY, CONSUMER_SECRET, null);
    }

    @Override
    protected OAuthConsumer buildConsumer(String consumerKey, String consumerSecret,
            OAuthMessageSigner messageSigner) {
        return new DefaultOAuthConsumer(consumerKey, consumerSecret);
    }

}
