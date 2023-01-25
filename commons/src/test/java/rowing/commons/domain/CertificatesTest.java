package rowing.commons.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rowing.commons.Certificates;
import rowing.commons.CoxCertificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CertificatesTest {

    @BeforeAll
    public static void setup() {
        Certificates.initialize();
    }

    @Test
    public void testExistName() {
        assertThat(Certificates.existByName("C4")).isEqualTo(true);
    }

    @Test
    public void testNonExistentName() {
        assertThat(Certificates.existByName("C12")).isEqualTo(false);
    }

    @Test
    public void certificateNonExistent() {
        assertThrows(RuntimeException.class, () -> {
            Certificates.getCertificate("dota_player");
        });
    }

    @Test
    public void certificateExists() {
        assertThat(Certificates.getCertificatesList().get(0)).isEqualTo(Certificates.getCertificate("C4"));
    }
}
