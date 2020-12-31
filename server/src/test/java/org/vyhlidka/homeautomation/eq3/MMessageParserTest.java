package org.vyhlidka.homeautomation.eq3;

import org.junit.jupiter.api.Test;
import org.vyhlidka.homeautomation.eq3.domain.MMaxMessage;

import static org.assertj.core.api.Assertions.assertThat;

public class MMessageParserTest {

    private MMessageParser parser = new MMessageParser();

    @Test
    public void testParse() throws Exception {
        String message = "M:00,01,VgIDAQtMaXZpbmcgUm9vbRUWXgIIQmF0aHJvb20VFlYDB0JlZHJvb20VFLAIAxaBdU5FUTEyMDM3MTkYTGl2aW5nIFJvb20gVGhlcm1vc3RhdCAxAQEVFl5NRVExODE4MjEyGExpdmluZyBSb29tIFJhZGlhdG9yIEJpZwEBFRV6TUVRMTgxNzk4MhpMaXZpbmcgUm9vbSBSYWRpYXRvciBTbWFsbAEDFnrWTkVRMTIwMTU5MhhCYXRocm9vbSBXYWxsIFRoZXJtb3N0YXQCARUWVk1FUTE4MTgyMTAXQmF0aHJvb20gUmFkaWF0b3IgTGFkZXICARUWI01FUTE4MTgxNTAcQmF0aHJvb20gUmFkaWF0b3IgVGhlcm1vc3RhdAIDFoEGTkVRMTIwMzE2ORdXYWxsIFRoZXJtb3N0YXQgQmVkcm9vbQMBFRSwTUVRMTgxNzgyMRtSYWRpYXRvciBUaGVybW9zdGF0IEJlZHJvb20DAQ==";
        final MMaxMessage mMsg = this.parser.parse(message);

        assertThat(mMsg).isNotNull();
    }
}