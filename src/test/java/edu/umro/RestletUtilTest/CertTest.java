package edu.umro.RestletUtilTest;

import org.restlet.Context;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;


/**
 * Make HTTPS calls to verify that security is being enforced via certificates.
 * <p> </p>
 * This will print a lot of errors, but the only one that matters is the 'Finished' line.
 */

public class CertTest {

    private static int passCount = 0;
    private static int failCount = 0;

    private static void test(String url, Boolean pass) {

        ClientResource clientResource = new ClientResource(new Context(), url);
        // clientResource.setChallengeResponse(challengeResponse);
        try {
            Representation representation = clientResource.get();
            String text = representation.getText();
            System.out.println("----------\n" + text.substring(0, 100) + "\n----------");

            if (pass) {
                System.out.println("pass");
                passCount++;
            } else {
                System.out.println("fail");
                failCount++;
            }

        } catch (Throwable e) {
            System.out.println("Failed: " + e.getMessage());

            if (pass) {
                System.out.println("fail");
                failCount++;
            } else {
                System.out.println("pass");
                passCount++;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting...");
        // ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "userID", "password");

        String[] passList = {
                "https://automatedqualityassurance.org",
                "https://www.google.com/",
                "https://badssl.com/"
        };

        String[] failList = {
                "https://self-signed.badssl.com",
                "https://expired.badssl.com/",
                "https://untrusted-root.badssl.com/",
                "https://automatedqualityassurance.org/view/OutputList" // this will fail
        };

        for (String url : passList) test(url, true);

        for (String url : failList) test(url, false);

        System.out.println("Finished.   passCount: " + passCount + "    failCount: " + failCount);
    }

}
