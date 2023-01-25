package rowing.commons;


import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Certificates {
    @Getter
    private static List<CoxCertificate> certificatesList;

    /**
     * Initializes the known certificates. Is called exactly once when authentication microservice starts.
     */
    public static void initialize() {
        certificatesList = new ArrayList<>();
        CoxCertificate cer1 = new CoxCertificate("C4", null);
        certificatesList.add(cer1);
        CoxCertificate cer2 = new CoxCertificate("4+", new ArrayList<>(Arrays.asList("C4")));
        certificatesList.add(cer2);
        CoxCertificate cer3 = new CoxCertificate("8+", new ArrayList<>(Arrays.asList("C4", "4+")));
        certificatesList.add(cer3);
    }

    /**
     * Checks if a certificate with a given name exists already.
     *
     * @param name - the name of the certificate to look for.
     * @return - true if found, false otherwise.
     */
    public static boolean existByName(String name) {
        for (CoxCertificate c : certificatesList) {
            if (c.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for certificate.
     *
     * @param name - name of the certificate
     * @return - a certificate object.
     */
    public static CoxCertificate getCertificate (String name) {
        if (existByName(name)) {
            for (CoxCertificate certificate : certificatesList) {
                if (certificate.getName().equals(name)) {
                    return certificate;
                }
            }
        }
        throw new RuntimeException();
    }
}
